package com.yyon.grapplinghook.registry;

import com.google.common.collect.ImmutableMap;
import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.attachable.LongFallBootsLayer;
import com.yyon.grapplinghook.client.attachable.model.LongFallBootsModel;
import com.yyon.grapplinghook.util.BiParamFunction;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;

// This doesn't use a Built-In registry but follows a style similar to one as
// model locations need registering.
@SuppressWarnings("rawtypes")
public class GrappleModEntityRenderLayers {

    private static HashMap<Identifier, RenderLayerEntry> renderLayers;

    static {
        GrappleModEntityRenderLayers.renderLayers = new HashMap<>();
    }

    public static void registerAll() { }

    public static RenderLayerEntry layer(String path, String modelLayerName, BiParamFunction<FeatureRendererContext, EntityModelLoader, FeatureRenderer> layerFactory, Supplier<TexturedModelData> def) {
        Identifier qualId = GrappleMod.fakeId(path);
        RenderLayerEntry entry = new RenderLayerEntry(qualId, modelLayerName, def, layerFactory);

        entry.registerModelLocation();
        GrappleModEntityRenderLayers.renderLayers.put(qualId, entry);
        entry.finalize(def.get());
        return entry;
    }

    public static RenderLayerEntry layer(String id, BiParamFunction<FeatureRendererContext, EntityModelLoader, FeatureRenderer> layerFactory, Supplier<TexturedModelData> def) {
        return layer(id, "main", layerFactory, def);
    }


    private static BiParamFunction<FeatureRendererContext<?, ?>, EntityModelLoader, FeatureRenderer<?, ?>> noModelIncluded(Function<FeatureRendererContext<?, ?>, FeatureRenderer<?, ?>> layerFactory) {
        return (parent, model) -> layerFactory.apply(parent);
    }


    // Registry Entries:
    public static final RenderLayerEntry LONG_FALL_BOOTS = GrappleModEntityRenderLayers.layer("long_fall_boots", LongFallBootsLayer::new, LongFallBootsModel::generateLayer);


    public static Map<Identifier, RenderLayerEntry> getRenderLayers() {
        return Collections.unmodifiableMap(renderLayers);
    }

    public static class RenderLayerEntry extends AbstractRegistryReference<TexturedModelData> {

        private final EntityModelLayer location;

        private final BiParamFunction<FeatureRendererContext, EntityModelLoader, FeatureRenderer> layerFactory;


        protected RenderLayerEntry(Identifier path, String modelLayerName, Supplier<TexturedModelData> def, BiParamFunction<FeatureRendererContext, EntityModelLoader, FeatureRenderer> layerFactory) {
            super(path, def);
            this.location = new EntityModelLayer(path, modelLayerName);
            this.layerFactory = layerFactory;
        }

        public EntityModelLayer getLocation() {
            return this.location;
        }

        //@SuppressWarnings("unchecked")
        public FeatureRenderer getLayer(FeatureRendererContext<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> parent, EntityModelLoader modelSet) {
            return this.getLayerFactory().apply(parent, modelSet);
        }

        public BiParamFunction<FeatureRendererContext, EntityModelLoader, FeatureRenderer> getLayerFactory() {
            return layerFactory;
        }

        private void registerModelLocation() {
            EntityModelLayer loc = this.getLocation();
            EntityModelLayers.register(loc.getId().getPath(), loc.getName());
        }
    }

}
