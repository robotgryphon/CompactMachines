package dev.compactmods.machines.datagen;

import dev.compactmods.machines.api.core.Constants;
import dev.compactmods.machines.datagen.lang.EnglishLangGenerator;
import dev.compactmods.machines.datagen.lang.RussianLangGenerator;
import dev.compactmods.machines.datagen.room.RoomTemplates;
import dev.compactmods.machines.datagen.tags.BlockTagGenerator;
import dev.compactmods.machines.datagen.tags.ItemTagGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber(modid = Constants.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class DataGeneration {

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        final var helper = event.getExistingFileHelper();
        final var generator = event.getGenerator();

        final var packOut = generator.getPackOutput();
        final var holderLookup = event.getLookupProvider();

        // Server
        boolean server = event.includeServer();
        generator.addProvider(server, new LevelBiomeGenerator(generator));
        generator.addProvider(server, (DataProvider.Factory<LootTableProvider>) output -> new LootTableProvider(output,
                Collections.emptySet(),
                List.of(new LootTableProvider.SubProviderEntry(BlockLootGenerator::new, LootContextParamSets.BLOCK))
        ));

        generator.addProvider(server, new RecipeGenerator(packOut, holderLookup));

        final var blocks = new BlockTagGenerator(packOut, helper, holderLookup);
        generator.addProvider(server, blocks);
        generator.addProvider(server, new ItemTagGenerator(packOut, blocks, holderLookup));

        // generator.addProvider(server, new PointOfInterestTagGenerator(packOut, holderLookup, helper));

        RoomTemplates.make(event);

        // Client
        boolean client = event.includeClient();
        generator.addProvider(client, new StateGenerator(packOut, helper));
        // generator.addProvider(client, new TunnelWallStateGenerator(packOut, helper));
        generator.addProvider(client, new ItemModelGenerator(packOut, helper));

        generator.addProvider(client, new EnglishLangGenerator(generator));
        generator.addProvider(client, new RussianLangGenerator(generator));
    }
}
