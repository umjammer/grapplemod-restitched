package com.yyon.grapplinghook.network.clientbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.entity.grapplehook.GrapplehookEntity;
import com.yyon.grapplinghook.entity.grapplehook.SegmentHandler;
import com.yyon.grapplinghook.network.NetworkContext;
import com.yyon.grapplinghook.network.clientbound.BaseMessageClient;
import com.yyon.grapplinghook.util.GrappleCustomization;
import com.yyon.grapplinghook.util.Vec;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import java.util.LinkedList;

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

public class GrappleAttachMessage extends BaseMessageClient {
   
	public int id;
	public double x;
	public double y;
	public double z;
	public int controlId;
	public int entityId;
	public BlockPos blockPos;
	public LinkedList<Vec> segments;
	public LinkedList<Direction> segmentTopSides;
	public LinkedList<Direction> segmentBottomSides;
	public GrappleCustomization custom;

    public GrappleAttachMessage(PacketByteBuf buf) {
    	super(buf);
    }

    public GrappleAttachMessage(int id, double x, double y, double z, int controlid, int entityid, BlockPos blockpos, LinkedList<Vec> segments, LinkedList<Direction> segmenttopsides, LinkedList<Direction> segmentbottomsides, GrappleCustomization custom) {
    	this.id = id;
        this.x = x;
        this.y = y;
        this.z = z;
        this.controlId = controlid;
        this.entityId = entityid;
        this.blockPos = blockpos;
        this.segments = segments;
        this.segmentTopSides = segmenttopsides;
        this.segmentBottomSides = segmentbottomsides;
        this.custom = custom;
    }

    @Override
    public void decode(PacketByteBuf buf) {
    	this.id = buf.readInt();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
        this.controlId = buf.readInt();
        this.entityId = buf.readInt();
        int blockx = buf.readInt();
        int blocky = buf.readInt();
        int blockz = buf.readInt();
        this.blockPos = new BlockPos(blockx, blocky, blockz);
        
        this.custom = new GrappleCustomization();
        this.custom.readFromBuf(buf);
        
        int size = buf.readInt();
        this.segments = new LinkedList<>();
        this.segmentBottomSides = new LinkedList<>();
        this.segmentTopSides = new LinkedList<>();

		segments.add(new Vec(0, 0, 0));
		segmentBottomSides.add(null);
		segmentTopSides.add(null);
		
		for (int i = 1; i < size-1; i++) {
        	this.segments.add(new Vec(buf.readDouble(), buf.readDouble(), buf.readDouble()));
        	this.segmentBottomSides.add(buf.readEnumConstant(Direction.class));
        	this.segmentTopSides.add(buf.readEnumConstant(Direction.class));
        }
		
		segments.add(new Vec(0, 0, 0));
		segmentBottomSides.add(null);
		segmentTopSides.add(null);
    }

    @Override
    public void encode(PacketByteBuf buf) {
    	buf.writeInt(this.id);
        buf.writeDouble(this.x);
        buf.writeDouble(this.y);
        buf.writeDouble(this.z);
        buf.writeInt(this.controlId);
        buf.writeInt(this.entityId);
        buf.writeInt(this.blockPos.getX());
        buf.writeInt(this.blockPos.getY());
        buf.writeInt(this.blockPos.getZ());
        
        this.custom.writeToBuf(buf);
        
        buf.writeInt(this.segments.size());
        for (int i = 1; i < this.segments.size()-1; i++) {
        	buf.writeDouble(this.segments.get(i).x);
        	buf.writeDouble(this.segments.get(i).y);
        	buf.writeDouble(this.segments.get(i).z);
        	buf.writeEnumConstant(this.segmentBottomSides.get(i));
        	buf.writeEnumConstant(this.segmentTopSides.get(i));
        }
    }

    @Override
    public Identifier getChannel() {
        return GrappleMod.id("grapple_attach");
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void processMessage(NetworkContext ctx) {
		World world = MinecraftClient.getInstance().world;

        if(world == null) {
            GrappleMod.LOGGER.warn("Network Message received in invalid context (World not present | GrappleAttach)");
            return;
        }

    	if (world.getEntityById(this.id) instanceof GrapplehookEntity grapple) {

        	grapple.clientAttach(this.x, this.y, this.z);
        	SegmentHandler segmenthandler = grapple.segmentHandler;
        	segmenthandler.segments = this.segments;
        	segmenthandler.segmentBottomSides = this.segmentBottomSides;
        	segmenthandler.segmentTopSides = this.segmentTopSides;
        	
        	Entity holder = world.getEntityById(this.entityId);

            if(holder == null) {
                GrappleMod.LOGGER.warn("Network Message received in invalid context (Holder does not exist | GrappleAttach)");
                return;
            }

        	segmenthandler.forceSetPos(new Vec(this.x, this.y, this.z), Vec.positionVec(holder));
    	}
    	            	
    	GrappleModClient.get().createControl(this.controlId, this.id, this.entityId, world, new Vec(this.x, this.y, this.z), this.blockPos, this.custom);
    }
}
