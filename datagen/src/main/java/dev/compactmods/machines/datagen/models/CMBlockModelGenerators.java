package dev.compactmods.machines.datagen.models;

import dev.compactmods.machines.api.CompactMachines;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CMBlockModelGenerators extends BlockModelGenerators {

    public CMBlockModelGenerators(Consumer<BlockModelDefinitionGenerator> p_387996_, ItemModelOutput p_387053_, BiConsumer<Identifier, ModelInstance> p_387066_) {
        super(p_387996_, p_387053_, p_387066_);
    }

    @Override
    public void run() {
    }

    public Identifier createSimpleWithTextures(Holder<Block> block, ModelTemplate template, TextureMapping mapping) {
        final var b = block.value();
        final var id = template.create(b, mapping, this.modelOutput);
        this.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(b, BlockModelGenerators.plainVariant(id)));
        return id;
    }

    public Identifier createCompactMachine(Holder<Block> block) {
        final var textures = new TextureMapping()
                .put(TextureSlot.PARTICLE, new Material(CompactMachines.identifier("block/machine/tiny")))
                .put(CMModelTemplates.BORDER_SLOT, new Material(CompactMachines.identifier("block/machine/border")))
                .put(CMModelTemplates.OVERLAY_SLOT, new Material(CompactMachines.identifier("block/machine/overlay")))
                .put(CMModelTemplates.TINT_SLOT, new Material(CompactMachines.identifier("block/machine/tint")));

        return createSimpleWithTextures(block, CMModelTemplates.MACHINE_TEMPLATE, textures);
    }
}
