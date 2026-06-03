package dev.compactmods.machines.client.machine.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.machine.shader.MachineFlags;
import dev.compactmods.machines.client.machine.shader.MachineShaders;
import dev.compactmods.machines.machine.Machines;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

import static dev.compactmods.machines.client.machine.render.CompactMachineRenderer.emitFace;
import static dev.compactmods.machines.client.machine.render.CompactMachineRenderer.emitPanes;

public class MachineShaderRenderer {

    /**
     * Tiny outward offset so the overlay always wins the depth test against the panes.
     */
    private static final float OUTSET = 0.002f;
    /**
     * Pane geometry in block-local space (matches the model — 14×14 inset by 1px).
     */
    private static final float A = 1f / 16f;
    private static final float B = 15f / 16f;

    private static List<BlockPos> visibleMachines = new ArrayList<>();

    public static void afterBlocksRender(RenderLevelStageEvent.AfterOpaqueBlocks e) {
        visibleMachines = e.getLevelRenderState().blockEntityRenderStates
                .stream()
                .filter(be -> be.blockEntityType.equals(Machines.BlockEntities.MACHINE.get()))
                .map(s -> s.blockPos.immutable())
                .toList();
    }

    public static void afterTranslucent(RenderLevelStageEvent.AfterOpaqueBlocks e) {

        final var poseStack = e.getPoseStack();

//        final var poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(Vec3.ZERO.subtract(e.getLevelRenderState().cameraRenderState.pos));

        final var gameRenderer = Minecraft.getInstance().gameRenderer;
        final var collector = gameRenderer.getSubmitNodeStorage();

//        visibleMachines = List.of(BlockPos.ZERO);

        for (var pos : visibleMachines) {
            poseStack.pushPose();
            collector.submitCustomGeometry(poseStack, MachineShaders.renderTypeFor(MachineFlags.BAKER_PRIDE.id()), (pose, buffer) -> {
                pose.translate(pos.getX(), pos.getY(), pos.getZ());
                emitPanes(pose, buffer, 0);
//                emitFace(pose, buffer, Direction.UP, 1f + OUTSET, A, A, B, B);
            });
            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
