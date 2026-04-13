package dev.compactmods.machines.core.client.render;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.network.chat.Component;

public class CMPlayerFaceRenderer {

    public static void render(GameProfile profile, GuiGraphicsExtractor graphics, int x, int y, int size) {
        final var skins = Minecraft.getInstance().getSkinManager();

        // TODO: Cache?
        final var playerSkin = skins.createLookup(profile, false).get();
        PlayerFaceExtractor.extractRenderState(graphics, playerSkin, x, y, size);
    }
}
