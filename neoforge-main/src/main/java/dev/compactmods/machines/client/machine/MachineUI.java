package dev.compactmods.machines.client.machine;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.i18n.MachineTranslations;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Inventory;

public class MachineUI extends AbstractContainerScreen<MachineUIMenu> {

    public MachineUI(MachineUIMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {

        final var pose = graphics.pose();

        pose.pushMatrix();
        pose.translate(leftPos, topPos);
        for(var i : this.menu.slots) {
            graphics.fill(i.x, i.y, i.x + 16, i.y + 16, ARGB.color(0.6f, CommonColors.BLUE));
        }
        pose.popMatrix();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);

        final var pose = graphics.pose();

        pose.pushMatrix();
        pose.translate(leftPos, topPos);

        menu.currentTemplate()
                .ifPresent(template -> renderTemplateDetails(graphics, template));

        pose.popMatrix();
    }

    private void renderTemplateDetails(GuiGraphicsExtractor graphics, RoomTemplate template) {
        graphics.text(minecraft.font,
                Component.translatable(MachineTranslations.IDs.SIZE, template.internalDimensions().toString()),
                32, 10, CommonColors.WHITE, true);
    }
}
