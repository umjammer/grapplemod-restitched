package com.yyon.grapplinghook.content.registry.internal;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.attachable.LongFallBootsRenderer;
import com.yyon.grapplinghook.client.attachable.model.LongFallBootsModel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.model.geom.ModelLayerLocation;

/**
 * Model layers used by the mod's custom armour.
 * Fabric's ModelLayerRegistry + ArmorRenderer replace the old renderer/LayerDefinitions mixins.
 */
@Environment(EnvType.CLIENT)
public class ModEntityLayerIdentifiers {

    public static final ModelLayerLocation LONG_FALL_BOOTS = new ModelLayerLocation(GrappleMod.id("long_fall_boots"), "armor");

    public static void registerAll() {
        ModelLayerRegistry.registerModelLayer(LONG_FALL_BOOTS, LongFallBootsModel::createBodyLayer);
        ArmorRenderer.register(LongFallBootsRenderer::new, ModItems.LONG_FALL_BOOTS.get());
    }

}
