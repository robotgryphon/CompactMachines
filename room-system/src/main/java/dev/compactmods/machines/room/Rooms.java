package dev.compactmods.machines.room;

import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.core.data.CMSingletonDataFileManager;
import dev.compactmods.machines.room.block.BreakableWallBlock;
import dev.compactmods.machines.room.block.ItemBlockWall;
import dev.compactmods.machines.room.block.SolidWallBlock;
import dev.compactmods.machines.room.registry.RoomRegistrarData;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.*;

import java.util.UUID;
import java.util.function.Supplier;

public interface Rooms {

    DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(CompactMachinesCore.MOD_ID);
    DeferredRegister.Items ITEMS = DeferredRegister.createItems(CompactMachinesCore.MOD_ID);
    DeferredRegister<MenuType<?>> CONTAINERS = DeferredRegister.create(BuiltInRegistries.MENU, CompactMachinesCore.MOD_ID);
    DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, CompactMachinesCore.MOD_ID);

    interface Blocks {
        ResourceKey<Block> SOLID_WALL_KEY = ResourceKey.create(Registries.BLOCK, CompactMachinesCore.identifier("solid_wall"));
        ResourceKey<Block> WALL_KEY = ResourceKey.create(Registries.BLOCK, CompactMachinesCore.identifier("wall"));

        DeferredBlock<SolidWallBlock> SOLID_WALL = BLOCKS.register("solid_wall", () ->
                new SolidWallBlock(BlockBehaviour.Properties.of()
                        .setId(SOLID_WALL_KEY)
                        .strength(-1.0F, 3600000.8F)
                        .sound(SoundType.METAL)
                        .lightLevel((state) -> 15)));


        DeferredBlock<BreakableWallBlock> BREAKABLE_WALL = BLOCKS.register("wall", () ->
                new BreakableWallBlock(BlockBehaviour.Properties.of()
                        .setId(WALL_KEY)
                        .strength(3.0f, 128.0f)
                        .requiresCorrectToolForDrops()));

        static void prepare() {
        }
    }

    interface Items {
        Supplier<Item.Properties> WALL_ITEM_PROPS = Item.Properties::new;

        DeferredItem<ItemBlockWall> ITEM_SOLID_WALL = ITEMS.register("solid_wall", () ->
                new ItemBlockWall(Blocks.SOLID_WALL.get(), WALL_ITEM_PROPS.get()
                        .setId(ResourceKey.create(Registries.ITEM, CompactMachinesCore.identifier("solid_wall")))));

        DeferredItem<ItemBlockWall> BREAKABLE_WALL = ITEMS.register("wall", () ->
                new ItemBlockWall(Blocks.BREAKABLE_WALL.get(), WALL_ITEM_PROPS.get()
                        .setId(ResourceKey.create(Registries.ITEM, CompactMachinesCore.identifier("wall")))));

        static void prepare() {
        }
    }

    interface DataAttachments {

        Supplier<AttachmentType<UUID>> ROOM_OWNER = ATTACHMENT_TYPES.register("room_owner", () -> AttachmentType
                .builder(() -> Util.NIL_UUID)
                .serialize(UUIDUtil.CODEC.fieldOf("owner"))
                .build());

        Supplier<AttachmentType<CMSingletonDataFileManager<RoomRegistrarData>>> ROOM_REGISTRAR_DATA = ATTACHMENT_TYPES.register("room_registrar_data", () -> AttachmentType
                .builder((server) -> new CMSingletonDataFileManager<>(((MinecraftServer) server), "room_registrations", new RoomRegistrarData()))
                .build());

        static void prepare() {
        }
    }

    static void prepare() {
        Blocks.prepare();
        Items.prepare();
        DataAttachments.prepare();
    }

    static void registerContent(IEventBus modBus) {
        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        CONTAINERS.register(modBus);
        ATTACHMENT_TYPES.register(modBus);
    }

    static void registerEvents(IEventBus modBus) {
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::checkSpawn);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityChangedDimensions);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityJoined);
        NeoForge.EVENT_BUS.addListener(RoomEventHandler::entityTeleport);
    }
}
