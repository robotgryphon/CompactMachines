package dev.compactmods.machines.neoforge.command.argument;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import dev.compactmods.compactmachines.api.room.Rooms;
import dev.compactmods.compactmachines.api.room.registration.IRoomRegistration;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;

public class Suggestors {

    // FIXME
//    public static final SuggestionProvider<CommandSourceStack> ROOM_UPGRADES = (ctx, builder) ->
//            SharedSuggestionProvider.suggestResource(MachineRoomUpgrades.REGISTRY.keySet(), builder);

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
}
