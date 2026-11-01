package com.yyon.grapplinghook.mixin.client;

import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.client.physics.ClientPhysicsControllerTracker;
import com.yyon.grapplinghook.client.physics.controller.AirFrictionPhysicsController;
import com.yyon.grapplinghook.client.physics.controller.ForcefieldPhysicsController;
import com.yyon.grapplinghook.client.physics.controller.GrapplingHookPhysicsController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class MovementInputHandlerMixin {

    @Shadow
    public ClientInput input;

    @Inject(method = "aiStep()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/ClientInput;tick()V", shift = At.Shift.AFTER))
    public void inputHandle(CallbackInfo ci) {
        Player player = Minecraft.getInstance().player;
        if (!Minecraft.getInstance().isRunning() || player == null) return;

        ClientPhysicsControllerTracker physManager = GrappleModClient.get().getClientControllerManager();

        int id = player.getId();
        if (!physManager.controllers.containsKey(id))
            return;

        ClientInput input = this.input;
        GrapplingHookPhysicsController control = physManager.controllers.get(id);
        Vec2 move = input.getMoveVector();
        control.receivePlayerMovementMessage(move.x, move.y, input.keyPresses.shift());

        boolean overrideMovement = true;
        if (player.onGround()) {
            if (!(control instanceof AirFrictionPhysicsController) && !(control instanceof ForcefieldPhysicsController)) {
                overrideMovement = false;
            }
        }

        if (overrideMovement) {
            // jump and shift are left alone (shift drives alternate throw angles)
            Input keys = input.keyPresses;
            input.keyPresses = new Input(false, false, false, false, keys.jump(), keys.shift(), keys.sprint());
            ((ClientInputAccessor) input).grapplemod$setMoveVector(Vec2.ZERO);
        }
    }
}
