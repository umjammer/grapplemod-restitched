package com.yyon.grapplinghook.network.serverbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.network.LogicalSide;
import com.yyon.grapplinghook.network.NetworkContext;
import java.util.function.Supplier;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public abstract class BaseMessageServer {
	public BaseMessageServer(PacketByteBuf buf) {
		this.decode(buf);
	}
	
	public BaseMessageServer() {
	}
	
	public abstract void decode(PacketByteBuf buf);
	
	public abstract void encode(PacketByteBuf buf);

    public abstract Identifier getChannel();

    public abstract void processMessage(NetworkContext ctx);
    
    public void onMessageReceived(Supplier<NetworkContext> ctxSupplier) {
        NetworkContext ctx = ctxSupplier.get();
        LogicalSide sideReceived = ctx.getReceptionSide();
        if (sideReceived != LogicalSide.FOR_SERVER) {
			GrappleMod.LOGGER.warn("message received on wrong side:" + ctx.getReceptionSide());
			return;
        }
        
        ctx.handle();
        
        final ServerPlayerEntity sendingPlayer = ctx.getSender();
        if (sendingPlayer == null) {
        	GrappleMod.LOGGER.warn("EntityPlayerMP was null when message was received");
        }

        ctx.getServer().execute(() -> processMessage(ctx));
    }
}
