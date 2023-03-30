package com.yyon.grapplinghook.network.serverbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.network.NetworkContext;
import com.yyon.grapplinghook.network.serverbound.BaseMessageServer;
import com.yyon.grapplinghook.util.Vec;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

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

public class PlayerMovementMessage extends BaseMessageServer {
   
	public int entityId;
	public double x;
	public double y;
	public double z;
	public double mx;
	public double my;
	public double mz;
	
	public PlayerMovementMessage(PacketByteBuf buf) {
		super(buf);
	}
	
    public PlayerMovementMessage(int entityId, double x, double y, double z, double mx, double my, double mz) {
    	this.entityId = entityId;
    	this.x = x;
    	this.y = y;
    	this.z = z;
    	this.mx = mx;
    	this.my = my;
    	this.mz = mz;
    }

	@Override
    public void decode(PacketByteBuf buf) {
    	try {
	    	this.entityId = buf.readInt();
	    	this.x = buf.readDouble();
	    	this.y = buf.readDouble();
	    	this.z = buf.readDouble();
	    	this.mx = buf.readDouble();
	    	this.my = buf.readDouble();
	    	this.mz = buf.readDouble();
    	} catch (Exception e) {
    		System.out.print("Playermovement error: ");
    		GrappleMod.LOGGER.warn(buf);
    	}
    }

	@Override
    public void encode(PacketByteBuf buf) {
        buf.writeInt(entityId);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
        buf.writeDouble(mx);
        buf.writeDouble(my);
        buf.writeDouble(mz);
        
    }

	@Override
	public Identifier getChannel() {
		return GrappleMod.id("player_movement");
	}

	@Override
    public void processMessage(NetworkContext ctx) {
    	final ServerPlayerEntity referencedPlayer = ctx.getSender();

		ctx.getServer().execute(() -> {
			if(referencedPlayer.getId() == this.entityId) {
				new Vec(this.x, this.y, this.z).setPos(referencedPlayer);
				new Vec(this.mx, this.my, this.mz).setMotion(referencedPlayer);

				referencedPlayer.networkHandler.syncWithPlayerPosition();

				if (!referencedPlayer.isOnGround()) {
					if (this.my >= 0) {
						referencedPlayer.fallDistance = 0;
					} else {
						double gravity = 0.05 * 2;
						// d = v^2 / 2g
						referencedPlayer.fallDistance = (float) (Math.pow(this.my, 2) / (2 * gravity));
					}
				}
			}
		});
    }
}
