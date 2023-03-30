package com.yyon.grapplinghook.mixin.client.attachable;

import java.util.List;

import com.yyon.grapplinghook.registry.GrappleModEntityRenderLayers;
import net.minecraft.client.render.entity.ArmorStandEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.model.ArmorStandArmorEntityModel;
import net.minecraft.entity.decoration.ArmorStandEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorStandEntityRenderer.class)
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class ArmorStandAttachablesMixin {

    @Inject(method = "<init>(Lnet/minecraft/client/render/entity/EntityRendererFactory$Context;)V", at = @At("TAIL"))
    public void appendRenderLayers(EntityRendererFactory.Context context, CallbackInfo ci) {
        FeatureRenderer longFallBootsLayer = GrappleModEntityRenderLayers.LONG_FALL_BOOTS.getLayer(((ArmorStandEntityRenderer) (Object) this), context.getModelLoader());
        ((ArmorStandEntityRenderer) (Object) this).addFeature(longFallBootsLayer);
    }

}
