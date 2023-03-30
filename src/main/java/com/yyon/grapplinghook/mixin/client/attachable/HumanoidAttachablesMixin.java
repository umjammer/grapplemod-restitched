package com.yyon.grapplinghook.mixin.client.attachable;

import com.yyon.grapplinghook.registry.GrappleModEntityRenderLayers;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedEntityRenderer.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class HumanoidAttachablesMixin {

    @Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;Lnet/minecraft/client/render/entity/model/BipedEntityModel;FFFF)V", at = @At("TAIL"))
    public void appendRenderLayers(EntityRendererFactory.Context context, BipedEntityModel humanoidModel, float f, float g, float h, float i, CallbackInfo ci) {
        FeatureRenderer longFallBootsLayer = GrappleModEntityRenderLayers.LONG_FALL_BOOTS.getLayer(((BipedEntityRenderer) (Object) this), context.getModelLoader());
        ((BipedEntityRenderer) (Object) this).addFeature(longFallBootsLayer);
    }

}
