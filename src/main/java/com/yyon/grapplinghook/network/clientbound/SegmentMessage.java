package com.yyon.grapplinghook.network.clientbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.entity.grapplehook.GrapplehookEntity;
import com.yyon.grapplinghook.entity.grapplehook.SegmentHandler;
import com.yyon.grapplinghook.network.NetworkContext;
import com.yyon.grapplinghook.network.clientbound.BaseMessageClient;
import com.yyon.grapplinghook.util.Vec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
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

public class SegmentMessage extends BaseMessageClient {
   
	public int id;
	public boolean add;
	public int index;
	public Vec pos;
	public Direction topFacing;
	public Direction bottomFacing;

    public SegmentMessage(PacketByteBuf buf) {
    	super(buf);
    }

    public SegmentMessage(int id, boolean add, int index, Vec pos, Direction topfacing, Direction bottomfacing) {
    	this.id = id;
    	this.add = add;
    	this.index = index;
    	this.pos = pos;
    	this.topFacing = topfacing;
    	this.bottomFacing = bottomfacing;
    }

	@Override
    public void decode(PacketByteBuf buf) {
    	this.id = buf.readInt();
    	this.add = buf.readBoolean();
    	this.index = buf.readInt();
    	this.pos = new Vec(buf.readDouble(), buf.readDouble(), buf.readDouble());
    	this.topFacing = buf.readEnumConstant(Direction.class);
    	this.bottomFacing = buf.readEnumConstant(Direction.class);
    }

	@Override
    public void encode(PacketByteBuf buf) {
    	buf.writeInt(this.id);
    	buf.writeBoolean(this.add);
    	buf.writeInt(this.index);
    	buf.writeDouble(pos.x);
    	buf.writeDouble(pos.y);
    	buf.writeDouble(pos.z);
    	buf.writeEnumConstant(this.topFacing);
    	buf.writeEnumConstant(this.bottomFacing);
    }

	@Override
	public Identifier getChannel() {
		return GrappleMod.id("segment");
	}

    @Environment(EnvType.CLIENT)
	@Override
    public void processMessage(NetworkContext ctx) {
    	World world = MinecraftClient.getInstance().world;
    	Entity grapple = world.getEntityById(this.id);
    	if (grapple == null) {
    		return;
    	}
    	
    	if (grapple instanceof GrapplehookEntity) {
    		SegmentHandler segmenthandler = ((GrapplehookEntity) grapple).segmentHandler;
    		if (this.add) {
    			segmenthandler.actuallyAddSegment(this.index, this.pos, this.bottomFacing, this.topFacing);
    		} else {
    			segmenthandler.removeSegment(this.index);
    		}
    	}
    }
}
