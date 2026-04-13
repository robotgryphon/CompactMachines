package dev.compactmods.machines.room;

import dev.compactmods.machines.api.room.generation.NewRoomBuilder;
import dev.compactmods.machines.api.room.generation.RoomGenerationDetails;
import dev.compactmods.machines.api.room.spatial.RoomBoundaries;
import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.core.machine.MachineColor;
import net.minecraft.core.Holder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class ServerNewRoomBuilder implements NewRoomBuilder {
    private final String code;
    private MachineColor color = MachineColor.DEFAULT;

    private Holder<RoomTemplate> template;
    private AABB boundaries = AABB.ofSize(Vec3.ZERO, 1, 1, 1);
    UUID owner;

    public ServerNewRoomBuilder() {
        this.code = RoomCodeGenerator.generateRoomId();
    }

    public ServerNewRoomBuilder boundaries(AABB boundaries) {
        this.boundaries = boundaries;
        return this;
    }

    public ServerNewRoomBuilder offsetCenter(Vec3 offset) {
        this.boundaries = this.boundaries.move(offset);
        return this;
    }

    @Override
    public NewRoomBuilder template(Holder<RoomTemplate> template) {
        this.template = template;
        return this;
    }

    @Override
    public NewRoomBuilder template(RoomTemplate template) {
        this.template = Holder.direct(template);
        return this;
    }

    public ServerNewRoomBuilder owner(UUID owner) {
        this.owner = owner;
        return this;
    }

    @Override
    public RoomGenerationDetails build() {
        final var bounds = new RoomBoundaries(boundaries);
        return new RoomGenerationDetails(template, bounds, owner);
    }
}
