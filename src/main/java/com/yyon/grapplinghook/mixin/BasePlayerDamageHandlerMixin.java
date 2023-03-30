package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.util.SharedDamageHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class BasePlayerDamageHandlerMixin {

    @Shadow public abstract boolean isInvulnerableTo(DamageSource source);

    @Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"), cancellable = true)
    public void handleDamage(DamageSource source, float damage, CallbackInfo ci){
        if(this.isInvulnerableTo(source)) return;
        if(SharedDamageHandler.handleDamage((Entity) (Object) this, source)) ci.cancel();
    }

}
