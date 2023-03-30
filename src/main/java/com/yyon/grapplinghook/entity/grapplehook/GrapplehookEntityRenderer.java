package com.yyon.grapplinghook.entity.grapplehook;

import com.yyon.grapplinghook.util.Vec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Arm;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;


/*
 * This file is part of GrappleMod.

    GrappleMod is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    GrappleMod is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GrappleMod.  If not, see <http://www.gnu.org/licenses/>.
 */

@Environment(EnvType.CLIENT)
public class GrapplehookEntityRenderer<T extends GrapplehookEntity> extends EntityRenderer<T>
{

	public static final Vector3f X_AXIS = new Vector3f(1, 0, 0);
	public static final Vector3f Y_AXIS = new Vector3f(0, 1, 0);
	public static final Vector3f Z_AXIS = new Vector3f(0, 0, 1);

	protected static final boolean RENDER_GRAPPLE_HOOK = true;


    protected final Item item;
    private static final Identifier HOOK_TEXTURES = new Identifier("grapplemod", "textures/entity/hook.png");
    private static final Identifier ROPE_TEXTURES = new Identifier("grapplemod", "textures/entity/rope.png");
    private static final RenderLayer ROPE_RENDER = RenderLayer.getEntitySolid(ROPE_TEXTURES);
    EntityRendererFactory.Context context;

	public GrapplehookEntityRenderer(EntityRendererFactory.Context context, Item itemIn) {
		super(context);
		this.item = itemIn;
		this.context = context;
	}

    /**
     * Actually renders the given argument. This is a synthetic bridge method, always casting down its argument and then
     * handing it off to a worker function which does the actual work. In all probabilty, the class Render is generic
     * (Render<T extends Entity>) and this method has signature public void func_76986_a(T entity, double d, double d1,
     * double d2, float f, float f1). But JAD is pre 1.5 so doe
     */
    @Override
    public void render(T hookEntity, float entityYaw, float partialTicks, MatrixStack matrix, VertexConsumerProvider rendertype, int packedLight) {
		if (hookEntity == null || !hookEntity.isAlive()) return;
		
		SegmentHandler segmenthandler = hookEntity.segmentHandler;
		LivingEntity e = (LivingEntity) hookEntity.shootingEntity;
		
		if (e == null || !e.isAlive()) return;


		/* get player hand position */
		
		// is right hand?
		int hand_right = (e.getMainArm() == Arm.RIGHT ? 1 : -1) * (hookEntity.rightHand ? 1 : -1);
		
		// attack/swing progress
		float f = e.getHandSwingProgress(partialTicks);
		float f1 = MathHelper.sin(MathHelper.sqrt(f) * (float)Math.PI);
		
		// get the offset from the center of the head to the hand
		Vec hand_offset;
		if (this.dispatcher.gameOptions.getPerspective().isFirstPerson() && e == MinecraftClient.getInstance().player) {
			// if first person
			
			// base hand offset (no swing, when facing +Z)
			double d7 = this.dispatcher.gameOptions.getFov().getValue();
			d7 = d7 / 100.0D;
			hand_offset = new Vec((double) hand_right * -0.46D * d7, -0.18D * d7, 0.38D)
					.rotatePitch(-f1 * 0.7F) // swing
					.rotateYaw(-f1 * 0.5F)
					.rotatePitch(-Vec.lerp(partialTicks, e.prevPitch, e.getPitch()) * ((float)Math.PI / 180F)) // apply look dir
					.rotateYaw(Vec.lerp(partialTicks, e.prevYaw, e.getYaw()) * ((float)Math.PI / 180F));

		} else {
			// if third person
			
			// base hand offset (no swing, when facing +Z)
			double crouchOffset = e.isInSneakingPose() ? -0.1875F : 0.0F;
			hand_offset = new Vec(hand_right * -0.36D, -0.65D + crouchOffset, 0.6D)
					.rotatePitch(f1 * 0.7F) // apply swing
					.rotateYaw(Vec.lerp(partialTicks, e.prevBodyYaw, e.bodyYaw) * ((float)Math.PI / 180F)); // apply body rotation
		}
		
		// get the hand position
		hand_offset.y += e.getStandingEyeHeight();
		Vec hand_position = hand_offset.add(Vec.partialPositionVec(e, partialTicks));
        
		
		/* draw hook */
		// get direction of rope where hook is attached
		Vec attach_dir = Vec.motionVec(hookEntity).mult(-1);

		if (attach_dir.length() == 0) {
			if (hookEntity.attach_dir != null) {
				attach_dir = hookEntity.attach_dir;

			} else {

		        if (segmenthandler == null || segmenthandler.segments.size() <= 2) {
		        	attach_dir = getRelativeToEntity(hookEntity, new Vec(hand_position), partialTicks);
		        } else {
		    		Vec from = segmenthandler.segments.get(1);
		    		Vec to = Vec.partialPositionVec(hookEntity, partialTicks);
		    		attach_dir = from.sub(to);
		        }
			}
		}

        attach_dir.normalize_ip();

		if (hookEntity.attached && hookEntity.attach_dir != null)
			attach_dir = hookEntity.attach_dir;
		hookEntity.attach_dir = attach_dir;
		
		// transformation so hook texture is facing the correct way
		matrix.push();
		matrix.scale(0.5F, 0.5F, 0.5F);

		matrix.multiply(new Quaternionf().rotateAxis((float) -attach_dir.getYaw(), Y_AXIS));
		matrix.multiply(new Quaternionf().rotateAxis((float) attach_dir.getPitch() - 90.0f, X_AXIS));
		matrix.multiply(new Quaternionf().rotateAxis(45.0f * hand_right, Y_AXIS));
		matrix.multiply(new Quaternionf().rotateAxis(-45.0f, Z_AXIS));
		
		// draw hook
		ItemStack stack = this.getStackToRender();
		BakedModel bakedmodel = context.getItemRenderer().getModel(stack, hookEntity.world, null, hookEntity.getId());

		if(RENDER_GRAPPLE_HOOK)
			context.getItemRenderer()
					.renderItem(stack, ModelTransformationMode.NONE, false, matrix, rendertype, packedLight, OverlayTexture.DEFAULT_UV, bakedmodel);

		// revert transformation
		matrix.pop();

		
		/* draw rope */
		
		// transformation (no tranformation)
        matrix.push();
        MatrixStack.Entry matrixstack$entry = matrix.peek();
        Matrix4f matrix4f1 = matrixstack$entry.getPositionMatrix();
        Matrix3f matrix3f1 = matrixstack$entry.getNormalMatrix();

        // initialize vertexbuffer (used for drawing)
        VertexConsumer vertexbuffer = rendertype.getBuffer(ROPE_RENDER);
        
        // draw rope
        if (segmenthandler == null) {
        	// if no segmenthandler, straight line from hand to hook
    		drawSegment(new Vec(0,0,0), getRelativeToEntity(hookEntity, new Vec(hand_position), partialTicks), 1.0F, vertexbuffer, matrix4f1, matrix3f1, packedLight);

		} else {
        	for (int i = 0; i < segmenthandler.segments.size() - 1; i++) {
        		Vec from = segmenthandler.segments.get(i);
        		Vec to = segmenthandler.segments.get(i+1);
        		
        		if (i == 0)
					from = Vec.partialPositionVec(hookEntity, partialTicks);

        		if (i + 2 == segmenthandler.segments.size())
					to = hand_position;
        		
        		from = getRelativeToEntity(hookEntity, from, partialTicks);
        		to = getRelativeToEntity(hookEntity, to, partialTicks);
        		
        		double taut = 1;
        		if (i == segmenthandler.segments.size() - 2) {
					taut = hookEntity.taut;
        		}
        		
        		drawSegment(from, to, taut, vertexbuffer, matrix4f1, matrix3f1, packedLight);
        	}
        }
        
        // draw tip of rope closest to hand
		Vec hook_pos = Vec.partialPositionVec(hookEntity, partialTicks);
        Vec hand_closest = segmenthandler == null || segmenthandler.segments.size() <= 2
				? hook_pos
				: segmenthandler.segments.get(segmenthandler.segments.size() - 2);

        Vec diff = hand_closest.sub(hand_position);
        Vec forward = diff.changeLen(1);
        Vec up = forward.cross(new Vec(1, 0, 0));

        if (up.length() == 0)
			up = forward.cross(new Vec(0, 0, 1));

        up.changeLen_ip(0.025);

        Vec side = forward.cross(up);
        side.changeLen_ip(0.025);
        
        Vec[] corners = new Vec[] {up.mult(-1).add(side.mult(-1)), up.mult(-1).add(side), up.add(side), up.add(side.mult(-1))};
        float[][] uvs = new float[][] {{0, 0.99F}, {0, 1}, {1, 1}, {1, 0.99F}};
        
        for (int size = 0; size < 4; size++) {
            Vec corner = corners[size];
        	Vec normal = corner.normalize(); //.add(forward.normalize().mult(-1)).normalize();
        	Vec cornerpos = getRelativeToEntity(hookEntity, hand_position, partialTicks).add(corner);
        	vertexbuffer.vertex(matrix4f1, (float) cornerpos.x, (float) cornerpos.y, (float) cornerpos.z).color(255, 255, 255, 255).texture(uvs[size][0], uvs[size][1]).overlay(OverlayTexture.DEFAULT_UV).light(packedLight).normal(matrix3f1, (float) normal.x, (float) normal.y, (float) normal.z).next();
        }

		matrix.pop();
		super.render(hookEntity, entityYaw, partialTicks, matrix, rendertype, packedLight);
    }
    
    Vec getRelativeToEntity(GrapplehookEntity hookEntity, Vec inVec, float partialTicks) {
    	return inVec.sub(Vec.partialPositionVec(hookEntity, partialTicks));
    }

    // draw a segment of the rope
    public void drawSegment(Vec start, Vec finish, double taut, VertexConsumer vertexbuffer, Matrix4f matrix, Matrix3f matrix3, int p_225623_6_) {
    	if (start.sub(finish).length() < 0.05) {
    		return;
    	}

        int number_squares = 16;
        if (taut == 1.0F) {
        	number_squares = 1;
        }

    	Vec diff = finish.sub(start);
        
        Vec forward = diff.changeLen(1);
        Vec up = forward.cross(new Vec(1, 0, 0));
        if (up.length() == 0) {
        	up = forward.cross(new Vec(0, 0, 1));
        }
        up.changeLen_ip(0.025);
        Vec side = forward.cross(up);
        side.changeLen_ip(0.025);
        
        Vec[] corners = new Vec[] {up.mult(-1).add(side.mult(-1)), up.add(side.mult(-1)), up.add(side), up.mult(-1).add(side)};

        for (int size = 0; size < 4; size++) {
            Vec corner1 = corners[size];
            Vec corner2 = corners[(size + 1) % 4];

        	Vec normal1 = corner1.normalize();
        	Vec normal2 = corner2.normalize();
            
            for (int square_num = 0; square_num < number_squares; square_num++) {
                float squarefrac1 = (float)square_num / (float) number_squares;
                Vec pos1 = start.add(diff.mult(squarefrac1));
                pos1.y += - (1 - taut) * (0.25 - Math.pow((squarefrac1 - 0.5), 2)) * 1.5;
                float squarefrac2 = ((float) square_num+1) / (float) number_squares;
                Vec pos2 = start.add(diff.mult(squarefrac2));
                pos2.y += - (1 - taut) * (0.25 - Math.pow((squarefrac2 - 0.5), 2)) * 1.5;
                
                Vec corner1pos1 = pos1.add(corner1);
                Vec corner2pos1 = pos1.add(corner2);
                Vec corner1pos2 = pos2.add(corner1);
                Vec corner2pos2 = pos2.add(corner2);
            	
                vertexbuffer.vertex(matrix, (float) corner1pos1.x, (float) corner1pos1.y, (float) corner1pos1.z).color(255, 255, 255, 255).texture(0, squarefrac1).overlay(OverlayTexture.DEFAULT_UV).light(p_225623_6_).normal(matrix3, (float) normal1.x, (float) normal1.y, (float) normal1.z).next();
                vertexbuffer.vertex(matrix, (float) corner2pos1.x, (float) corner2pos1.y, (float) corner2pos1.z).color(255, 255, 255, 255).texture(1, squarefrac1).overlay(OverlayTexture.DEFAULT_UV).light(p_225623_6_).normal(matrix3, (float) normal2.x, (float) normal2.y, (float) normal2.z).next();
                vertexbuffer.vertex(matrix, (float) corner2pos2.x, (float) corner2pos2.y, (float) corner2pos2.z).color(255, 255, 255, 255).texture(1, squarefrac2).overlay(OverlayTexture.DEFAULT_UV).light(p_225623_6_).normal(matrix3, (float) normal2.x, (float) normal2.y, (float) normal2.z).next();
                vertexbuffer.vertex(matrix, (float) corner1pos2.x, (float) corner1pos2.y, (float) corner1pos2.z).color(255, 255, 255, 255).texture(0, squarefrac2).overlay(OverlayTexture.DEFAULT_UV).light(p_225623_6_).normal(matrix3, (float) normal1.x, (float) normal1.y, (float) normal1.z).next();
            }
        }
        
    }

    @Override
    public boolean shouldRender(T entity, Frustum frustum, double camX, double camY, double camZ) {
		return true;
	}

	public ItemStack getStackToRender() {
		ItemStack stack = new ItemStack(this.item);
		NbtCompound tag = stack.getOrCreateNbt();
		tag.putBoolean("hook", true);
		stack.setNbt(tag);
        return stack;
    }

	/**
	 * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
	 */
	@Override
	public Identifier getTexture(T entity) {
		return HOOK_TEXTURES;
	}
}
