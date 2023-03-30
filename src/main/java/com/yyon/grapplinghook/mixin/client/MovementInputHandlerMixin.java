package com.yyon.grapplinghook.mixin.client;

import com.yyon.grapplinghook.client.ClientControllerManager;
import com.yyon.grapplinghook.controller.AirfrictionController;
import com.yyon.grapplinghook.controller.ForcefieldController;
import com.yyon.grapplinghook.controller.GrappleController;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public class MovementInputHandlerMixin {

    @Shadow
    public Input input;

    @Inject(method = "tickNewAi()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/AbstractClientPlayerEntity;tickNewAi()V", shift = At.Shift.AFTER)) // TODO vavi
    public void inputHandle(CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (!MinecraftClient.getInstance().isRunning() || player == null) return;

        int id = player.getId();
        if (ClientControllerManager.controllers.containsKey(id)) {
            Input input = this.input;
            GrappleController control = ClientControllerManager.controllers.get(id);
            control.receivePlayerMovementMessage(input.movementSideways, input.movementForward, input.jumping, input.sneaking);

            boolean overrideMovement = true;
            if (MinecraftClient.getInstance().player.isOnGround()) {
                if (!(control instanceof AirfrictionController) && !(control instanceof ForcefieldController)) {
                    overrideMovement = false;
                }
            }

            if (overrideMovement) {
                input.jumping = false;
                input.pressingBack = false;
                input.pressingForward = false;
                input.pressingLeft = false;
                input.pressingRight = false;
                input.movementForward = 0;
                input.movementSideways = 0;
//				input.sneak = false; // fix alternate throw angles
            }
        }
    }
}
