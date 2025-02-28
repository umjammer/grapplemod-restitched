package com.yyon.grapplinghook.network.clientbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.ClientControllerManager;
import com.yyon.grapplinghook.network.NetworkContext;
import com.yyon.grapplinghook.network.clientbound.BaseMessageClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;
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

public class GrappleDetachMessage extends BaseMessageClient {
   
	public int id;

    public GrappleDetachMessage(PacketByteBuf buf) {
    	super(buf);
    }

    public GrappleDetachMessage(int id) {
    	this.id = id;
    }

    @Override
    public void decode(PacketByteBuf buf) {
    	this.id = buf.readInt();
    }

    @Override
    public void encode(PacketByteBuf buf) {
    	buf.writeInt(this.id);
    }

    @Override
    public Identifier getChannel() {
        return GrappleMod.id("grapple_detach");
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void processMessage(NetworkContext ctx) {
    	ClientControllerManager.receiveGrappleDetach(this.id);
    }
}
