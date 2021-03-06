package com.robotgryphon.compactmachines.client.gui.widget;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.List;

public class ScrollableWrappedTextWidget extends AbstractCMGuiWidget {

    private String localeKey;
    private double yScroll = 0;
    private FontRenderer fontRenderer;

    private int maxLinesToShow;
    private int lineIndexStart;
    private List<IReorderingProcessor> lines;
    private int charSize;

    public ScrollableWrappedTextWidget(String key, int x, int y, int width, int height) {
        super(x, y, width, height);
        this.localeKey = key;
        this.fontRenderer = Minecraft.getInstance().font;

        this.recalculate();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        double temp = yScroll - delta;
        yScroll = MathHelper.clamp(temp, 0, lines.size() - maxLinesToShow - 1);
        recalculate();
        return true;
    }

    private void recalculate() {
        String t = I18n.get(localeKey);
        lines = fontRenderer.split(new StringTextComponent(t), width);

        charSize = fontRenderer.width("M");
        int maxOnScreen = height / (charSize + 4);
        maxLinesToShow = Math.min(lines.size(), maxOnScreen);

        // startClamp - either the current line scroll, or the max allowed line
        int startClamp = Math.min((int) Math.floor(yScroll), lines.size());
        lineIndexStart = MathHelper.clamp(0, startClamp, lines.size() - 1);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        matrixStack.pushPose();
        matrixStack.translate(x, y, 10);
        
        FontRenderer fr = Minecraft.getInstance().font;

        try {
            for (int y = lineIndexStart; y <= lineIndexStart + maxLinesToShow; y++) {
                IReorderingProcessor s = lines.get(y);
                fr.drawShadow(matrixStack, s, 0, (y - lineIndexStart) * (charSize + 4), 0xFFFFFF);
            }
        }

        catch(Exception ex1) {}

        matrixStack.popPose();
    }
}
