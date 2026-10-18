package dev.compactmods.machines.machine.client;

import dev.compactmods.machines.api.room.template.RoomTemplate;
import dev.compactmods.machines.machine.Machines;
import dev.compactmods.machines.machine.i18n.MachineTranslations;
import dev.compactmods.machines.machine.ui.MachineUIMenu;
import dev.robotgryphon.screenlib.ui.api.panel.Panel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;

/** Shows what the machine holds: its core, and the room it's bound to or the template it will create. */
public class MachineInfoPanel extends Panel {

    private static final int LINE = 12;

    private final MachineUIMenu menu;

    public MachineInfoPanel(MachineUIMenu menu) {
        super(160, 72, Machines.Blocks.MACHINE.get().getName());
        this.menu = menu;

        closable(false);
        draggable(true);
        open();
    }

    @Override
    protected void renderContent(GuiGraphicsExtractor graphics, ScreenRectangle content, int mouseX, int mouseY) {
        final var font = Minecraft.getInstance().font;
        final int x = content.left();
        int y = content.top() + 2;

        final var coreSlot = menu.getCoreSlot();
        if (coreSlot == null || !coreSlot.hasItem()) {
            graphics.text(font, Component.literal("No room core"), x, y, CommonColors.LIGHT_GRAY, false);
            return;
        }

        final var core = coreSlot.getItem();
        graphics.text(font, core.getOrDefault(DataComponents.CUSTOM_NAME, core.getItemName()), x, y, CommonColors.YELLOW, false);
        y += LINE;

        final var boundRoom = menu.currentRoom();
        if (boundRoom.isPresent()) {
            graphics.text(font, Component.literal("Bound To: " + boundRoom.get()), x, y, CommonColors.WHITE, false);
            return;
        }

        final var template = menu.currentTemplate();
        if (template.isPresent()) {
            renderTemplateDetails(graphics, template.get(), x, y);
            graphics.text(font, Component.literal("Room pending creation"), x, y + 2 * LINE, CommonColors.LIGHT_GRAY, false);
        }
    }

    private static void renderTemplateDetails(GuiGraphicsExtractor graphics, RoomTemplate template, int x, int y) {
        final var mc = Minecraft.getInstance();
        final var key = mc.level.registryAccess()
                .lookupOrThrow(RoomTemplate.REGISTRY_KEY)
                .getKey(template);

        graphics.text(mc.font, Component.literal("Template: " + key), x, y, CommonColors.WHITE, false);
        graphics.text(mc.font, Component.translatable(MachineTranslations.IDs.SIZE, template.internalDimensions().toString()),
                x, y + LINE, CommonColors.WHITE, false);
    }
}
