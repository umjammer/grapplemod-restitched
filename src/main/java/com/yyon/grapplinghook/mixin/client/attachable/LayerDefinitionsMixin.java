package com.yyon.grapplinghook.mixin.client.attachable;

import com.google.common.collect.ImmutableMap;
import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.registry.GrappleModEntityRenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModels;

@Mixin(EntityModels.class)
public class LayerDefinitionsMixin {

    private static ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builderRef = null;

    @ModifyVariable(method = "getModels()Ljava/util/Map;", at = @At("STORE"))
    private static ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder(ImmutableMap.Builder<EntityModelLayer, TexturedModelData> builder) {
        builderRef = builder;
        return builder;
    }

    //@Inject(method = "createRoots()Ljava/util/Map;", at = @At(value = "HEAD"), locals = LocalCapture.CAPTURE_FAILHARD)
    //private static void builder(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> cir, ImmutableMap.Builder<ModelLayerLocation, LayerDefinition> builder) {
    //    builderRef = builder;
    //}

    @Inject(method = "getModels()Ljava/util/Map;",
            remap = false,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/google/common/collect/ImmutableMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableMap$Builder;",
                    ordinal = 1
            ))
    private static void insertLayers(CallbackInfoReturnable<Map<EntityModelLayer, TexturedModelData>> cir) {
        GrappleModEntityRenderLayers.RenderLayerEntry longFallBoots = GrappleModEntityRenderLayers.LONG_FALL_BOOTS;

        builderRef.put(longFallBoots.getLocation(), longFallBoots.get());
    }

}
