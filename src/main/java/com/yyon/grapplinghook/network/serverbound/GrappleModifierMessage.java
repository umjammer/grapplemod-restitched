package com.yyon.grapplinghook.network.serverbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.blockentity.GrappleModifierBlockEntity;
import com.yyon.grapplinghook.network.NetworkContext;
import com.yyon.grapplinghook.util.GrappleCustomization;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/*
    GrappleMod is free software: you can redistribute it and/or modify
    it under the teHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with GrappleMod.  If not, see <http://www.gnu.org/licenses/>.
 */

public class GrappleModifierMessage extends BaseMessageServer {
   
	public BlockPos pos;
	public GrappleCustomization custom;

    public GrappleModifierMessage(BlockPos pos, GrappleCustomization custom) {
    	this.pos = pos;
    	this.custom = custom;
    }

	public GrappleModifierMessage(PacketByteBuf buf) {
		super(buf);
	}

	@Override
    public void decode(PacketByteBuf buf) {
    	this.pos = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
    	this.custom = new GrappleCustomization();
    	this.custom.readFromBuf(buf);
    }

	@Override
    public void encode(PacketByteBuf buf) {
    	buf.writeInt(this.pos.getX());
    	buf.writeInt(this.pos.getY());
    	buf.writeInt(this.pos.getZ());
    	this.custom.writeToBuf(buf);
    }

	@Override
	public Identifier getChannel() {
		return GrappleMod.id("grapple_modifier");
	}

	@Override
    public void processMessage(NetworkContext ctx) {
		// Block Entities must be obtained on the main thread.
		ctx.getServer().execute(() -> {
			World w = ctx.getSender().getWorld();
			BlockEntity ent = w.getBlockEntity(this.pos);

			if (ent instanceof GrappleModifierBlockEntity e) {
				e.setCustomizationServer(this.custom);
				return;
			}

			GrappleMod.LOGGER.warn("Wrong type! is null: %s, pos: %s, isClient: %s".formatted(ent == null, this.pos, ctx.getSender().world.isClient));
		});
	}
}
