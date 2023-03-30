package com.yyon.grapplinghook.mixin.client;

import com.yyon.grapplinghook.client.ClientControllerManager;
import com.yyon.grapplinghook.controller.AirfrictionController;
import com.yyon.grapplinghook.controller.GrappleController;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeybindingHandlerMixin {


    @Inject(method = "onKey(JIIII)V", at = @At("TAIL"))
    public void handleModKeybindings(long pWindowPointer, int pKey, int pScanCode, int pAction, int pModifiers, CallbackInfo ci) {
        if(pWindowPointer != MinecraftClient.getInstance().getWindow().getHandle()) return;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (!MinecraftClient.getInstance().isRunning() || player == null) return;


        GrappleController controller = null;
        if (ClientControllerManager.controllers.containsKey(player.getId())) {
            controller = ClientControllerManager.controllers.get(player.getId());
        }

        if (MinecraftClient.getInstance().options.jumpKey.isPressed()) {
            if (controller != null) {
                if (controller instanceof AirfrictionController && ((AirfrictionController) controller).wasSliding) {
                    controller.slidingJump();
                }
            }
        }

        ClientControllerManager.instance.checkSlide(MinecraftClient.getInstance().player);
    }

}
