package dev.compactmods.machines.client.machine.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.machine.shader.FlagShader;
import dev.compactmods.machines.client.machine.shader.MachineFlagRenderTypes;
import dev.compactmods.machines.client.machine.shader.MachineShaders;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.concurrent.atomic.AtomicReference;

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
    private static final ContextKey<PrideRenderState> KEY = new ContextKey<>(CompactMachines.identifier("pride_renderer"));

    // named constants for clarity
    private static final Vector4f colorModulator = new Vector4f(1, 1, 1, 1);
    private static final Vector3f worldOffset = new Vector3f(0, 0, 0);
    private static final Matrix4f textureTransform = new Matrix4f();

    /**
     * Size of the {@code FlagPalette} UBO in bytes — std140 layout of:
     * {@code ivec4 paletteMeta; vec4 paletteColors[10];}
     *
     * <p>Cached at class-load so we can pre-size the GPU buffer without
     * recomputing each frame. Mirrors the layout in
     * {@code assets/compactmachines/shaders/pride_stripes.fsh}.</p>
     */
    private static final int PALETTE_UBO_SIZE = new Std140SizeCalculator()
            .putIVec4()
            .align(16)
            // Std140SizeCalculator has no putVec4Array — emulate by putting
            // MAX_STRIPES vec4s. Each is aligned to 16 (their natural size),
            // matching how a `vec4[N]` lays out in std140.
            .putVec4().putVec4().putVec4().putVec4().putVec4()
            .putVec4().putVec4().putVec4().putVec4().putVec4()
            .get();

    /**
     * Lazy-allocated palette UBO. Created on first use, then re-uploaded each
     * frame with the resolved {@link FlagShader} colours.
     */
    private static @Nullable GpuBuffer paletteBuffer;

    public static void extractPrideRenderState(ExtractLevelRenderStateEvent e) {

        var state = e.getRenderState().getRenderDataOrDefault(KEY, PrideRenderState.INSTANCE);

        state.reset();

        positions.clear();
        state.clientLevel = e.getLevel();

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

    public static void afterTranslucent(RenderLevelStageEvent.AfterTranslucentBlocks e) {

        var state = e.getLevelRenderState().getRenderDataOrThrow(KEY);

        if (state.MeshState == null) return;

        var transforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), colorModulator, worldOffset, textureTransform);

        var renderTarget = Minecraft.getInstance().getMainRenderTarget();
        if(e.getLevelRenderer().getTranslucentTarget() != null)
            renderTarget =  e.getLevelRenderer().getTranslucentTarget();

        AtomicReference<RenderPipeline> shader = new AtomicReference<>(MachineShaders.TYE_DYE_PIPELINE);
        final var prideShader = state.clientLevel.registryAccess()
                .lookupOrThrow(FlagShader.REGISTRY_KEY)
                .getRandom(state.clientLevel.getRandom());

        // Whether the pride pipeline is active this frame — gates the palette
        // UBO upload + bind below. Avoids touching the buffer for the tye-dye
        // path, which doesn't declare FlagPalette.
        final boolean[] usePride = { false };
        prideShader.ifPresent(ref -> {
            shader.set(MachineFlagRenderTypes.PRIDE_STRIPES_PIPELINE);
            usePride[0] = true;
            uploadPaletteUbo(ref.value());
        });

        try (var pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                () -> CompactMachinesCore.dotPrefix("machines"),
                renderTarget.getColorTextureView(),
                OptionalInt.empty(),
                renderTarget.getDepthTextureView(),
                OptionalDouble.empty())
        ) {
            pass.setPipeline(shader.get());
            RenderSystem.bindDefaultUniforms(pass);
            pass.setVertexBuffer(0, state.VertexBuffer);
            pass.setIndexBuffer(state.IndexBuffer, state.MeshState.indexType());
//            pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
            pass.setUniform("DynamicTransforms", transforms);
            if (usePride[0] && paletteBuffer != null) {
                pass.setUniform("FlagPalette", paletteBuffer);
            }
            pass.drawIndexed(0, 0, state.MeshState.indexCount(), 1);
        }
    }

    /**
     * Resize-or-reuse the palette UBO and copy {@code shader}'s colours into
     * it. Mirrors the std140 block declared in {@code pride_stripes.fsh} —
     * an {@code ivec4} holding the active stripe count (followed by reserved
     * yzw lanes that dodge std140's 16-byte alignment on the trailing array),
     * then {@code vec4[10]} of colours.
     *
     * <p>Buffer is allocated lazily and reused across frames. The contents are
     * cheap to re-upload (≈176 bytes), so we just write the whole thing every
     * frame rather than tracking dirty state.</p>
     */
    private static void uploadPaletteUbo(FlagShader shader) {
        if (paletteBuffer == null) {
            paletteBuffer = RenderSystem.getDevice().createBuffer(
                    () -> CompactMachinesCore.dotPrefix("flag_palette_ubo"),
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                    PALETTE_UBO_SIZE);
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            Std140Builder b = Std140Builder.onStack(stack, PALETTE_UBO_SIZE)
                    // Active stripe count packed into .x; .yzw reserved
                    .putIVec4(shader.size(), 0, 0, 0);

            // Fill all MAX_STRIPES slots so the buffer write is deterministic —
            // unused tail entries are zeroed (and ignored by the fragment
            // shader, which clamps to paletteMeta.x).
            for (int i = 0; i < FlagShader.MAX_STRIPES; i++) {
                if (i < shader.size()) {
                    b.putVec4(shader.r(i), shader.g(i), shader.b(i), 0f);
                } else {
                    b.putVec4(0f, 0f, 0f, 0f);
                }
            }

            ByteBuffer payload = b.get();
            RenderSystem.getDevice().createCommandEncoder()
                    .writeToBuffer(paletteBuffer.slice(), payload);
        }
    }

    private static class PrideRenderState {
        public static final PrideRenderState INSTANCE = new PrideRenderState();

        public ClientLevel clientLevel;
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
