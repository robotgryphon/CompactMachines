package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.api.room.template.RoomTemplateHelper;
import dev.compactmods.machines.machine.i18n.MachineTranslations;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.compactmods.machines.room.Rooms;
import dev.compactmods.machines.machine.util.SlotRangeUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;

import java.util.Objects;

public class MachineUI extends AbstractContainerScreen<MachineUIMenu> {

    public MachineUI(MachineUIMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

        final var pose = graphics.pose();

        pose.pushMatrix();
        pose.translate(leftPos, topPos);

        graphics.fill(0, 0, this.imageWidth, this.imageHeight, ARGB.color(0.5f, CommonColors.BLACK));
        graphics.outline(0, 0, this.imageWidth, this.imageHeight, CommonColors.GRAY);

        for (var i : this.menu.slots) {
            graphics.outline(i.x, i.y, 16, 16, ARGB.color(0.3f, CommonColors.WHITE));
            graphics.fill(i.x, i.y, i.x + 16, i.y + 16, ARGB.color(0.1f, CommonColors.WHITE));
        }
        pose.popMatrix();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        final var pose = graphics.pose();

        pose.pushMatrix();
        pose.translate(leftPos, topPos);

        graphics.pose().pushMatrix();
        pose.translate(32, 10);

        graphics.pose().scale(0.7f, 0.7f);

        final var coreSlot = this.menu.getCoreSlot();
        if (coreSlot != null && coreSlot.hasItem()) {
            final var item = coreSlot.getItem();
            graphics.text(minecraft.font,
                    item.getOrDefault(DataComponents.CUSTOM_NAME, item.getItemName()),
                    0, 0, CommonColors.YELLOW, true);

            menu.currentRoom().ifPresent(boundRoom -> {
                graphics.text(minecraft.font,
                        Component.literal("Bound To: " + boundRoom),
                        0, 14, CommonColors.WHITE, true);
            });

            menu.currentTemplate()
                    .ifPresent(template -> renderTemplateDetails(graphics, template));
        }
        graphics.pose().popMatrix();

        graphics.nextStratum();

        for (var i : this.menu.slots) {
            if (SlotRangeUtil.PLAYER_INV.slots().contains(i.index)) {
                if (this.menu.getCarried().isEmpty()) {
                    if (!i.hasItem() || !(i.getItem().has(Rooms.DataComponents.BOUND_ROOM_CODE) || i.getItem().has(Rooms.DataComponents.ROOM_TEMPLATE_ID)))
                        graphics.fill(i.x, i.y, i.x + 16, i.y + 16, ARGB.color(0.5f, CommonColors.BLACK));
                    else
                        graphics.outline(i.x, i.y, 16, 16, ARGB.color(0.2f, CommonColors.GREEN));
                }
            }
        }

        pose.popMatrix();
    }

    private void renderTemplateDetails(GuiGraphicsExtractor graphics, RoomTemplate template) {
        final var key = minecraft.level.registryAccess()
                .lookupOrThrow(RoomTemplate.REGISTRY_KEY)
                .getKey(template);

        graphics.text(minecraft.font,
                Component.literal("Template: " + key),
                0, 14, CommonColors.WHITE, true);

        graphics.text(minecraft.font,
                Component.translatable(MachineTranslations.IDs.SIZE, template.internalDimensions().toString()),
                0, 28, CommonColors.WHITE, true);
    }
}
