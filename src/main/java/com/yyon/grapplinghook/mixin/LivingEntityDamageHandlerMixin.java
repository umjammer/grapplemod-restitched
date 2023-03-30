package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.item.LongFallBoots;
import com.yyon.grapplinghook.util.SharedDamageHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityDamageHandlerMixin {

    @Shadow public abstract void remove(Entity.RemovalReason reason);

    @Inject(method = "onDeath(Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"), cancellable = true)
    public void handleDeath(DamageSource source, CallbackInfo ci){
        if(SharedDamageHandler.handleDeath((Entity) (Object) this)) ci.cancel();
    }

    @Inject(method = "applyDamage(Lnet/minecraft/entity/damage/DamageSource;F)V", at = @At("HEAD"), cancellable = true)
    public void handleDamage(DamageSource source, float damage, CallbackInfo ci){
        Entity thiss = (Entity) (Object) this;

        if(thiss.isInvulnerableTo(source)) return;
        if(SharedDamageHandler.handleDamage((Entity) (Object) this, source)) ci.cancel();
    }

    @Inject(method = "handleFallDamage(FFLnet/minecraft/entity/damage/DamageSource;)Z", at = @At("HEAD"), cancellable = true)
    public void handleFall(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        Entity thiss = (Entity) (Object) this;
        if (thiss instanceof PlayerEntity player) {

            for (ItemStack armorStack : player.getArmorItems()) {
                if(armorStack == null) continue;
                if(armorStack.getItem() instanceof LongFallBoots)
                    cir.setReturnValue(false);
            }
        }
    }
}
