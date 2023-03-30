package com.yyon.grapplinghook.client.attachable;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.attachable.model.LongFallBootsModel;
import com.yyon.grapplinghook.registry.GrappleModEntityRenderLayers;
import com.yyon.grapplinghook.registry.GrappleModItems;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class LongFallBootsLayer<T extends LivingEntity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

    public static final Identifier BOOTS_TEXTURE = GrappleMod.id("textures/armor/long_fall_boots.png");

    private final LongFallBootsModel<T> model;

    public LongFallBootsLayer(FeatureRendererContext<T, M> renderLayerParent, EntityModelLoader modelSet) {
        super(renderLayerParent);
        this.model = new LongFallBootsModel<>(modelSet.getModelPart(GrappleModEntityRenderLayers.LONG_FALL_BOOTS.getLocation()));
    }

    @Override
    public void render(MatrixStack poseStack, VertexConsumerProvider buffer, int packedLight, T livingEntity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        ItemStack bootsStack = livingEntity.getEquippedStack(EquipmentSlot.FEET);

        if (bootsStack.isOf(GrappleModItems.LONG_FALL_BOOTS.get())) {
            poseStack.push();
            //poseStack.translate(0.0F, 0.0F, 0.0F);
            this.getContextModel().copyStateTo(this.model);
            this.model.setAngles(livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            VertexConsumer vertexConsumer = ItemRenderer.getArmorGlintConsumer(buffer, RenderLayer.getArmorCutoutNoCull(BOOTS_TEXTURE), false, bootsStack.hasGlint());
            this.model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.DEFAULT_UV, 1.0F, 1.0F, 1.0F, 1.0F);
            poseStack.pop();
        }
    }

}
