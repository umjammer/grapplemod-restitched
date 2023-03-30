package com.yyon.grapplinghook.network.serverbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.network.NetworkContext;
import com.yyon.grapplinghook.server.ServerControllerManager;
import java.util.HashSet;
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

public class GrappleEndMessage extends BaseMessageServer {
   
	public int entityId;
	public HashSet<Integer> hookEntityIds;

    public GrappleEndMessage(PacketByteBuf buf) {
    	super(buf);
    }

    public GrappleEndMessage(int entityId, HashSet<Integer> hookEntityIds) {
    	this.entityId = entityId;
    	this.hookEntityIds = hookEntityIds;
    }

	@Override
    public void decode(PacketByteBuf buf) {
    	this.entityId = buf.readInt();
    	int size = buf.readInt();
    	this.hookEntityIds = new HashSet<>();
    	for (int i = 0; i < size; i++) {
    		this.hookEntityIds.add(buf.readInt());
    	}
    }

	@Override
    public void encode(PacketByteBuf buf) {
    	buf.writeInt(this.entityId);
    	buf.writeInt(this.hookEntityIds.size());
    	for (int id : this.hookEntityIds) {
        	buf.writeInt(id);
    	}
    }

	@Override
	public Identifier getChannel() {
		return GrappleMod.id("grapple_end");
	}

	@Override
    public void processMessage(NetworkContext ctx) {
		int id = this.entityId;
		ServerPlayerEntity player = ctx.getSender();

		ctx.getServer().execute(() -> {
			if (player == null) return;
			ServerControllerManager.receiveGrappleEnd(id, player.world, this.hookEntityIds);
		});
    }
}
