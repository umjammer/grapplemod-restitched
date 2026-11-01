package com.yyon.grapplinghook.client.gui.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class FadingMultiLineTextWidget extends MultiLineTextWidget {

    private final int maxFadeTicks;
    private float ticksAlive;

    public FadingMultiLineTextWidget(int x, int y, Component component, int maxFadeTicks, Font font) {
        super(x, y, component, font);
        this.maxFadeTicks = maxFadeTicks;
        this.ticksAlive = 0.0f;
    }


    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        this.ticksAlive += partialTicks;

        float visibilityLifecycle = 1.3f - (this.ticksAlive / this.maxFadeTicks);
        float opacity = Mth.clamp(visibilityLifecycle, 0.0f, 1.0f);

        // Very low opacities aren't worth drawing - skip instead.
        if(opacity * 255 < 5)
            return;

        // Text colour is no longer settable on the widget; text drawn for a
        // widget picks up the widget's alpha instead.
        float initialAlpha = this.getAlpha();
        this.setAlpha(opacity);
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);
        this.setAlpha(initialAlpha);
    }


    public int getMaxLifespan() {
        return this.maxFadeTicks;
    }
}
