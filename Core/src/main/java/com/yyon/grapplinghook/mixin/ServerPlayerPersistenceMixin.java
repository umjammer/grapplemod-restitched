package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.physics.persistence.HookPersistenceManager;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class ServerPlayerPersistenceMixin {

    @Inject(method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At("TAIL"))
    private void grapplemod$saveHooks(ValueOutput output, CallbackInfo ci) {
        HookPersistenceManager.saveForPlayer((ServerPlayer) (Object) this, output);
    }

    @Inject(method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At("TAIL"))
    private void grapplemod$loadHooks(ValueInput input, CallbackInfo ci) {
        HookPersistenceManager.loadForPlayer((ServerPlayer) (Object) this, input);
    }
}
