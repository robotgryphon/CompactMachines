package dev.compactmods.machines.client.keybinds.room;

import com.mojang.blaze3d.platform.InputConstants;
import dev.compactmods.machines.core.CompactMachinesCore;
import dev.compactmods.machines.CMDataAttachments;
import dev.compactmods.machines.api.dimension.CompactDimension;
import dev.compactmods.machines.client.room.RoomKeyMappings;
import dev.compactmods.machines.room.CMFeatureFlags;
import dev.compactmods.machines.network.room.PlayerRequestedUpgradeUIPacket;
import dev.compactmods.machines.shrinking.Shrinking;
import net.minecraft.util.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;

public class RoomUpgradeUIMapping {

   public static final String NAME = Util.makeDescriptionId("key.mapping", CompactMachinesCore.identifier("open_upgrade_screen"));

   public static final IKeyConflictContext CONFLICT_CONTEXT = new IKeyConflictContext() {
	  @Override
	  public boolean isActive() {
		 final var level = Minecraft.getInstance().level;
		 return level != null && level.dimension().equals(CompactDimension.LEVEL_KEY);
	  }

	  @Override
	  public boolean conflicts(IKeyConflictContext other) {
		 return this == other;
	  }
   };

   public static final KeyMapping MAPPING = new KeyMapping(NAME, CONFLICT_CONTEXT, InputConstants.UNKNOWN, RoomKeyMappings.CATEGORY);

   public static void handle() {
	  final var level = Minecraft.getInstance().level;
	  final var player = Minecraft.getInstance().player;
	  if (player != null && level != null && level.dimension().equals(CompactDimension.LEVEL_KEY)) {
		  if(CMFeatureFlags.ROOM_UPGRADES.isSubsetOf(level.enabledFeatures())) {
			  player.getExistingData(Shrinking.CURRENT_ROOM_CODE).ifPresent(currentRoom -> {
                  ClientPacketDistributor.sendToServer(new PlayerRequestedUpgradeUIPacket(currentRoom, true));
			  });
		  } else {
			  player.sendOverlayMessage(Component.literal("You must enable room components for this keybind!"));
		  }
	  }
   }
}
