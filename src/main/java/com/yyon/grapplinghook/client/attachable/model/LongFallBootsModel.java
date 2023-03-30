package com.yyon.grapplinghook.client.attachable.model;

import com.google.common.collect.ImmutableList;
import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.util.model.ModelPath;
import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.render.entity.model.AnimalModel;
import net.minecraft.entity.LivingEntity;
import java.util.Iterator;
import java.util.function.Supplier;

public class LongFallBootsModel<T extends LivingEntity> extends AnimalModel<T> {

    public static final Supplier<Iterator<String>> LEFT_BOOT_PATH = ModelPath.combine(ModelPath.ROOT_TO_LEFT_LEG, "left_boot");
    public static final Supplier<Iterator<String>> RIGHT_BOOT_PATH = ModelPath.combine(ModelPath.ROOT_TO_RIGHT_LEG, "right_boot");


    protected ModelPart parent;

    protected ModelPart leftBoot;
    protected ModelPart rightBoot;


    public LongFallBootsModel(ModelPart root) {
        this.parent = root;
        this.leftBoot = ModelPath.goTo(root, LEFT_BOOT_PATH.get());
        this.rightBoot = ModelPath.goTo(root, RIGHT_BOOT_PATH.get());
    }

    public static TexturedModelData generateLayer() {
        ModelData mesh = new ModelData();
        Dilation expand = new Dilation(1.0F);

        mesh.getRoot().addChild("left_boot", ModelPartBuilder.create().uv(0, 0).cuboid(-4.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, expand), ModelTransform.NONE);
        mesh.getRoot().addChild("right_boot", ModelPartBuilder.create().uv(0, 0).mirrored().cuboid(0.0F, 0.0F, 0.0F, 4.0F, 4.0F, 4.0F, expand), ModelTransform.NONE);

        GrappleMod.LOGGER.info("Generated!");

        return TexturedModelData.of(mesh, 64, 32);
    }

    @Override
    protected Iterable<ModelPart> getHeadParts() {
        return ImmutableList.of();
    }

    @Override
    protected Iterable<ModelPart> getBodyParts() {
        return ImmutableList.of(this.leftBoot, this.rightBoot);
    }

    @Override
    public void setAngles(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

    }
}
