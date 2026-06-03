package dev.compactmods.machines.client.machine.render;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.machine.shader.MachineFlags;
import dev.compactmods.machines.client.machine.shader.MachineShaders;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

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

    private static final ContextKey<PRIDE> KEY = new ContextKey<>(CompactMachines.identifier("pride_renderer"));

    public static void afterBlocksRender(ExtractLevelRenderStateEvent e) {

        var customState = new PRIDE();
        e.getLevelRenderer().iterateVisibleBlockEntities(be -> {
            if(be instanceof CompactMachineBlockEntity mbe)
                customState.visibleMachines.add(mbe.getBlockPos());
        });

        e.getRenderState().setRenderData(KEY, customState);
    }

    public static void afterTranslucent(RenderLevelStageEvent.AfterTranslucentBlocks e) {

        final var poseStack = e.getPoseStack();

//        final var poseStack = new PoseStack();
        poseStack.pushPose();
        poseStack.translate(Vec3.ZERO.subtract(e.getLevelRenderState().cameraRenderState.pos));

        final var gameRenderer = Minecraft.getInstance().gameRenderer;
        final var collector = gameRenderer.getSubmitNodeStorage();

//        visibleMachines = List.of(BlockPos.ZERO);

        final var customState = e.getLevelRenderState().getRenderDataOrThrow(KEY);
        for (var pos : customState.visibleMachines) {
            poseStack.pushPose();
            collector.submitCustomGeometry(poseStack, MachineShaders.TYE_DYE_RENDER_TYPE, (pose, buffer) -> {
                pose.translate(pos.getX(), pos.getY(), pos.getZ());
                emitPanes(pose, buffer, 0);
//                emitFace(pose, buffer, Direction.UP, 1f + OUTSET, A, A, B, B);
            });
            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private static class PRIDE {
        List<BlockPos> visibleMachines = new ArrayList<>();
    }
}
