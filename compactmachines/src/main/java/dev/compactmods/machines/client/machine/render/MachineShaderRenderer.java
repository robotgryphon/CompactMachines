package dev.compactmods.machines.client.machine.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.machine.shader.MachineShaders;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.Minecraft;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

import java.util.OptionalDouble;
import java.util.OptionalInt;

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

    private static final LongList positions = new LongArrayList();
    private static final ContextKey<PRIDE> KEY = new ContextKey<>(CompactMachines.identifier("pride_renderer"));

    public static void extractPrideRenderState(ExtractLevelRenderStateEvent e) {

        var state = e.getRenderState().getRenderDataOrDefault(KEY, PRIDE.INSTANCE);

        state.reset();

        positions.clear();
        // iterate positions of BEs
        e.getLevelRenderer().iterateVisibleBlockEntities(be -> {
            if(be instanceof CompactMachineBlockEntity mbe)
                positions.add(mbe.getBlockPos().asLong());
        });

        final var poseStack = new PoseStack();
        try (var mesh = MachineMeshHelper.buildMesh(poseStack, positions, e.getFrustum())) {
            if (mesh != null) {
                state.MeshState = mesh.drawState();
                state.VertexBuffer = VerticesHelper.uploadVertices(state.VertexBuffer, mesh, () -> CompactMachinesCore.dotPrefix("machine_vertices"));
                state.IndexBuffer = VerticesHelper.uploadIndices(state.IndexBuffer, mesh, () -> CompactMachinesCore.dotPrefix("machine_vertices"));
            }
        }

        e.getRenderState().setRenderData(KEY, state);
    }

    // named constants for clarity
    private static final Vector4f colorModulator = new Vector4f(1, 1, 1, 1);
    private static final Vector3f worldOffset = new Vector3f(0, 0, 0);
    private static final Matrix4f textureTransform = new Matrix4f();

    public static void afterTranslucent(RenderLevelStageEvent.AfterTranslucentBlocks e) {

        var state = e.getLevelRenderState().getRenderDataOrThrow(KEY);

        if (state.MeshState == null) return;

        var transforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), colorModulator, worldOffset, textureTransform);

        var renderTarget = Minecraft.getInstance().getMainRenderTarget();
        if(e.getLevelRenderer().getTranslucentTarget() != null)
            renderTarget =  e.getLevelRenderer().getTranslucentTarget();

        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> CompactMachinesCore.dotPrefix("machines"),
                renderTarget.getColorTextureView(),
                OptionalInt.empty(),
                renderTarget.getDepthTextureView(),
                OptionalDouble.empty())
        ) {
            pass.setPipeline(MachineShaders.TYE_DYE_PIPELINE);
            RenderSystem.bindDefaultUniforms(pass);
            pass.setVertexBuffer(0, state.VertexBuffer);
            pass.setIndexBuffer(state.IndexBuffer, state.MeshState.indexType());
//            pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
            pass.setUniform("DynamicTransforms", transforms);
            pass.drawIndexed(0, 0, state.MeshState.indexCount(), 1);
        }
    }

    private static class PRIDE {
        public static final PRIDE INSTANCE = new PRIDE();

        public MeshData.@Nullable DrawState MeshState;
        public @Nullable GpuBuffer VertexBuffer;
        public @Nullable GpuBuffer IndexBuffer;

        public void reset() {
            MeshState = null;
            if (VertexBuffer != null) {
                VertexBuffer.close();
                VertexBuffer = null;
            }

            if (IndexBuffer != null) {
                IndexBuffer.close();
                IndexBuffer = null;
            }
        }
    }


}
