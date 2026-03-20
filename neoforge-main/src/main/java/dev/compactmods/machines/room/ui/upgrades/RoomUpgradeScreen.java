package dev.compactmods.machines.room.ui.upgrades;

import dev.compactmods.machines.api.CompactMachines;
import dev.compactmods.machines.client.render.ConditionalGhostSlot;
import dev.compactmods.machines.client.render.NineSliceRenderer;
import dev.compactmods.machines.client.widget.ImageButtonBuilder;
import dev.compactmods.machines.network.room.PlayerRequestedRoomUIPacket;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RoomUpgradeScreen extends AbstractContainerScreen<RoomUpgradeMenu> {
    private final Inventory inventory;

    private static Identifier CONTAINER_BACKGROUND = CompactMachines.identifier("textures/gui/psd_screen_9slice.png");

    WidgetSprites BACK_BTN_SPRITES = new WidgetSprites(
        Identifier.withDefaultNamespace("recipe_book/page_backward"),
        Identifier.withDefaultNamespace("recipe_book/page_backward_highlighted")
    );

    private final NineSliceRenderer backgroundRenderer;

    public RoomUpgradeScreen(RoomUpgradeMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title, 256, 152);
        this.inventory = playerInv;
        this.titleLabelY = 6;
        this.inventoryLabelY = 26 + 32;

        this.backgroundRenderer = NineSliceRenderer.builder(CompactMachines.identifier("textures/gui/psd_screen_9slice.png"))
                .area(0, 0, imageWidth, imageHeight)
                .uv(32, 32)
                .sliceSize(4, 4)
                .textureSize(32, 32)
                .build();
    }

    @Override
    protected void init() {
        super.init();

        if(menu.showBackButton) {
            var backButton = ImageButtonBuilder.button(BACK_BTN_SPRITES)
                .location(leftPos - 12, topPos + 2)
                .size(8, 12)
                .message(Component.literal("Close"))
                .onPress(button -> {
                    ClientPacketDistributor.sendToServer(new PlayerRequestedRoomUIPacket(menu.roomCode));
                })
                .build();

            addRenderableWidget(backButton);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        final int white = DyeColor.WHITE.getTextColor();
        graphics.text(this.font, Component.literal("Room Upgrades"), this.titleLabelX, this.titleLabelY, white, false);
        graphics.text(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, white, false);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        final var pose = graphics.pose();

        pose.pushMatrix();
        pose.translate(leftPos, topPos);
        backgroundRenderer.render(graphics);
        pose.popMatrix();

        pose.pushMatrix();
        pose.translate(leftPos, topPos);
        for(var i : this.menu.slots) {
            graphics.fill(i.x, i.y, i.x + 16, i.y + 16, 0x0F000000);
        }
        pose.popMatrix();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        extractTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderSlotContents(GuiGraphicsExtractor guiGraphics, ItemStack itemstack, Slot slot, @Nullable String countString) {
        if (slot instanceof ConditionalGhostSlot cgs && cgs.matched(itemstack)) {
            renderGhostSlot(guiGraphics, itemstack, slot, countString);
            return;
        }

        super.renderSlotContents(guiGraphics, itemstack, slot, countString);
    }

    private void renderGhostSlot(@NotNull GuiGraphicsExtractor graphics, @NotNull ItemStack itemstack, @NotNull Slot slot, @Nullable String countString) {
        graphics.item(slot.getItem(), slot.x, slot.y);
        graphics.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, ARGB.color(150, 30, 70, 210));

        if (!itemstack.isEmpty()) {
            if (itemstack.getCount() != 1 || countString != null) {
                String s = countString == null ? String.valueOf(itemstack.getCount()) : countString;
                graphics.text(this.font, s, slot.x + 19 - 2 - this.font.width(s), slot.y + 6 + 3,
                        ARGB.color(120, 255, 255, 255), false);
            }
        }
    }
}
