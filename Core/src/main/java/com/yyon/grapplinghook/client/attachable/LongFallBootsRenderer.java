package com.yyon.grapplinghook.client.attachable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.attachable.model.LongFallBootsModel;
import com.yyon.grapplinghook.content.registry.internal.ModEntityLayerIdentifiers;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

/**
 * Renders the 3D long fall boots model. Replaces the old LongFallBootsLayer, which was
 * injected into every humanoid renderer through mixins.
 */
@Environment(EnvType.CLIENT)
public class LongFallBootsRenderer implements ArmorRenderer {

    public static final Identifier BOOTS_TEXTURE = GrappleMod.id("textures/models/armor/long_fall_boot_ish_layer_custom.png");

    private final LongFallBootsModel model;

    public LongFallBootsRenderer(EntityRendererProvider.Context context) {
        this.model = new LongFallBootsModel(context.bakeLayer(ModEntityLayerIdentifiers.LONG_FALL_BOOTS));
    }

    @Override
    public void render(PoseStack poseStack, SubmitNodeCollector collector, ItemStack stack, HumanoidRenderState state,
                       EquipmentSlot slot, int light, HumanoidModel<HumanoidRenderState> contextModel) {
        if (slot != EquipmentSlot.FEET) return;

        RenderType renderType = stack.hasFoil()
                ? RenderTypes.armorCutoutNoCullGlint(BOOTS_TEXTURE)
                : RenderTypes.armorCutoutNoCull(BOOTS_TEXTURE);

        ArmorRenderer.submitTransformCopyingModel(
                contextModel, state,
                this.model, state,
                false,
                collector, poseStack, renderType,
                light, OverlayTexture.NO_OVERLAY, state.outlineColor
        );
    }
}
