package com.yyon.grapplinghook.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.content.entity.grapplinghook.GrapplinghookEntity;
import com.yyon.grapplinghook.physics.attach.HookAttachment;
import com.yyon.grapplinghook.physics.rope.RopeSegmentHandler;
import com.yyon.grapplinghook.content.registry.CustomizationProperties;
import com.yyon.grapplinghook.content.registry.internal.ModDataComponents;
import com.yyon.grapplinghook.content.customization.data.HookCustomization;
import com.yyon.grapplinghook.content.customization.type.enums.RopeStyle;
import com.yyon.grapplinghook.util.Vec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;


/** This file is part of GrappleMod.

 GrappleMod is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 GrappleMod is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with GrappleMod.  If not, see <a href="http://www.gnu.org/licenses/">...</a>.
 */

@Environment(EnvType.CLIENT)
public class GrapplinghookEntityRenderer<T extends GrapplinghookEntity> extends EntityRenderer<T, GrapplinghookRenderState> {

	public static final Vector3f X_AXIS = new Vector3f(1, 0, 0);
	public static final Vector3f Y_AXIS = new Vector3f(0, 1, 0);
	public static final Vector3f Z_AXIS = new Vector3f(0, 0, 1);

    private static final Identifier ROPE_TEXTURES = GrappleMod.id("textures/entity/rope.png");

	private static final RenderType ROPE_RENDER_GLOWING = RenderTypes.entityTranslucentEmissive(ROPE_TEXTURES);
	private static final RenderType ROPE_RENDER = RenderTypes.entityTranslucentCull(ROPE_TEXTURES);

	private final ItemModelResolver itemModelResolver;
	private final Item item;

	public GrapplinghookEntityRenderer(EntityRendererProvider.Context context, Item itemIn) {
		super(context);
		this.item = itemIn;
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public GrapplinghookRenderState createRenderState() {
		return new GrapplinghookRenderState();
	}

	@Override
	public void extractRenderState(T hookEntity, GrapplinghookRenderState state, float partialTicks) {
		super.extractRenderState(hookEntity, state, partialTicks);
		state.visible = false;

		if (!hookEntity.isAlive()) return;
		if (!(hookEntity.shootingEntity instanceof LivingEntity holder)) return;
		if (!holder.isAlive()) return;

		RopeSegmentHandler ropeHandler = hookEntity.getSegmentHandler();

		// is right hand?
		int handDirection = (holder.getMainArm() == HumanoidArm.RIGHT ? 1 : -1) * (hookEntity.isHeldInMainHand() ? 1 : -1);

		// attack/swing progress
		float completion = holder.getSwingAnimation(partialTicks);
		float swingPosition = Mth.sin(Mth.sqrt(completion) * (float) Math.PI);

		// get the offset from the center of the head to the hand
		boolean isFirstPerson = this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && holder == Minecraft.getInstance().player;
		Vec handOffset = isFirstPerson
				? this.getFirstPersonHandOffset(holder, handDirection, swingPosition, partialTicks)
				: this.getThirdPersonHandOffset(holder, handDirection, swingPosition, partialTicks);

		// get the hand position
		handOffset.y += holder.getEyeHeight();
		Vec handPosition = handOffset.add(Vec.partialPositionVec(holder, partialTicks));

		double dispatcherLerpX = Mth.lerp((double) partialTicks, hookEntity.xOld, hookEntity.getX());
		double dispatcherLerpY = Mth.lerp((double) partialTicks, hookEntity.yOld, hookEntity.getY());
		double dispatcherLerpZ = Mth.lerp((double) partialTicks, hookEntity.zOld, hookEntity.getZ());

		HookAttachment attachment = hookEntity.attachment();
		Vec renderAnchor;
		if (attachment != null && attachment.rendersViaExplicitAnchor()) {
			Vec3 w = attachment.worldHitPoint(partialTicks);
			renderAnchor = new Vec(w.x, w.y, w.z);
			state.anchorOffsetX = renderAnchor.x - dispatcherLerpX;
			state.anchorOffsetY = renderAnchor.y - dispatcherLerpY;
			state.anchorOffsetZ = renderAnchor.z - dispatcherLerpZ;
		} else {
			renderAnchor = new Vec(dispatcherLerpX, dispatcherLerpY, dispatcherLerpZ);
			state.anchorOffsetX = 0;
			state.anchorOffsetY = 0;
			state.anchorOffsetZ = 0;
		}

		state.renderAnchor = renderAnchor;
		state.handPosition = handPosition;
		state.handDirection = handDirection;
		state.attachDirection = this.computeAttachDirection(hookEntity, ropeHandler, handPosition, renderAnchor);
		state.setSegments(ropeHandler == null ? null : ropeHandler.getSegments());
		state.taut = hookEntity.taut;

		HookCustomization volume = hookEntity.getCurrentCustomizations();
		state.glowingRope = volume.get(CustomizationProperties.GLOWING_ROPE.get());
		state.ropeStyle = volume.get(CustomizationProperties.ROPE_STYLE.get());

		this.itemModelResolver.updateForNonLiving(state.hookItem, this.getStackToRender(), ItemDisplayContext.NONE, hookEntity);
		state.visible = true;
	}

	@Override
	public void submit(GrapplinghookRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
		if (state.visible) {
			poseStack.pushPose();
			poseStack.translate(state.anchorOffsetX, state.anchorOffsetY, state.anchorOffsetZ);
			this.drawHook(state, poseStack, collector);
			this.drawRope(state, poseStack, collector);
			poseStack.popPose();
		}

		super.submit(state, poseStack, collector, camera);
	}

	protected Vec getFirstPersonHandOffset(LivingEntity grappleHookHolder, int handDirection, float swingPos, float partialTicks) {
		// base hand offset (no swing, when facing +Z)
		double d7 = this.entityRenderDispatcher.options.fov().get();
		d7 = d7 / 100.0D;

		Vec handOffset = new Vec(
				(double) handDirection * -0.46D * d7,
				-0.18D * d7,
				0.38D
		);

		// apply swing
		handOffset = handOffset.rotatePitch(-swingPos * 0.7F);
		handOffset = handOffset.rotateYaw(-swingPos * 0.5F);

		// apply looking direction
		handOffset = handOffset.rotatePitch(-Vec.lerp(partialTicks, grappleHookHolder.xRotO, grappleHookHolder.getXRot()) * ((float)Math.PI / 180F));
		return handOffset.rotateYaw(Vec.lerp(partialTicks, grappleHookHolder.yRotO, grappleHookHolder.getYRot()) * ((float)Math.PI / 180F));
	}

	protected Vec getThirdPersonHandOffset(LivingEntity grappleHookHolder, int handDirection, float swingPos, float partialTicks) {
		// base hand offset (no swing, when facing +Z)
		Vec handOffset = new Vec(
				(double) handDirection * -0.36D,
				-0.65D + (grappleHookHolder.isCrouching() ? -0.1875F : 0.0F),
				0.6D
		);

		// apply swing
		handOffset = handOffset.rotatePitch(swingPos * 0.7F);

		// apply body rotation
		return handOffset.rotateYaw(Vec.lerp(partialTicks, grappleHookHolder.yBodyRotO, grappleHookHolder.yBodyRot) * ((float)Math.PI / 180F));
	}

    private static Vec getRelativeToAnchor(Vec renderAnchor, Vec inVec) {
    	return inVec.sub(renderAnchor);
    }

	private Vec computeAttachDirection(GrapplinghookEntity hookEntity, RopeSegmentHandler ropeHandler, Vec handPosition, Vec renderAnchor) {
		Vec attachDirection = Vec.motionVec(hookEntity).scale(-1);

		if (attachDirection.length() == 0) {

			if (hookEntity.attachDirection != null) {
				attachDirection = hookEntity.attachDirection;

			} else {

				List<Vec> dirSegs = ropeHandler == null ? null : ropeHandler.getSegments();
				if (dirSegs == null || dirSegs.size() <= 2) {
					attachDirection = getRelativeToAnchor(renderAnchor, new Vec(handPosition));

				} else {
					Vec from = dirSegs.get(1);
					attachDirection = from.sub(renderAnchor);
				}
			}
		}

		attachDirection.mutableNormalize();

		if (hookEntity.isAttachedToSurface() && hookEntity.attachDirection != null)
			attachDirection = hookEntity.attachDirection;

		hookEntity.attachDirection = attachDirection;
		return new Vec(attachDirection);
	}

	private void drawHook(GrapplinghookRenderState state, PoseStack matrix, SubmitNodeCollector collector) {
		Vec attachDirection = state.attachDirection;

		// transformation so hook texture is facing the correct way
		matrix.pushPose();
		matrix.scale(0.5F, 0.5F, 0.5F);

		matrix.rotate(rotateAxis(-attachDirection.getYaw(), Y_AXIS));
		matrix.rotate(rotateAxis(attachDirection.getPitch() - 90.0f, X_AXIS));
		matrix.rotate(rotateAxis(45.0f * state.handDirection, Y_AXIS));
		matrix.rotate(rotateAxis(-45.0f, Z_AXIS));

		// draw hook
		state.hookItem.submit(matrix, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

		// revert transformation
		matrix.popPose();
	}

	public static Quaternionf rotateAxis(double angleDegrees, Vector3f axis) {
		return new Quaternionf().rotateAxis((float) Math.toRadians(angleDegrees), axis);
	}

	private void drawRope(GrapplinghookRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
		RenderType renderType = state.glowingRope ? ROPE_RENDER_GLOWING : ROPE_RENDER;
		RopeStyle styleId = state.ropeStyle;
		int packedLight = state.lightCoords;
		Vec renderAnchor = state.renderAnchor;
		Vec handPosition = state.handPosition;
		List<Vec> segments = state.segments;

		collector.submitCustomGeometry(poseStack, renderType, (pose, vertexBuffer) -> {
			// draw rope
			if (segments == null) {
				// if no segmenthandler, straight line from hand to hook
				Vec finishRelative = getRelativeToAnchor(renderAnchor, new Vec(handPosition));
				drawSegment(new Vec(0,0,0), finishRelative, 1.0F, vertexBuffer, pose, packedLight, styleId);

			} else {
				for (int i = 0; i < segments.size() - 1; i++) {
					Vec from = segments.get(i);
					Vec to = segments.get(i+1);

					if (i == 0)
						from = renderAnchor;

					if (i + 2 == segments.size())
						to = handPosition;

					from = getRelativeToAnchor(renderAnchor, from);
					to = getRelativeToAnchor(renderAnchor, to);

					double taut = i == segments.size() - 2
							? state.taut
							: 1.0D;

					drawSegment(from, to, taut, vertexBuffer, pose, packedLight, styleId);
				}
			}

			drawRopeEnding(segments, handPosition, packedLight, styleId, vertexBuffer, pose, renderAnchor);
		});
	}

	private static void drawRopeEnding(List<Vec> endingSegs, Vec handPosition, int packedLight, RopeStyle styleId, VertexConsumer vertexBuffer, PoseStack.Pose pose, Vec renderAnchor) {
		// draw tip of rope closest to hand
		Vec hook_pos = renderAnchor;
		Vec hand_closest = endingSegs == null || endingSegs.size() <= 2
				? hook_pos
				: endingSegs.get(endingSegs.size() - 2);

		Vec diff = hand_closest.sub(handPosition);
		Vec forward = diff.withMagnitude(1);
		Vec up = forward.cross(new Vec(1, 0, 0));

		if (up.length() == 0)
			up = forward.cross(new Vec(0, 0, 1));

		up.mutableSetMagnitude(0.025);

		Vec sideDir = forward.cross(up);
		sideDir.mutableSetMagnitude(0.025);

		Vec[] corners = new Vec[] {
				up.scale(-1).add(sideDir.scale(-1)),
				up.scale(-1).add(sideDir),
				up.add(sideDir),
				up.add(sideDir.scale(-1))
		};

		float[][] uvs = new float[][] {
				{styleId.getTextureMinBound(),  0f},
				{styleId.getTextureMidBound(),  0f},
				{styleId.getTextureMidBound(),  1f / 16f},
				{styleId.getTextureMinBound(),  1f / 16f}
		};

		for (int side = 0; side < 4; side++) {
			Vec corner = corners[side];
			Vec normal = corner.normalize();
			Vec cornerPos = getRelativeToAnchor(renderAnchor, handPosition).add(corner);
			vertexBuffer
					.addVertex(pose, (float) cornerPos.x, (float) cornerPos.y, (float) cornerPos.z)
					.setColor(255, 255, 255, 255)
					.setUv(uvs[side][0], uvs[side][1]).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
					.setNormal(pose, (float) normal.x, (float) normal.y, (float) normal.z);
		}
	}

	// draw a segment of the rope
    private static void drawSegment(Vec start, Vec finish, double taut, VertexConsumer vertexBuffer, PoseStack.Pose pose, int packedLight, RopeStyle style) {
    	if (start.sub(finish).length() < 0.05)
			return;

		float ropeStyleUVStart = style.getTextureMinBound();
		float ropeStyleUVEnd = style.getTextureMaxBound();

		int number_squares = taut == 1.0F ? 1 : 16;

    	Vec diff = finish.sub(start);

        Vec forward = diff.withMagnitude(1);
        Vec up = forward.cross(new Vec(1, 0, 0));

        if (up.length() == 0)
			up = forward.cross(new Vec(0, 0, 1));

        up.mutableSetMagnitude(0.025);
        Vec sideDir = forward.cross(up);
        sideDir.mutableSetMagnitude(0.025);

        Vec[] corners = new Vec[] {
				up.scale(-1).add(sideDir.scale(-1)),
				up.add(sideDir.scale(-1)),
				up.add(sideDir),
				up.scale(-1).add(sideDir)
		};

        for (int side = 0; side < 4; side++) {
            Vec corner1 = corners[side];
            Vec corner2 = corners[(side + 1) % 4];

        	Vec normal1 = corner1.normalize();
        	Vec normal2 = corner2.normalize();

			boolean flipNormal = side % 2 == 0;

            for (int square_num = 0; square_num < number_squares; square_num++) {
                float squarefrac1 = (float)square_num / (float) number_squares;
                Vec pos1 = start.add(diff.scale(squarefrac1));
                pos1.y += - (1 - taut) * (0.25 - Math.pow((squarefrac1 - 0.5), 2)) * 1.5;

                float squarefrac2 = ((float) square_num+1) / (float) number_squares;
                Vec pos2 = start.add(diff.scale(squarefrac2));
                pos2.y += - (1 - taut) * (0.25 - Math.pow((squarefrac2 - 0.5), 2)) * 1.5;

                Vec corner1pos1 = pos1.add(corner1);
                Vec corner2pos1 = pos1.add(corner2);
                Vec corner1pos2 = pos2.add(corner1);
                Vec corner2pos2 = pos2.add(corner2);

				float uLeft = flipNormal ? ropeStyleUVEnd : ropeStyleUVStart;
				float uRight = flipNormal ? ropeStyleUVStart : ropeStyleUVEnd;

                vertexBuffer
						.addVertex(pose, (float) corner1pos1.x, (float) corner1pos1.y, (float) corner1pos1.z)
						.setColor(255, 255, 255, 255)
						.setUv(uLeft, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
						.setNormal(pose, (float) normal1.x, (float) normal1.y, (float) normal1.z);
                vertexBuffer
						.addVertex(pose, (float) corner2pos1.x, (float) corner2pos1.y, (float) corner2pos1.z)
						.setColor(255, 255, 255, 255)
						.setUv(uRight, 0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
						.setNormal(pose, (float) normal2.x, (float) normal2.y, (float) normal2.z);
                vertexBuffer
						.addVertex(pose, (float) corner2pos2.x, (float) corner2pos2.y, (float) corner2pos2.z)
						.setColor(255, 255, 255, 255)
						.setUv(uRight, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
						.setNormal(pose, (float) normal2.x, (float) normal2.y, (float) normal2.z);
				vertexBuffer
						.addVertex(pose, (float) corner1pos2.x, (float) corner1pos2.y, (float) corner1pos2.z)
						.setColor(255, 255, 255, 255)
						.setUv(uLeft, 1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight)
						.setNormal(pose, (float) normal1.x, (float) normal1.y, (float) normal1.z);
            }
        }

    }

	@Override
	protected boolean affectedByCulling(T entity) {
		// The rope can stretch far outside the hook's own bounding box.
		return false;
	}

	public ItemStack getStackToRender() {
		ItemStack stack = new ItemStack(this.item);
		stack.set(ModDataComponents.FORCE_HOOK_DISPLAY, Unit.INSTANCE);
        return stack;
    }
}
