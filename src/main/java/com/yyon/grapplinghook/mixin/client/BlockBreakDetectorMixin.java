package com.yyon.grapplinghook.mixin.client;

import com.yyon.grapplinghook.client.ClientControllerManager;
import com.yyon.grapplinghook.controller.GrappleController;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class BlockBreakDetectorMixin {

    // Was in client events but uses a server class????

    @Inject(method = "tryBreakBlock(Lnet/minecraft/util/math/BlockPos;)Z", at = @At(value = "HEAD", shift = At.Shift.BY, by = 1))
    public void handleBlockBreak(BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (pos != null) {
            if (ClientControllerManager.controllerPos.containsKey(pos)) {
                GrappleController control = ClientControllerManager.controllerPos.get(pos);

                control.unattach();

                ClientControllerManager.controllerPos.remove(pos);
            }
        }
    }

}
