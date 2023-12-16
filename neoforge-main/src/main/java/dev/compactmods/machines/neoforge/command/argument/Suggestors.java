package dev.compactmods.machines.neoforge.command.argument;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.compactmachines.api.room.registration.IRoomRegistration;
import dev.compactmods.machines.api.room.upgrade.RoomUpgrade;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class Suggestors {
    public static final SuggestionProvider<CommandSourceStack> ROOM_TEMPLATES = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(getRegistryValues(ctx, Rooms.TEMPLATE_REG_KEY), builder);

    public static final SuggestionProvider<CommandSourceStack> ROOM_UPGRADES = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(getRegistryValues(ctx, RoomUpgrade.REG_KEY), builder);

    public static final SuggestionProvider<CommandSourceStack> OWNED_ROOM_CODES = (ctx, builder) -> {
        final var owner = ctx.getSource().getPlayerOrException();

        final var codes = Rooms.owners()
                .findByOwner(owner.getUUID())
                .map(IRoomRegistration::code);

        return SharedSuggestionProvider.suggest(codes, builder);
    };

    public static final SuggestionProvider<CommandSourceStack> ROOM_CODES = (ctx, builder) -> {
        final var codes = Rooms.registrar()
                .allRooms()
                .map(IRoomRegistration::code);

        return SharedSuggestionProvider.suggest(codes, builder);
    };

    private static <T> Set<ResourceLocation> getRegistryValues(CommandContext<CommandSourceStack> ctx, ResourceKey<Registry<T>> keyType) {
        return ctx.getSource().getServer().registryAccess()
                .registryOrThrow(keyType)
                .keySet();
    }
}
