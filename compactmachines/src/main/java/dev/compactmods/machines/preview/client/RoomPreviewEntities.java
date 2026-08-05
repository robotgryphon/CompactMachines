package dev.compactmods.machines.preview.client;

import dev.compactmods.machines.preview.RoomEntitySnapshot;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntitySpawnRequest;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Client-side pool of throwaway {@link Entity} instances used only to <em>render</em> the live
 * entities in a room preview, treated like real tracked entities so the motion reads as continuous.
 *
 * <p>Updates arrive every server tick; {@link #update} shifts each entity's previous→current snapshot
 * and records the wall-clock arrival time and the measured inter-arrival interval. Each frame,
 * {@link #prepareForRender} interpolates position/rotation across that measured interval (rendering
 * one update behind, exactly like a networked entity), pins the age clock to real time for idle
 * animation, and advances {@link LivingEntity#walkAnimation} once per tick from ground speed. Only
 * touched on the client render thread.
 */
public final class RoomPreviewEntities {

    /** Clamp for the measured update interval (ms), so a hitch or burst can't wreck interpolation. */
    private static final long MIN_INTERVAL_MS = 25L;
    private static final long MAX_INTERVAL_MS = 200L;

    private static final Map<String, Map<Integer, Tracked>> BY_ROOM = new HashMap<>();

    private RoomPreviewEntities() {}

    /** Monotonic wall clock in milliseconds — used only for interpolation timing. */
    private static long nowMs() {
        return System.nanoTime() / 1_000_000L;
    }

    /** Applies a fresh snapshot (packet thread). Creates the render instance on first sight. */
    public static void update(String roomCode, RoomEntitySnapshot snapshot) {
        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        final long nowMs = nowMs();
        final Map<Integer, Tracked> perRoom = BY_ROOM.computeIfAbsent(roomCode, k -> new HashMap<>());
        final Tracked tracked = perRoom.get(snapshot.entityId());
        if (tracked == null || tracked.entity.getType() != snapshot.type()) {
            final Entity created = snapshot.type().create(mc.level, new EntitySpawnRequest(EntitySpawnReason.LOAD, true));
            if (created == null) return;
            // A never-added entity has no network id; renderers read getId() (as a model seed), so assign one.
            created.setId(snapshot.entityId());
            perRoom.put(snapshot.entityId(), new Tracked(created, snapshot, nowMs));
        } else {
            tracked.push(snapshot, nowMs);
        }
    }

    /** {@return the interpolated, animated render instance for {@code entityId} in {@code roomCode}} */
    public static @Nullable Entity prepareForRender(String roomCode, int entityId) {
        final Map<Integer, Tracked> perRoom = BY_ROOM.get(roomCode);
        if (perRoom == null) return null;
        final Tracked tracked = perRoom.get(entityId);
        if (tracked == null) return null;

        final Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;
        final long gameTime = mc.level.getGameTime();

        final float f = tracked.factor(nowMs());
        final Vec3 pos = tracked.prevPos.lerp(tracked.curPos, f);
        final float yaw = Mth.rotLerp(f, tracked.prevYaw, tracked.curYaw);
        final float pitch = Mth.lerp(f, tracked.prevPitch, tracked.curPitch);
        final float head = Mth.rotLerp(f, tracked.prevHead, tracked.curHead);

        final Entity entity = tracked.entity;
        entity.setPos(pos.x, pos.y, pos.z);
        entity.setYRot(yaw);
        entity.setXRot(pitch);
        entity.setYHeadRot(head);
        entity.setYBodyRot(yaw);
        // We interpolate ourselves, so freeze the renderer's own lerp to the interpolated value.
        entity.xOld = pos.x;
        entity.yOld = pos.y;
        entity.zOld = pos.z;
        entity.yRotO = yaw;
        entity.xRotO = pitch;
        if (entity instanceof LivingEntity living) {
            living.yHeadRotO = head;
            living.yBodyRotO = yaw;
        }

        // Idle animations: pin the age clock to real time so they play at the correct speed.
        entity.tickCount = (int) gameTime;

        // Limb-swing: advance once per game tick from the entity's per-tick ground speed.
        if (entity instanceof LivingEntity living && gameTime != tracked.lastAnimTick) {
            tracked.lastAnimTick = gameTime;
            final double dx = tracked.curPos.x - tracked.prevPos.x;
            final double dz = tracked.curPos.z - tracked.prevPos.z;
            final float perTick = (float) Math.sqrt(dx * dx + dz * dz);
            living.walkAnimation.update(Math.min(perTick * 4f, 1f), 0.4f, 1f);
        }
        return entity;
    }

    /** Drops cached instances (and rooms) no longer present in the live snapshots. */
    public static void retainCurrent() {
        BY_ROOM.entrySet().removeIf(roomEntry -> {
            final var current = ClientRoomEntities.get(roomEntry.getKey());
            if (current.isEmpty()) return true;
            final Set<Integer> ids = new HashSet<>();
            for (RoomEntitySnapshot s : current) ids.add(s.entityId());
            roomEntry.getValue().keySet().retainAll(ids);
            return roomEntry.getValue().isEmpty();
        });
    }

    public static void clear() {
        BY_ROOM.clear();
    }

    /** One tracked preview entity plus the two snapshots (and their timing) it interpolates between. */
    private static final class Tracked {
        final Entity entity;
        Vec3 prevPos, curPos;
        float prevYaw, curYaw, prevPitch, curPitch, prevHead, curHead;
        long arrivalMs;
        long intervalMs = 50L;
        long lastAnimTick = Long.MIN_VALUE;

        Tracked(Entity entity, RoomEntitySnapshot s, long nowMs) {
            this.entity = entity;
            this.curPos = this.prevPos = new Vec3(s.x(), s.y(), s.z());
            this.curYaw = this.prevYaw = s.yaw();
            this.curPitch = this.prevPitch = s.pitch();
            this.curHead = this.prevHead = s.headYaw();
            this.arrivalMs = nowMs;
        }

        void push(RoomEntitySnapshot s, long nowMs) {
            prevPos = curPos;
            prevYaw = curYaw;
            prevPitch = curPitch;
            prevHead = curHead;
            curPos = new Vec3(s.x(), s.y(), s.z());
            curYaw = s.yaw();
            curPitch = s.pitch();
            curHead = s.headYaw();
            intervalMs = Mth.clamp(nowMs - arrivalMs, MIN_INTERVAL_MS, MAX_INTERVAL_MS);
            arrivalMs = nowMs;
        }

        float factor(long nowMs) {
            return (float) Mth.clamp((double) (nowMs - arrivalMs) / intervalMs, 0.0, 1.0);
        }
    }
}
