package com.yyon.grapplinghook.controller;

import com.yyon.grapplinghook.util.Vec;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public class ForcefieldController extends GrappleController {
	public ForcefieldController(int grapplehookEntityId, int entityId, World world, int id) {
		super(grapplehookEntityId, entityId, world, id, null);
		
		this.playerMovementMult = 1;
	}

	public void updatePlayerPos() {
		Entity entity = this.entity;
		
		if (this.attached) {
			if(entity != null) {
				this.normalGround(false);
				this.normalCollisions(false);
//					this.applyAirFriction();

				Vec playerpos = Vec.positionVec(entity);

//					double dist = oldspherevec.length();

				if (playerSneak) {
					motion.mult_ip(0.95);
				}
				applyPlayerMovement();

				Vec blockpush = checkRepel(playerpos, entity.world);
				blockpush.mult_ip(0.5);
				blockpush = new Vec(blockpush.x*0.5, blockpush.y*2, blockpush.z*0.5);
				this.motion.add_ip(blockpush);

				if (!entity.isOnGround()) {
					motion.add_ip(0, -0.05, 0);
				}

				motion.setMotion(this.entity);

				this.updateServerPos();
			}
		}
	}
}
