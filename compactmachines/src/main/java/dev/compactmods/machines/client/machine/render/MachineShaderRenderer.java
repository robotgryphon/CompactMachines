package dev.compactmods.machines.client.machine.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.compactmods.machines.CMDataComponents;
import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.config.ClientConfig;
import dev.compactmods.machines.client.machine.shader.flag.FlagShader;
import dev.compactmods.machines.client.machine.shader.flag.FlagShaders;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.machine.block.CompactMachineBlockEntity;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.context.ContextKey;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.time.Month;
import java.util.*;
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

    private static final ContextKey<PrideRenderState> KEY = new ContextKey<>(CompactMachines.identifier("pride_renderer"));

    // named constants for clarity
    private static final Vector4f colorModulator = new Vector4f(1, 1, 1, 1);
    private static final Vector3f worldOffset = new Vector3f(0, 0, 0);
    private static final Matrix4f textureTransform = new Matrix4f();

    private static final Identifier DEFAULT_FLAG = CompactMachines.identifier("pride/baker");

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
        if (state.meshes == null || state.meshes.isEmpty())
            state.meshes = new Object2ObjectOpenHashMap<>();

        for (var mesh : state.meshes.values())
            mesh.reset();

        state.clientLevel = e.getLevel();
        final var shadersTmp = new Object2ObjectOpenHashMap<Identifier, LongList>();

        final var shaders = state.clientLevel.registryAccess()
                .lookupOrThrow(FlagShader.REGISTRY_KEY);

        shaders.registryKeySet()
                .forEach(rk -> shadersTmp.put(rk.identifier(), new LongArrayList()));

        var defaultFlag = Identifier.tryParse(ClientConfig.DEFAULT_PRIDE_FLAG.get());
        if(defaultFlag == null)
            defaultFlag = shaders.getAny().map(r -> r.key().identifier()).orElse(DEFAULT_FLAG);

        // iterate positions of BEs
        Identifier finalDefaultFlag = defaultFlag;
        e.getLevelRenderer().iterateVisibleBlockEntities(be -> {
            if (be instanceof CompactMachineBlockEntity mbe) {
                final var core = mbe.coreHandler().getResource(0);

                // If we have a core, try to pull the shader from the core item
                if (!core.isEmpty()) {
                    final var flag = core.getOrDefault(CMDataComponents.PRIDE_FLAG, finalDefaultFlag);
                    if(shadersTmp.containsKey(flag)) {
                        final var list = shadersTmp.get(flag);
                        if (list != null)
                            list.add(mbe.getBlockPos().asLong());
                    }
                } else {
                    // If we DO NOT have a core, and it's Pride month...
                    if(ClientConfig.ENABLE_PRIDE.isFalse() || LocalDate.now().getMonth() != Month.JUNE)
                        return;

                    if(shadersTmp.containsKey(finalDefaultFlag)) {
                        final var list = shadersTmp.get(finalDefaultFlag);
                        if (list != null)
                            list.add(mbe.getBlockPos().asLong());
                    }
                }
            }
        });

        state.shaders = shadersTmp;

        final var poseStack = new PoseStack();
        for (var kvp : state.shaders.entrySet()) {
            try (var mesh = MachineMeshHelper.buildMesh(poseStack, kvp.getValue(), e.getFrustum())) {
                if (mesh != null) {
                    final var mesh2 = new PrideRenderState.Mesh();
                    mesh2.MeshState = mesh.drawState();
                    mesh2.VertexBuffer = VerticesHelper.uploadVertices(mesh2.VertexBuffer, mesh, () -> CompactMachinesCore.dotPrefix("machine_vertices"));
                    mesh2.IndexBuffer = VerticesHelper.uploadIndices(mesh2.IndexBuffer, mesh, () -> CompactMachinesCore.dotPrefix("machine_vertices"));
                    state.meshes.put(kvp.getKey(), mesh2);
                }
            }
        }

        e.getRenderState().setRenderData(KEY, state);
    }

    public static void afterTranslucent(RenderLevelStageEvent.AfterTranslucentBlocks e) {

        var state = e.getLevelRenderState().getRenderData(KEY);

        if (state == null || state.meshes.isEmpty())
            return;

        var transforms = RenderSystem.getDynamicUniforms().writeTransform(RenderSystem.getModelViewMatrix(), colorModulator, worldOffset, textureTransform);

        var renderTarget = Minecraft.getInstance().getMainRenderTarget();
        if (e.getLevelRenderer().getTranslucentTarget() != null)
            renderTarget = e.getLevelRenderer().getTranslucentTarget();

        AtomicReference<RenderPipeline> shader = new AtomicReference<>(FlagShaders.PRIDE_STRIPES_PIPELINE);

        for (var shaderId : state.shaders.keySet()) {
            var mesh = state.meshes.get(shaderId);
            if (mesh == null)
                continue;

            if(mesh.MeshState == null || mesh.VertexBuffer == null || mesh.IndexBuffer == null)
                continue;

            final var registry = state.clientLevel.registryAccess()
                    .lookupOrThrow(FlagShader.REGISTRY_KEY);

            final var prideShader = registry.getOptional(shaderId);

            // Whether the pride pipeline is active this frame — gates the palette
            // UBO upload + bind below. Avoids touching the buffer for the tye-dye
            // path, which doesn't declare FlagPalette.
            final boolean[] usePride = {false};
            prideShader.ifPresent(ref -> {
                shader.set(FlagShaders.PRIDE_STRIPES_PIPELINE);
                usePride[0] = true;
                uploadPaletteUbo(ref);
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
                pass.setVertexBuffer(0, mesh.VertexBuffer);
                pass.setIndexBuffer(mesh.IndexBuffer, mesh.MeshState.indexType());
//            pass.bindTexture("Sampler0", texture.getTextureView(), texture.getSampler());
                pass.setUniform("DynamicTransforms", transforms);
                if (usePride[0] && paletteBuffer != null) {
                    pass.setUniform("FlagPalette", paletteBuffer);
                }
                pass.drawIndexed(0, 0, mesh.MeshState.indexCount(), 1);
            }
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
                    final var color = shader.colors().get(i);
                    b.putVec4(ARGB.redFloat(color), ARGB.greenFloat(color), ARGB.blueFloat(color), 0f);
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

        public Map<Identifier, LongList> shaders;
        public Map<Identifier, Mesh> meshes;

        static class Mesh {
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


}
