package com.yyon.grapplinghook.item;

import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.client.keybind.GrappleModKeyBindings;
import com.yyon.grapplinghook.client.keybind.MCKeys;
import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.entity.grapplehook.GrapplehookEntity;
import com.yyon.grapplinghook.network.NetworkManager;
import com.yyon.grapplinghook.network.clientbound.DetachSingleHookMessage;
import com.yyon.grapplinghook.network.clientbound.GrappleDetachMessage;
import com.yyon.grapplinghook.network.serverbound.KeypressMessage;
import com.yyon.grapplinghook.server.ServerControllerManager;
import com.yyon.grapplinghook.util.GrappleCustomization;
import com.yyon.grapplinghook.util.GrappleModUtils;
import com.yyon.grapplinghook.util.Vec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import java.util.HashMap;
import java.util.List;


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

public class GrapplehookItem extends Item implements KeypressItem, DroppableItem {
	public static HashMap<Entity, GrapplehookEntity> grapplehookEntitiesLeft = new HashMap<>();
	public static HashMap<Entity, GrapplehookEntity> grapplehookEntitiesRight = new HashMap<>();
	
	public GrapplehookItem() {
		super(new Item.Settings().maxCount(1).maxDamage(GrappleConfig.getConf().grapplinghook.other.default_durability));
	}

	public boolean hasHookEntity(Entity entity) {
		GrapplehookEntity hookLeft = getHookEntityLeft(entity);
		GrapplehookEntity hookRight = getHookEntityRight(entity);
		return (hookLeft != null) || (hookRight != null);
	}

	public void setHookEntityLeft(Entity entity, GrapplehookEntity hookEntity) {
		GrapplehookItem.grapplehookEntitiesLeft.put(entity, hookEntity);
	}
	public void setHookEntityRight(Entity entity, GrapplehookEntity hookEntity) {
		GrapplehookItem.grapplehookEntitiesRight.put(entity, hookEntity);
	}
	public GrapplehookEntity getHookEntityLeft(Entity entity) {
		if (GrapplehookItem.grapplehookEntitiesLeft.containsKey(entity)) {
			GrapplehookEntity hookEntity = GrapplehookItem.grapplehookEntitiesLeft.get(entity);
			if (hookEntity != null && hookEntity.isAlive()) {
				return hookEntity;
			}
		}
		return null;
	}
	public GrapplehookEntity getHookEntityRight(Entity entity) {
		if (GrapplehookItem.grapplehookEntitiesRight.containsKey(entity)) {
			GrapplehookEntity hookEntity = GrapplehookItem.grapplehookEntitiesRight.get(entity);
			if (hookEntity != null && hookEntity.isAlive()) {
				return hookEntity;
			}
		}
		return null;
	}

	@Override
	public boolean canRepair(ItemStack stack, ItemStack repair) {
        if (repair != null && repair.getItem().equals(Items.LEATHER)) return true;
        return super.canRepair(stack, repair);
	}

	@Override
	public boolean postHit(ItemStack stack, LivingEntity target, LivingEntity attacker) {
		return true;
	}

	// previously: onBlockStartBreak
	@Override
	public boolean postMine(ItemStack stack, World level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
		return true;
	}

	@Override
	public boolean canMine(BlockState p_195938_1_, World p_195938_2_, BlockPos p_195938_3_,
			PlayerEntity p_195938_4_) {
		return false;
	}

	@Override
	public void onCustomKeyDown(ItemStack stack, PlayerEntity player, KeypressItem.Keys key, boolean ismainhand) {
		if (player.world.isClient) {
			if (key == KeypressItem.Keys.LAUNCHER) {
				if (this.getCustomization(stack).enderstaff) {
					GrappleModClient.get().launchPlayer(player);
				}
			} else if (key == KeypressItem.Keys.THROWLEFT || key == KeypressItem.Keys.THROWRIGHT || key == KeypressItem.Keys.THROWBOTH) {
				NetworkManager.packetToServer(new KeypressMessage(key, true));

			} else if (key == KeypressItem.Keys.ROCKET) {
				GrappleCustomization custom = this.getCustomization(stack);
				if (custom.rocket) {
					GrappleModClient.get().startRocket(player, custom);
				}
			}
		} else {
	    	GrappleCustomization custom = this.getCustomization(stack);

			if (key == KeypressItem.Keys.THROWBOTH || (!custom.doublehook && (key == KeypressItem.Keys.THROWLEFT || key == KeypressItem.Keys.THROWRIGHT))) {
	        	throwBoth(stack, player.world, player, ismainhand);

			} else if (key == KeypressItem.Keys.THROWLEFT) {
				GrapplehookEntity hookLeft = getHookEntityLeft(player);

	    		if (hookLeft != null) {
	    			detachLeft(player);
		    		return;
				}
				
				stack.damage(1, (ServerPlayerEntity) player, (p) -> {});
				if (stack.getCount() <= 0) {
					return;
				}
				
				boolean threw = throwLeft(stack, player.world, player, ismainhand);

				if (threw) {
			        player.world.playSound(null, player.getPos().x, player.getPos().y, player.getPos().z, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.0F / (player.getRandom().nextFloat() * 0.4F + 1.2F) + 2.0F * 0.5F);
				}

			} else if (key == KeypressItem.Keys.THROWRIGHT) {
				GrapplehookEntity hookRight = getHookEntityRight(player);

	    		if (hookRight != null) {
	    			detachRight(player);
		    		return;
				}
				
				stack.damage(1, (ServerPlayerEntity) player, (p) -> {});
				if (stack.getCount() <= 0) {
					return;
				}
				
				throwRight(stack, player.world, player, ismainhand);

		        player.world.playSound(null, player.getPos().x, player.getPos().y, player.getPos().z, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.0F / (player.getRandom().nextFloat() * 0.4F + 1.2F) + 2.0F * 0.5F);
			}
		}
	}
	
	@Override
	public void onCustomKeyUp(ItemStack stack, PlayerEntity player, KeypressItem.Keys key, boolean ismainhand) {
		if (player.world.isClient) {
			if (key == KeypressItem.Keys.THROWLEFT || key == KeypressItem.Keys.THROWRIGHT || key == KeypressItem.Keys.THROWBOTH) {
				NetworkManager.packetToServer(new KeypressMessage(key, false));
			}
		} else {
	    	GrappleCustomization custom = this.getCustomization(stack);
	    	
	    	if (custom.detachonkeyrelease) {
	    		GrapplehookEntity hookLeft = getHookEntityLeft(player);
	    		GrapplehookEntity hookRight = getHookEntityRight(player);
	    		
				if (key == KeypressItem.Keys.THROWBOTH) {
					detachBoth(player);
				} else if (key == KeypressItem.Keys.THROWLEFT) {
		    		if (hookLeft != null) detachLeft(player);
				} else if (key == KeypressItem.Keys.THROWRIGHT) {
		    		if (hookRight != null) detachRight(player);
				}
	    	}
		}
	}

	public void throwBoth(ItemStack stack, World worldIn, LivingEntity entityLiving, boolean righthand) {
		GrapplehookEntity hookLeft = getHookEntityLeft(entityLiving);
		GrapplehookEntity hookRight = getHookEntityRight(entityLiving);

		if (hookLeft != null || hookRight != null) {
			detachBoth(entityLiving);
    		return;
		}

		stack.damage(1, (ServerPlayerEntity) entityLiving, (p) -> {});
		if (stack.getCount() <= 0) {
			return;
		}

    	GrappleCustomization custom = this.getCustomization(stack);
  		double angle = custom.angle;
//  		double verticalangle = custom.verticalthrowangle;
  		if (entityLiving.isInSneakingPose()) {
  			angle = custom.sneakingangle;
//  			verticalangle = custom.sneakingverticalthrowangle;
  		}

    	if (!(!custom.doublehook || angle == 0)) {
    		throwLeft(stack, worldIn, entityLiving, righthand);
    	}
		throwRight(stack, worldIn, entityLiving, righthand);

		entityLiving.world.playSound(null, entityLiving.getPos().x, entityLiving.getPos().y, entityLiving.getPos().z, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.NEUTRAL, 1.0F, 1.0F / (worldIn.random.nextFloat() * 0.4F + 1.2F) + 2.0F * 0.5F);
	}
	
	public boolean throwLeft(ItemStack stack, World worldIn, LivingEntity entityLiving, boolean righthand) {
    	GrappleCustomization custom = this.getCustomization(stack);
    	
  		double angle = custom.angle;
  		double verticalangle = custom.verticalthrowangle;
  		
  		if (entityLiving.isInSneakingPose()) {
  			angle = custom.sneakingangle;
  			verticalangle = custom.sneakingverticalthrowangle;
  		}

		Vec anglevec = Vec.fromAngles(Math.toRadians(-angle), Math.toRadians(verticalangle));
  		anglevec = anglevec.rotatePitch(Math.toRadians(-entityLiving.getPitch(1.0F)));
  		anglevec = anglevec.rotateYaw(Math.toRadians(entityLiving.getYaw(1.0F)));
        float velx = -MathHelper.sin((float) anglevec.getYaw() * 0.017453292F) * MathHelper.cos((float) anglevec.getPitch() * 0.017453292F);
        float vely = -MathHelper.sin((float) anglevec.getPitch() * 0.017453292F);
        float velz = MathHelper.cos((float) anglevec.getYaw() * 0.017453292F) * MathHelper.cos((float) anglevec.getPitch() * 0.017453292F);
		GrapplehookEntity hookEntity = this.createGrapplehookEntity(stack, worldIn, entityLiving, false, true);
        float extravelocity = (float) Vec.motionVec(entityLiving).distAlong(new Vec(velx, vely, velz));
        if (extravelocity < 0) { extravelocity = 0; }
        hookEntity.setVelocity( velx, vely, velz, hookEntity.getVelocity_() + extravelocity, 0.0F);
        
		worldIn.spawnEntity(hookEntity);
		setHookEntityLeft(entityLiving, hookEntity);    			
		
		return true;
	}
	
	public void throwRight(ItemStack stack, World worldIn, LivingEntity entityLiving, boolean righthand) {
    	GrappleCustomization custom = this.getCustomization(stack);
    	
  		double angle = custom.angle;
  		double verticalangle = custom.verticalthrowangle;
  		if (entityLiving.isInSneakingPose()) {
  			angle = custom.sneakingangle;
  			verticalangle = custom.sneakingverticalthrowangle;
  		}

    	if (!custom.doublehook || angle == 0) {
			GrapplehookEntity hookEntity = this.createGrapplehookEntity(stack, worldIn, entityLiving, righthand, false);
      		Vec anglevec = new Vec(0,0,1).rotatePitch(Math.toRadians(verticalangle));
      		anglevec = anglevec.rotatePitch(Math.toRadians(-entityLiving.getPitch(1.0F)));
      		anglevec = anglevec.rotateYaw(Math.toRadians(entityLiving.getYaw(1.0F)));
	        float velx = -MathHelper.sin((float) anglevec.getYaw() * 0.017453292F) * MathHelper.cos((float) anglevec.getPitch() * 0.017453292F);
	        float vely = -MathHelper.sin((float) anglevec.getPitch() * 0.017453292F);
	        float velz = MathHelper.cos((float) anglevec.getYaw() * 0.017453292F) * MathHelper.cos((float) anglevec.getPitch() * 0.017453292F);
	        float extravelocity = (float) Vec.motionVec(entityLiving).distAlong(new Vec(velx, vely, velz));
	        if (extravelocity < 0) { extravelocity = 0; }
	        hookEntity.setVelocity(velx, vely, velz, hookEntity.getVelocity_() + extravelocity, 0.0F);
			setHookEntityRight(entityLiving, hookEntity);
			worldIn.spawnEntity(hookEntity);
    	} else {

			Vec anglevec = Vec.fromAngles(Math.toRadians(angle), Math.toRadians(verticalangle));
      		anglevec = anglevec.rotatePitch(Math.toRadians(-entityLiving.getPitch(1.0F)));
      		anglevec = anglevec.rotateYaw(Math.toRadians(entityLiving.getYaw(1.0F)));
	        float velx = -MathHelper.sin((float) anglevec.getYaw() * 0.017453292F) * MathHelper.cos((float) anglevec.getPitch() * 0.017453292F);
	        float vely = -MathHelper.sin((float) anglevec.getPitch() * 0.017453292F);
	        float velz = MathHelper.cos((float) anglevec.getYaw() * 0.017453292F) * MathHelper.cos((float) anglevec.getPitch() * 0.017453292F);
			GrapplehookEntity hookEntity = this.createGrapplehookEntity(stack, worldIn, entityLiving, true, true);
	        float extravelocity = (float) Vec.motionVec(entityLiving).distAlong(new Vec(velx, vely, velz));
	        if (extravelocity < 0) { extravelocity = 0; }
	        hookEntity.setVelocity(velx, vely, velz, hookEntity.getVelocity_() + extravelocity, 0.0F);
            
			worldIn.spawnEntity(hookEntity);
			setHookEntityRight(entityLiving, hookEntity);
		}
	}
	
	public void detachBoth(LivingEntity entityLiving) {
		GrapplehookEntity hookLeft = getHookEntityLeft(entityLiving);
		GrapplehookEntity hookRight = getHookEntityRight(entityLiving);

		setHookEntityLeft(entityLiving, null);
		setHookEntityRight(entityLiving, null);
		
		if (hookLeft != null) {
			hookLeft.removeServer();
		}
		if (hookRight != null) {
			hookRight.removeServer();
		}

		int id = entityLiving.getId();
		GrappleModUtils.sendToCorrectClient(new GrappleDetachMessage(id), entityLiving.getId(), entityLiving.world);

		ServerControllerManager.attached.remove(id);
	}
	
	public void detachLeft(LivingEntity entityLiving) {
		GrapplehookEntity hookLeft = getHookEntityLeft(entityLiving);
		
		setHookEntityLeft(entityLiving, null);
		
		if (hookLeft != null) {
			hookLeft.removeServer();
		}
		
		int id = entityLiving.getId();
		
		// remove controller if hook is attached
		if (getHookEntityRight(entityLiving) == null) {
			GrappleModUtils.sendToCorrectClient(new GrappleDetachMessage(id), id, entityLiving.world);
		} else {
			GrappleModUtils.sendToCorrectClient(new DetachSingleHookMessage(id, hookLeft.getId()), id, entityLiving.world);
		}

		ServerControllerManager.attached.remove(id);
	}
	
	public void detachRight(LivingEntity entityLiving) {
		GrapplehookEntity hookRight = getHookEntityRight(entityLiving);
		
		setHookEntityRight(entityLiving, null);
		
		if (hookRight != null) {
			hookRight.removeServer();
		}
		
		int id = entityLiving.getId();
		
		// remove controller if hook is attached
		if (getHookEntityLeft(entityLiving) == null) {
			GrappleModUtils.sendToCorrectClient(new GrappleDetachMessage(id), id, entityLiving.world);
		} else {
			GrappleModUtils.sendToCorrectClient(new DetachSingleHookMessage(id, hookRight.getId()), id, entityLiving.world);
		}

		ServerControllerManager.attached.remove(id);
	}
	
    public double getAngle(LivingEntity entity, ItemStack stack) {
    	GrappleCustomization custom = this.getCustomization(stack);
    	if (entity.isInSneakingPose()) {
    		return custom.sneakingangle;
    	} else {
    		return custom.angle;
    	}
    }
	
	public GrapplehookEntity createGrapplehookEntity(ItemStack stack, World worldIn, LivingEntity entityLiving, boolean righthand, boolean isdouble) {
		GrapplehookEntity hookEntity = new GrapplehookEntity(worldIn, entityLiving, righthand, this.getCustomization(stack), isdouble);
		ServerControllerManager.addGrapplehookEntity(entityLiving.getId(), hookEntity);
		return hookEntity;
	}
    
    public GrappleCustomization getCustomization(ItemStack itemstack) {
    	NbtCompound tag = itemstack.getOrCreateNbt();
    	
    	if (tag.contains("custom")) {
        	GrappleCustomization custom = new GrappleCustomization();
    		custom.loadNBT(tag.getCompound("custom"));
        	return custom;
    	} else {
    		GrappleCustomization custom = this.getDefaultCustomization();

			NbtCompound nbt = custom.writeNBT();
			
			tag.put("custom", nbt);
			itemstack.setNbt(tag);

    		return custom;
    	}
    }
    
    public GrappleCustomization getDefaultCustomization() {
    	return new GrappleCustomization();
    }
    
	@Override
	@Environment(EnvType.CLIENT)
	public void appendTooltip(ItemStack stack, World world, List<Text> list, TooltipContext par4) {
		GrappleCustomization custom = getCustomization(stack);
		
		if (Screen.hasShiftDown()) {
			if (!custom.detachonkeyrelease) {
				list.add(Text.literal(GrappleModKeyBindings.key_boththrow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.throw.desc").getString()));
				list.add(Text.literal(GrappleModKeyBindings.key_boththrow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.release.desc").getString()));
				list.add(Text.translatable("grappletooltip.double.desc").append(GrappleModKeyBindings.key_boththrow.getBoundKeyLocalizedText()).append(" ").append(Text.translatable("grappletooltip.releaseandthrow.desc")));
			} else {
				list.add(Text.literal(GrappleModKeyBindings.key_boththrow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.throwhold.desc").getString()));
			}
			list.add(Text.literal(GrappleModClient.get().getKeyname(MCKeys.keyBindForward) + ", " +
					GrappleModClient.get().getKeyname(MCKeys.keyBindLeft) + ", " +
					GrappleModClient.get().getKeyname(MCKeys.keyBindBack) + ", " +
					GrappleModClient.get().getKeyname(MCKeys.keyBindRight) +
					" " + Text.translatable("grappletooltip.swing.desc").getString()));
			list.add(Text.literal(GrappleModKeyBindings.key_jumpanddetach.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.jump.desc").getString()));
			list.add(Text.literal(GrappleModKeyBindings.key_slow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.slow.desc").getString()));
			list.add(Text.literal(GrappleModKeyBindings.key_climb.getBoundKeyLocalizedText().getString() + " + " + GrappleModClient.get().getKeyname(MCKeys.keyBindForward) + " / " +
					GrappleModKeyBindings.key_climbup.getBoundKeyLocalizedText().getString() +
					" " + Text.translatable("grappletooltip.climbup.desc").getString()));
			list.add(Text.literal(GrappleModKeyBindings.key_climb.getBoundKeyLocalizedText().getString() + " + " + GrappleModClient.get().getKeyname(MCKeys.keyBindBack) + " / " +
					GrappleModKeyBindings.key_climbdown.getBoundKeyLocalizedText().getString() +
					" " + Text.translatable("grappletooltip.climbdown.desc").getString()));
			if (custom.enderstaff) {
				list.add(Text.literal(GrappleModKeyBindings.key_enderlaunch.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.enderlaunch.desc").getString()));
			}
			if (custom.rocket) {
				list.add(Text.literal(GrappleModKeyBindings.key_rocket.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.rocket.desc").getString()));
			}
			if (custom.motor) {
				if (custom.motorwhencrouching && !custom.motorwhennotcrouching) {
					list.add(Text.literal(GrappleModKeyBindings.key_motoronoff.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.motoron.desc").getString()));
				}
				else if (!custom.motorwhencrouching && custom.motorwhennotcrouching) {
					list.add(Text.literal(GrappleModKeyBindings.key_motoronoff.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.motoroff.desc").getString()));
				}
			}
			if (custom.doublehook) {
				if (!custom.detachonkeyrelease) {
					list.add(Text.literal(GrappleModKeyBindings.key_leftthrow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.throwleft.desc").getString()));
					list.add(Text.literal(GrappleModKeyBindings.key_rightthrow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.throwright.desc").getString()));
				} else {
					list.add(Text.literal(GrappleModKeyBindings.key_leftthrow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.throwlefthold.desc").getString()));
					list.add(Text.literal(GrappleModKeyBindings.key_rightthrow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.throwrighthold.desc").getString()));
				}
			} else {
				list.add(Text.literal(GrappleModKeyBindings.key_rightthrow.getBoundKeyLocalizedText().getString() + " " + Text.translatable("grappletooltip.throwalt.desc").getString()));
			}
			if (custom.reelin) {
				list.add(Text.literal(GrappleModClient.get().getKeyname(MCKeys.keyBindSneak) + " " + Text.translatable("grappletooltip.reelin.desc").getString()));
			}
		} else {
			if (Screen.hasControlDown()) {
				for (String option : GrappleCustomization.booleanoptions) {
					if (custom.isOptionValid(option) && custom.getBoolean(option) != GrappleCustomization.DEFAULT.getBoolean(option)) {
						list.add(Text.literal((custom.getBoolean(option) ? "" : Text.translatable("grappletooltip.negate.desc").getString() + " ") + Text.translatable(custom.getName(option)).getString()));
					}
				}
				for (String option : GrappleCustomization.doubleoptions) {
					if (custom.isOptionValid(option) && (custom.getDouble(option) != GrappleCustomization.DEFAULT.getDouble(option))) {
						list.add(Text.translatable(custom.getName(option)).append(": " + Math.floor(custom.getDouble(option) * 100) / 100));
					}
				}
			} else {
				if (custom.doublehook) {
					list.add(Text.translatable(custom.getName("doublehook")));
				}
				if (custom.motor) {
					if (custom.smartmotor) {
						list.add(Text.translatable(custom.getName("smartmotor")));
					} else {
						list.add(Text.translatable(custom.getName("motor")));
					}
				}
				if (custom.enderstaff) {
					list.add(Text.translatable(custom.getName("enderstaff")));
				}
				if (custom.rocket) {
					list.add(Text.translatable(custom.getName("rocket")));
				}
				if (custom.attract) {
					list.add(Text.translatable(custom.getName("attract")));
				}
				if (custom.repel) {
					list.add(Text.translatable(custom.getName("repel")));
				}
				
				list.add(Text.literal(""));
				list.add(Text.translatable("grappletooltip.shiftcontrols.desc"));
				list.add(Text.translatable("grappletooltip.controlconfiguration.desc"));
			}
		}
	}

	public void setCustomOnServer(ItemStack helditemstack, GrappleCustomization custom) {
		NbtCompound tag = helditemstack.getOrCreateNbt();
		NbtCompound nbt = custom.writeNBT();
		
		tag.put("custom", nbt);
		
		helditemstack.setNbt(tag);
	}

	
	@Override
	public void onDroppedByPlayer(ItemStack item, PlayerEntity player) {
		int id = player.getId();
		GrappleModUtils.sendToCorrectClient(new GrappleDetachMessage(id), id, player.world);
		
		if (!player.world.isClient) {
			ServerControllerManager.attached.remove(id);
		}
		
		if (grapplehookEntitiesLeft.containsKey(player)) {
			GrapplehookEntity hookLeft = grapplehookEntitiesLeft.get(player);
			setHookEntityLeft(player, null);
			if (hookLeft != null) {
				hookLeft.removeServer();
			}
		}
		
		if (grapplehookEntitiesRight.containsKey(player)) {
			GrapplehookEntity hookRight = grapplehookEntitiesRight.get(player);
			setHookEntityLeft(player, null);
			if (hookRight != null) {
				hookRight.removeServer();
			}
		}
	}
	
	public boolean getPropertyRocket(ItemStack stack, World world, LivingEntity entity) {
		return this.getCustomization(stack).rocket;
	}

	public boolean getPropertyDouble(ItemStack stack, World world, LivingEntity entity) {
		return this.getCustomization(stack).doublehook;
	}

	public boolean getPropertyMotor(ItemStack stack, World world, LivingEntity entity) {
		return this.getCustomization(stack).motor;
	}

	public boolean getPropertySmart(ItemStack stack, World world, LivingEntity entity) {
		return this.getCustomization(stack).smartmotor;
	}

	public boolean getPropertyEnderstaff(ItemStack stack, World world, LivingEntity entity) {
		return this.getCustomization(stack).enderstaff;
	}

	public boolean getPropertyMagnet(ItemStack stack, World world, LivingEntity entity) {
		return this.getCustomization(stack).attract || this.getCustomization(stack).repel;
	}

	public boolean getPropertyHook(ItemStack stack, World world, LivingEntity entity) {
    	NbtCompound tag = stack.getOrCreateNbt();
    	return tag.contains("hook");
	}
}
