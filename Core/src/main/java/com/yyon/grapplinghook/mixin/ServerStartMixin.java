package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.config.GrappleModCommonConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftServer.class)
public abstract class ServerStartMixin {

    // MinecraftServer#setFlightAllowed was removed; flight permission is now
    // read through allowFlight(), so force it there instead.
    @Inject(method = "allowFlight()Z", at = @At("HEAD"), cancellable = true)
    public void onAllowFlight(CallbackInfoReturnable<Boolean> cir) {
        if (GrappleModCommonConfig.get().forceAllowFlight()) {
            cir.setReturnValue(true);
        }
    }
}
