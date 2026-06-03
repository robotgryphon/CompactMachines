package dev.compactmods.machines.client.machine.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.compactmods.machines.client.machine.shader.MachineShaderResolver;
import dev.compactmods.machines.client.machine.shader.MachineShaders;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CompactMachineRenderer implements BlockEntityRenderer<CompactMachineBlockEntity, MachineRenderState> {

    /**
     * Tiny outward offset so the overlay always wins the depth test against the panes.
     */
    private static final float OUTSET = 0.001f;
    /**
     * Pane geometry in block-local space (matches the model — 14×14 inset by 1px).
     */
    private static final float A = 1f / 16f;
    private static final float B = 15f / 16f;

    public CompactMachineRenderer(BlockEntityRendererProvider.Context ctx) {
        // No resources captured — everything we need lives on the render state.
    }

    @Override
    public MachineRenderState createRenderState() {
        return new MachineRenderState();
    }

    @Override
    public void extractRenderState(CompactMachineBlockEntity be, MachineRenderState state,
                                   float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        state.shaderId = MachineShaderResolver.resolve(be).orElse(null);
        // gameTime stays on the state for future shaders that might want a frame-
        // stamped tick value; the current pride shader reads the Globals UBO directly.
        state.gameTime = be.getLevel() != null ? be.getLevel().getGameTime() + partialTick : 0f;
        state.neighborMachineMask = computeNeighborMask(be.getLevel(), be.getBlockPos());
    }

    /**
     * Look at each of the six neighbour blocks. Bit set ⇒ neighbour is another
     * compact machine, so the BER should skip its overlay quad on that face
     */
    private static int computeNeighborMask(@Nullable Level level, BlockPos pos) {
        if (level == null) return 0;
        int mask = 0;
        var machineBlock = Machines.Blocks.MACHINE.get();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            cursor.setWithOffset(pos, dir);
            if (level.getBlockState(cursor).is(machineBlock)) {
                mask |= 1 << dir.get3DDataValue();
            }
        }
        return mask;
    }

    @Override
    public void submit(MachineRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState camera) {
        if (state.shaderId == null) return;
        var renderType = MachineShaders.renderTypeFor(state.shaderId);
        if (renderType == null) return;

        final int skipMask = state.neighborMachineMask;

//        collector.submitCustomGeometry(poseStack, renderType,
//                (pose, buffer) -> {
//                    emitPanes(pose, buffer, skipMask);
//                });

//        collector.submitCustomGeometry(poseStack, renderType, (pose, buffer) -> {
//            pose.translate(0, 5, 0);
//            emitFace(pose, buffer, Direction.EAST, 1f + OUTSET, A, A, B, B);
//        });
    }

    // --- geometry --------------------------------------------------------------------

    /**
     * Emit one quad per face, slightly outside each glass pane. Per-vertex
     * {@code Color.rgb} carries the block-local XYZ for the shader (packed as
     * three bytes in [0,255]); {@code Color.a} is full opacity (the blend
     * opacity is set by the fragment shader, not the vertex alpha);
     * {@code Normal} carries the outward face direction.
     */
    public static void emitPanes(PoseStack.Pose pose, VertexConsumer buffer, int skipMask) {
        // Side panes: each at the face's outer plane, 14×14 area between frame bars.
        // Sentinel for the "outside" axis: 0 - OUTSET on the negative face, 1 + OUTSET on the positive.
        if (!isSkipped(skipMask, Direction.NORTH))
            emitFace(pose, buffer, Direction.NORTH, -OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.SOUTH))
            emitFace(pose, buffer, Direction.SOUTH, 1f + OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.WEST))
            emitFace(pose, buffer, Direction.WEST, -OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.EAST))
            emitFace(pose, buffer, Direction.EAST, 1f + OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.UP))
            emitFace(pose, buffer, Direction.UP, 1f + OUTSET, A, A, B, B);
        if (!isSkipped(skipMask, Direction.DOWN))
            emitFace(pose, buffer, Direction.DOWN, -OUTSET, A, A, B, B);
    }

    private static boolean isSkipped(int mask, Direction dir) {
        return (mask & (1 << dir.get3DDataValue())) != 0;
    }

    /**
     * Emit one quad for the named face. {@code plane} is the coordinate along the
     * face's normal axis (slightly outside the pane). The {@code u1..v2} are the
     * in-plane extents — they line up with the inner 14×14 window between the
     * frame bars.
     */
    public static void emitFace(PoseStack.Pose pose, VertexConsumer buf,
                                Direction face, float plane,
                                float u1, float v1, float u2, float v2) {
        float nx = face.getStepX();
        float ny = face.getStepY();
        float nz = face.getStepZ();

        // For each face we pick the in-plane axes so the quad winds counter-clockwise
        // when viewed from outside the cube (correct facing for backface culling).
        switch (face) {
            case NORTH -> { // outward = -Z, plane is along Z; u=X, v=Y
                vertex(pose, buf, u2, v1, plane, nx, ny, nz);
                vertex(pose, buf, u1, v1, plane, nx, ny, nz);
                vertex(pose, buf, u1, v2, plane, nx, ny, nz);
                vertex(pose, buf, u2, v2, plane, nx, ny, nz);
            }
            case SOUTH -> { // outward = +Z, plane is along Z; u=X, v=Y (reverse wind)
                vertex(pose, buf, u1, v1, plane, nx, ny, nz);
                vertex(pose, buf, u2, v1, plane, nx, ny, nz);
                vertex(pose, buf, u2, v2, plane, nx, ny, nz);
                vertex(pose, buf, u1, v2, plane, nx, ny, nz);
            }
            case WEST -> {  // outward = -X, plane is along X; u=Z, v=Y
                vertex(pose, buf, plane, v1, u1, nx, ny, nz);
                vertex(pose, buf, plane, v1, u2, nx, ny, nz);
                vertex(pose, buf, plane, v2, u2, nx, ny, nz);
                vertex(pose, buf, plane, v2, u1, nx, ny, nz);
            }
            case EAST -> {  // outward = +X, plane is along X; u=Z, v=Y (reverse wind)
                vertex(pose, buf, plane, v1, u2, nx, ny, nz);
                vertex(pose, buf, plane, v1, u1, nx, ny, nz);
                vertex(pose, buf, plane, v2, u1, nx, ny, nz);
                vertex(pose, buf, plane, v2, u2, nx, ny, nz);
            }
            case UP -> {    // outward = +Y, plane is along Y; u=X, v=Z
                // Verified via (v2-v1)×(v3-v2): this ordering produces normal (0, +1, 0).
                vertex(pose, buf, u1, plane, v1, nx, ny, nz);
                vertex(pose, buf, u1, plane, v2, nx, ny, nz);
                vertex(pose, buf, u2, plane, v2, nx, ny, nz);
                vertex(pose, buf, u2, plane, v1, nx, ny, nz);
            }
            case DOWN -> {  // outward = -Y, plane is along Y; u=X, v=Z
                // Same vertex set, opposite winding — cross product gives (0, -1, 0).
                vertex(pose, buf, u1, plane, v1, nx, ny, nz);
                vertex(pose, buf, u2, plane, v1, nx, ny, nz);
                vertex(pose, buf, u2, plane, v2, nx, ny, nz);
                vertex(pose, buf, u1, plane, v2, nx, ny, nz);
            }
        }
    }

    /**
     * Emit a single vertex. See {@link #emitPanes} for the format conventions.
     */
    private static void vertex(PoseStack.Pose pose, VertexConsumer buf,
                               float x, float y, float z,
                               float nx, float ny, float nz) {
        int r = clampByte(Math.round(clamp01(x) * 255f));
        int g = clampByte(Math.round(clamp01(y) * 255f));
        int b = clampByte(Math.round(clamp01(z) * 255f));
        buf.addVertex(pose, x, y, z)
                .setColor(r, g, b, 255)
                .setNormal(nx, ny, nz);

    }

    private static float clamp01(float v) {
        return v < 0 ? 0 : (v > 1 ? 1 : v);
    }

    private static int clampByte(int v) {
        return v < 0 ? 0 : (v > 255 ? 255 : v);
    }
}
