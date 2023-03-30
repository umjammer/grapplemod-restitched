package com.yyon.grapplinghook.entity.grapplehook;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.config.GrappleConfigUtils;
import com.yyon.grapplinghook.network.NetworkManager;
import com.yyon.grapplinghook.network.clientbound.GrappleAttachMessage;
import com.yyon.grapplinghook.network.clientbound.GrappleAttachPosMessage;
import com.yyon.grapplinghook.registry.GrappleModEntities;
import com.yyon.grapplinghook.registry.GrappleModItems;
import com.yyon.grapplinghook.server.ServerControllerManager;
import com.yyon.grapplinghook.util.GrappleCustomization;
import com.yyon.grapplinghook.util.GrappleModUtils;
import com.yyon.grapplinghook.util.Vec;
import java.util.HashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

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

public class GrapplehookEntity extends ThrownItemEntity implements IExtendedSpawnPacketEntity {

	public GrapplehookEntity(EntityType<? extends GrapplehookEntity> type, World world) {
		super(type, world);

		this.segmentHandler = new SegmentHandler(this.world, this, Vec.positionVec(this), Vec.positionVec(this));
		this.customization = new GrappleCustomization();
	}

	public GrapplehookEntity(World world, LivingEntity shooter, boolean righthand, GrappleCustomization customization, boolean isdouble) {
		super(GrappleModEntities.GRAPPLE_HOOK.get(), shooter.getPos().x, shooter.getPos().y + shooter.getStandingEyeHeight(), shooter.getPos().z, world);
		
		this.shootingEntity = shooter;
		this.shootingEntityID = this.shootingEntity.getId();
		
		this.isDouble = isdouble;
		
		Vec pos = Vec.positionVec(this.shootingEntity).add(new Vec(0, this.shootingEntity.getStandingEyeHeight(), 0));

		this.segmentHandler = new SegmentHandler(this.world, this, new Vec(pos), new Vec(pos));

		this.customization = customization;
		this.r = customization.maxlen;
		
		this.rightHand = righthand;
	}

	public Entity shootingEntity = null;
	public int shootingEntityID;
	
	private boolean firstAttach = false;
	public Vec thisPos;
	
	public boolean rightHand = true;
	
	public boolean attached = false;
	
	public double pull;
	
	public double taut = 1;
	
	public boolean ignoreFrustumCheck = true;
	
	public boolean isDouble = false;
	
	public double r;
	
	public SegmentHandler segmentHandler = null;
	
	public GrappleCustomization customization = null;
	
	// magnet attract
	public Vec prevPos = null;
	public boolean foundBlock = false;
	public boolean wasInAir = false;
	public BlockPos magnetBlock = null;
	
	public Vec attach_dir = null;

	@Override
    public void writeSpawnData(PacketByteBuf data) {
	    data.writeInt(this.shootingEntity != null ? this.shootingEntity.getId() : 0);
	    data.writeBoolean(this.rightHand);
	    data.writeBoolean(this.isDouble);
	    if (this.customization == null) {
	    	GrappleMod.LOGGER.warn("error: customization null");
	    }
	    this.customization.writeToBuf(data);
    }
	
	@Override
    public void readSpawnData(PacketByteBuf data) {
    	this.shootingEntityID = data.readInt();
	    this.shootingEntity = this.world.getEntityById(this.shootingEntityID);
	    this.rightHand = data.readBoolean();
	    this.isDouble = data.readBoolean();
	    this.customization = new GrappleCustomization();
	    this.customization.readFromBuf(data);
    }

	@Override
	public void initDataTracker() {
		super.initDataTracker();
	}
	
	public void removeServer() {
		this.remove(RemovalReason.DISCARDED);
		this.shootingEntityID = 0;
	}

	public float getVelocity_() {
        return (float) this.customization.throwspeed;
    }
	
	@Override
	public void tick() {
		if (this.shootingEntityID == 0 || this.shootingEntity == null) { // removes ghost grappling hooks
			this.remove(RemovalReason.DISCARDED);
		}
		
		if (this.firstAttach) {
			this.setVelocity(0, 0, 0);
			this.firstAttach = false;
			super.setPosition(this.thisPos.x, this.thisPos.y, this.thisPos.z);
		}
		
		if (this.attached) {
			this.setVelocity(0, 0, 0);
		}
		
		super.tick();
		
		if (!this.world.isClient) {
			if (this.shootingEntity != null)  {
				if (!this.attached) {
					if (this.segmentHandler.hookPastBend(this.r)) {
						Vec farthest = this.segmentHandler.getFarthest();
						this.serverAttach(this.segmentHandler.getBendBlock(1), farthest, null);
					}
					
					if (!this.customization.phaserope) {
						this.segmentHandler.update(Vec.positionVec(this), Vec.positionVec(this.shootingEntity).add(new Vec(0, this.shootingEntity.getStandingEyeHeight(), 0)), this.r, true);
						
						if (this.customization.sticky) {
							if (this.segmentHandler.segments.size() > 2) {
								int bendnumber = this.segmentHandler.segments.size() - 2;
								Vec closest = this.segmentHandler.segments.get(bendnumber);
								BlockPos blockpos = this.segmentHandler.getBendBlock(bendnumber);
								for (int i = 1; i <= bendnumber; i++) { 
									this.segmentHandler.removeSegment(1);
								}
								this.serverAttach(blockpos, closest, null);
							}
						}
					} else {
						this.segmentHandler.updatePos(Vec.positionVec(this), Vec.positionVec(this.shootingEntity).add(new Vec(0, this.shootingEntity.getStandingEyeHeight(), 0)), this.r);
					}
					
					Vec farthest = this.segmentHandler.getFarthest();
					double distToFarthest = this.segmentHandler.getDistToFarthest();
					
					Vec ropevec = Vec.positionVec(this).sub(farthest);
					double d = ropevec.length();
					
					if (this.customization.reelin && this.shootingEntity.isInSneakingPose()) {
						double newdist = d + distToFarthest - 0.4;
						if (newdist > 1 && newdist <= this.customization.maxlen) {
							this.r = newdist;
						}
					}


					if (d + distToFarthest > this.r) {
						Vec motion = Vec.motionVec(this);
						
						if (motion.dot(ropevec) > 0) {
							motion = motion.removeAlong(ropevec);
						}
						
						this.setVelocityActually(motion.x, motion.y, motion.z);
						
						ropevec.changeLen_ip(this.r - distToFarthest);
						Vec newpos = ropevec.add(farthest);
						
						this.setPosition(newpos.x, newpos.y, newpos.z);
					}
					
				}
			}
		}
		
		// magnet attraction
		if (!this.attached && this.customization.attract && Vec.positionVec(this).sub(Vec.positionVec(this.shootingEntity)).length() > this.customization.attractradius) {
	    	if (this.shootingEntity == null) {return;}
	    	if (!this.foundBlock) {
	    		if (!this.world.isClient) {
	    			Vec playerpos = Vec.positionVec(this.shootingEntity);
	    			Vec pos = Vec.positionVec(this);
	    			if (magnetBlock == null) {
		    			if (prevPos != null) {
			    			HashMap<BlockPos, Boolean> checkedset = new HashMap<>();
			    			Vec vector = pos.sub(prevPos);
			    			if (vector.length() > 0) {
				    			Vec normvector = vector.normalize();
				    			for (int i = 0; i < vector.length(); i++) {
				    				double dist = prevPos.sub(playerpos).length();
				    				int radius = (int) dist / 4;
				    				BlockPos found = this.check(prevPos, checkedset);
				    				if (found != null) {
//				    					if (wasinair) {
								    		Vec distvec = new Vec(found.getX(), found.getY(), found.getZ());
								    		distvec.sub_ip(prevPos);
								    		if (distvec.length() < radius) {
						    					this.setPos(prevPos.x, prevPos.y, prevPos.z);
						    					pos = prevPos;
						    					
						    					magnetBlock = found;
						    					
						    					break;
								    		}
//				    					}
				    				} else {
				    					wasInAir = true;
				    				}
				    				
				    				prevPos.add_ip(normvector);
				    			}
			    			}
		    			}
	    			}
	    			
	    			if (magnetBlock != null) {
				    	BlockState blockstate = this.world.getBlockState(magnetBlock);
				    	VoxelShape BB = blockstate.getCollisionShape(this.world, magnetBlock);

						Vec blockvec = new Vec(magnetBlock.getX() + (BB.getMax(Axis.X) + BB.getMin(Axis.X)) / 2, magnetBlock.getY() + (BB.getMax(Axis.Y) + BB.getMin(Axis.Y)) / 2, magnetBlock.getZ() + (BB.getMax(Axis.Z) + BB.getMin(Axis.Z)) / 2);
						Vec newvel = blockvec.sub(pos);
						
						double l = newvel.length();
						
						newvel.changeLen(this.getVelocity_());
						
						this.setVelocity(newvel.x, newvel.y, newvel.z);
						
						if (l < 0.2) {
							this.serverAttach(magnetBlock, blockvec, Direction.UP);
						}
	    			}
	    			
	    			prevPos = pos;
	    		}
	    	}
		}
	}
	
	public void setVelocityActually(double x, double y, double z) {
		this.setVelocity(x, y, z);

        if (this.prevPitch == 0.0F && this.prevYaw == 0.0F)
        {
            double f = Math.sqrt(x * x + z * z);
            this.setYaw((float)(MathHelper.atan2(x, z) * (180D / Math.PI)));
            this.setPitch((float)(MathHelper.atan2(y, f) * (180D / Math.PI)));
            this.prevYaw = this.getYaw();
            this.prevPitch = this.getPitch();
        }
	}

	@Override
	public boolean shouldRender(double p_70112_1_) {
		return true;
	}

	@Override
	public boolean shouldRender(double p_145770_1_, double p_145770_3_, double p_145770_5_) {
		return true;
	}
	
	@Override
	public Box getVisibilityBoundingBox() {
		if (this.shootingEntity == null) {
			return super.getVisibilityBoundingBox();
		}
		return this.segmentHandler.getBoundingBox(Vec.positionVec(this), Vec.positionVec(this.shootingEntity).add(new Vec(0, this.shootingEntity.getStandingEyeHeight(), 0)));
	}

	@Override
	protected void onCollision(HitResult movingobjectposition) {
		if (!this.world.isClient) {
			if (this.attached) {
				return;
			}
			if (this.shootingEntity == null || this.shootingEntityID == 0) {
				return;
			}
			if (movingobjectposition == null) {
				return;
			}
			
			Vec vec3d = Vec.positionVec(this);
	        Vec vec3d1 = vec3d.add(Vec.motionVec(this));

			if (movingobjectposition instanceof EntityHitResult && !GrappleConfig.getConf().grapplinghook.other.hookaffectsentities) {
				onCollision(GrappleModUtils.rayTraceBlocks(this, this.world, vec3d, vec3d1));
		        return;
			}
			
			BlockHitResult blockhit = null;
			if (movingobjectposition instanceof BlockHitResult) {
				blockhit = (BlockHitResult) movingobjectposition;
			}
			
			if (blockhit != null) {
				BlockPos blockpos = blockhit.getBlockPos();
				Block block = this.world.getBlockState(blockpos).getBlock();
				if (GrappleConfigUtils.breaksBlock(block)) {
					this.world.breakBlock(blockpos, true);
					onCollision(GrappleModUtils.rayTraceBlocks(this, this.world, vec3d, vec3d1));
					return;
				}
			}
			
			if (movingobjectposition instanceof EntityHitResult entityHit) {
				// hit entity
				Entity entity = entityHit.getEntity();
				if (entity == this.shootingEntity) {
					return;
				}
				
				Vec playerpos = Vec.positionVec(this.shootingEntity);
				Vec entitypos = Vec.positionVec(entity);
				Vec yank = playerpos.sub(entitypos).mult(0.4);
				yank.y = Math.min(yank.y, 2);
				Vec newmotion = Vec.motionVec(entity).add(yank);
				entity.setVelocity(newmotion.toVec3d());
				
				this.removeServer();

			} else if (blockhit != null) {
				BlockPos blockpos = blockhit.getBlockPos();
				
				Vec vec3 = new Vec(movingobjectposition.getPos());

				this.serverAttach(blockpos, vec3, blockhit.getSide());
			} else {
				GrappleMod.LOGGER.warn("unknown impact?");
			}
		}
	}

	@Override
	protected Item getDefaultItem() {
		return GrappleModItems.GRAPPLING_HOOK.get();
	}
	
	public void serverAttach(BlockPos blockpos, Vec pos, Direction sideHit) {
		if (this.attached) {
			return;
		}
		if (this.shootingEntity == null || this.shootingEntityID == 0) {
			return;
		}
		this.attached = true;
		
		if (blockpos != null) {
			Block block = this.world.getBlockState(blockpos).getBlock();

			if (!GrappleConfigUtils.attachesBlock(block)) {
				this.removeServer();
				return;
			}
		}
		
		Vec vec3 = Vec.positionVec(this);
		vec3.add_ip(Vec.motionVec(this));
		if (pos != null) {
            vec3 = pos;
            
            this.setPos(vec3.x, vec3.y, vec3.z);
		}
		
		//west -x
		//north -z
		Vec curpos = Vec.positionVec(this);
		if (sideHit == Direction.DOWN) {
			curpos.y -= 0.3;
		} else if (sideHit == Direction.WEST) {
			curpos.x -= 0.05;
		} else if (sideHit == Direction.NORTH) {
			curpos.z -= 0.05;
		} else if (sideHit == Direction.SOUTH) {
			curpos.z += 0.05;
		} else if (sideHit == Direction.EAST) {
			curpos.x += 0.05;
		} else if (sideHit == Direction.UP) {
			curpos.y += 0.05;
		}
		curpos.setPos(this);
		
		this.setVelocity(0, 0, 0);
        
        this.thisPos = Vec.positionVec(this);
		this.firstAttach = true;
		ServerControllerManager.attached.add(this.shootingEntityID);
		
		GrappleModUtils.sendToCorrectClient(new GrappleAttachMessage(this.getId(), this.getPos().x, this.getPos().y, this.getPos().z, this.getControlId(), this.shootingEntityID, blockpos, this.segmentHandler.segments, this.segmentHandler.segmentTopSides, this.segmentHandler.segmentBottomSides, this.customization), this.shootingEntityID, this.world);
		
		GrappleAttachPosMessage msg = new GrappleAttachPosMessage(this.getId(), this.getPos().x, this.getPos().y, this.getPos().z);
		NetworkManager.packetToClient(msg, GrappleModUtils.getChunkPlayers((ServerWorld) this.world, new Vec(this.getPos())));
	}
	
	public void clientAttach(double x, double y, double z) {
		this.setAttachPos(x, y, z);
		
		if (this.shootingEntity instanceof PlayerEntity) {
			GrappleModClient.get().resetLauncherTime(this.shootingEntityID);
		}
	}
	
	
	@Override
	protected float getGravity() {
		if (this.attached) {
			return 0.0F;
		}
        return (float) this.customization.hookgravity * 0.1F;
    }
	
	public int getControlId() {
		return GrappleModUtils.GRAPPLE_ID;
	}

	public void setAttachPos(double x, double y, double z) {
		this.setPos(x, y, z);

		this.setVelocity(0, 0, 0);
		this.firstAttach = true;
		this.attached = true;
        this.thisPos = new Vec(x, y, z);
	}
	
	// used for magnet attraction
    public BlockPos check(Vec p, HashMap<BlockPos, Boolean> checkedset) {
    	int radius = (int) Math.floor(this.customization.attractradius);
    	BlockPos closestpos = null;
    	double closestdist = 0;
    	for (int x = (int)p.x - radius; x <= (int)p.x + radius; x++) {
        	for (int y = (int)p.y - radius; y <= (int)p.y + radius; y++) {
            	for (int z = (int)p.z - radius; z <= (int)p.z + radius; z++) {
			    	BlockPos pos = new BlockPos(x, y, z);
					if (hasBlock(pos, checkedset)) {
						Vec distvec = new Vec(pos.getX(), pos.getY(), pos.getZ());
						distvec.sub_ip(p);
						double dist = distvec.length();
						if (closestpos == null || dist < closestdist) {
							closestpos = pos;
							closestdist = dist;
						}
					}
				}
	    	}
    	}
		return closestpos;
	}

	// used for magnet attraction
	public boolean hasBlock(BlockPos pos, HashMap<BlockPos, Boolean> checkedset) {
    	if (!checkedset.containsKey(pos)) {
    		boolean isblock = false;
	    	BlockState blockstate = this.world.getBlockState(pos);
	    	Block b = blockstate.getBlock();
			if (GrappleConfigUtils.attachesBlock(b)) {
		    	if (!(blockstate.isAir())) {
			    	VoxelShape BB = blockstate.getCollisionShape(this.world, pos);
			    	if (!BB.isEmpty()) {
			    		isblock = true;
			    	}
		    	}
			}
			
	    	checkedset.put(pos, isblock);
	    	return isblock;
    	} else {
    		return checkedset.get(pos);
    	}
	}

	@Override
	public ItemStack getStack() {
		return new ItemStack(this.getDefaultItem());
	}

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket() {
		return new EntitySpawnS2CPacket(this);
	}
}
