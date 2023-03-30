package com.yyon.grapplinghook.mixin.client.attachable;

import com.yyon.grapplinghook.registry.GrappleModEntityRenderLayers;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class PlayerRendererAttachablesMixin {

    @Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Z)V", at = @At("TAIL"))
    public void appendRenderLayers(EntityRendererFactory.Context context, boolean bl, CallbackInfo ci) {
        FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> longFallBootsLayer = GrappleModEntityRenderLayers.LONG_FALL_BOOTS.getLayer(((PlayerEntityRenderer) (Object) this), context.getModelLoader());
        ((PlayerEntityRenderer) (Object) this).addFeature(longFallBootsLayer);
    }

}
