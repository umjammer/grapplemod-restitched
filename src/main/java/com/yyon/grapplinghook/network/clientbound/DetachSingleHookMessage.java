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

public class DetachSingleHookMessage extends BaseMessageClient {
   
	public int id;
	public int hookid;

    public DetachSingleHookMessage(PacketByteBuf buf) {
    	super(buf);
    }

    public DetachSingleHookMessage(int id, int hookid) {
    	this.id = id;
    	this.hookid = hookid;
    }

    @Override
    public void decode(PacketByteBuf buf) {
    	this.id = buf.readInt();
    	this.hookid = buf.readInt();
    }

    @Override
    public void encode(PacketByteBuf buf) {
    	buf.writeInt(this.id);
    	buf.writeInt(this.hookid);
    }

    @Override
    public Identifier getChannel() {
        return GrappleMod.id("detach_single_hook");
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void processMessage(NetworkContext ctx) {
    	ClientControllerManager.receiveGrappleDetachHook(this.id, this.hookid);
    }
}
