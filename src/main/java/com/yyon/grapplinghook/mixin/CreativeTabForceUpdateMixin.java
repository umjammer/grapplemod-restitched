package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.registry.GrappleModItems;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemGroup.class)
public class CreativeTabForceUpdateMixin {

    @Inject(method = "shouldDisplay()Z", at = @At("RETURN"), cancellable = true)
    private void checkGrappleTabCondition(CallbackInfoReturnable<Boolean> cir) {
        if(GrappleModItems.isCreativeCacheInvalid())
            cir.setReturnValue(true);
    }

}
