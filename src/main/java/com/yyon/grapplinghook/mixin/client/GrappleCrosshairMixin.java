package com.yyon.grapplinghook.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yyon.grapplinghook.client.ClientControllerManager;
import com.yyon.grapplinghook.item.GrapplehookItem;
import com.yyon.grapplinghook.registry.GrappleModItems;
import com.yyon.grapplinghook.util.GrappleCustomization;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class GrappleCrosshairMixin {

    private static final double Z_LEVEL = -90.0D;

    @Final @Shadow
    private MinecraftClient client;

    @Inject(method = "renderCrosshair(Lnet/minecraft/client/util/math/MatrixStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;drawTexture(Lnet/minecraft/client/util/math/MatrixStack;IIIIII)V", shift = At.Shift.AFTER, ordinal = 0))
    public void renderModCrosshair(MatrixStack matrices, CallbackInfo ci) {

        ClientPlayerEntity player = this.client.player;
        ItemStack grapplehookItemStack = null;

        if (player == null) throw new IllegalStateException("Player should not be null when rendering crosshair");

        if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof GrapplehookItem) {
            grapplehookItemStack = player.getStackInHand(Hand.MAIN_HAND);
        } else if (player.getStackInHand(Hand.OFF_HAND).getItem() instanceof GrapplehookItem) {
            grapplehookItemStack = player.getStackInHand(Hand.OFF_HAND);
        }

        if (grapplehookItemStack != null) {
            GrappleCustomization custom = GrappleModItems.GRAPPLING_HOOK.get().getCustomization(grapplehookItemStack);
            double angle = Math.toRadians(custom.angle);
            double verticalAngle = Math.toRadians(custom.verticalthrowangle);

            if (player.isInSneakingPose()) {
                angle = Math.toRadians(custom.sneakingangle);
                verticalAngle = Math.toRadians(custom.sneakingverticalthrowangle);
            }

            if (!custom.doublehook) angle = 0;

            Window resolution = this.client.getWindow();
            int w = resolution.getScaledWidth();
            int h = resolution.getScaledHeight();

            double fov = Math.toRadians(this.client.options.getFov().getValue());
            fov *= player.getFovMultiplier();
            double l = ((double) h/2) / Math.tan(fov/2);

            if (!((verticalAngle == 0) && (!custom.doublehook || angle == 0))) {
                int offset = (int) (Math.tan(angle) * l);
                int verticalOffset = (int) (-Math.tan(verticalAngle) * l);

                this.drawCrosshair(matrices, w / 2 + offset, h / 2 + verticalOffset);
                if (angle != 0) {
                    this.drawCrosshair(matrices, w / 2 - offset, h / 2 + verticalOffset);
                }
            }

            if (custom.rocket && custom.rocket_vertical_angle != 0) {
                int verticalOffset = (int) (-Math.tan(Math.toRadians(custom.rocket_vertical_angle)) * l);
                this.drawCrosshair(matrices, w / 2, h / 2 + verticalOffset);
            }
        }

        double rocketFuel = ClientControllerManager.instance.rocketFuel;

        if (rocketFuel < 1) {
            Window resolution = this.client.getWindow();
            int w = resolution.getScaledWidth();
            int h = resolution.getScaledHeight();

            int totalbarLength = w / 8;

            RenderSystem.getModelViewStack().push();

            this.drawRect(w / 2 - totalbarLength / 2, h * 3 / 4, totalbarLength, 2, 50, 100);
            this.drawRect(w / 2 - totalbarLength / 2, h * 3 / 4, (int) (totalbarLength * rocketFuel), 2, 200, 255);

            RenderSystem.getModelViewStack().pop();
        }
    }


    private void drawCrosshair(MatrixStack mStack, int x, int y) {
        RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
        MinecraftClient.getInstance().inGameHud.drawTexture(mStack, (int) (x - (15.0F/2)), (int) (y - (15.0F/2)), 0, 0, 15, 15);
        RenderSystem.defaultBlendFunc();
    }

    public void drawRect(int x, int y, int width, int height, int g, int a)
    {
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(x, y + height, Z_LEVEL).color(g, g, g, a).next();
        bufferbuilder.vertex(x + width, y + height, Z_LEVEL).color(g, g, g, a).next();
        bufferbuilder.vertex(x + width, y, Z_LEVEL).color(g, g, g, a).next();
        bufferbuilder.vertex(x, y, Z_LEVEL).color(g, g, g, a).next();

        BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
    }
}
