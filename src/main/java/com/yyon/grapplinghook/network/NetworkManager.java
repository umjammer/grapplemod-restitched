package com.yyon.grapplinghook.network;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.network.clientbound.*;
import com.yyon.grapplinghook.network.serverbound.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import java.util.function.Function;

public class NetworkManager {

    protected static ClientPlayNetworking.PlayChannelHandler generateClientPacketHandler(Function<PacketByteBuf, BaseMessageClient> packetFactory) {
        return (client, handler, buf, responseSender) -> {
            BaseMessageClient packet = packetFactory.apply(buf);
            NetworkContext context = new NetworkContext()
                    .setDestination(LogicalSide.FOR_CLIENT)
                    .setClient(client)
                    .setClientHandle(handler)
                    .setRespond(responseSender);

            packet.processMessage(context);
        };
    }

    protected static ServerPlayNetworking.PlayChannelHandler generateServerPacketHandler(Function<PacketByteBuf, BaseMessageServer> packetFactory) {
        return (server, player, handler, buf, responseSender) -> {
            BaseMessageServer packet = packetFactory.apply(buf);
            NetworkContext context = new NetworkContext()
                    .setDestination(LogicalSide.FOR_SERVER)
                    .setServer(server)
                    .setServerHandle(handler)
                    .setSender(player)
                    .setRespond(responseSender);

            packet.processMessage(context);
        };
    }


    public static void registerClient(String channelId, Function<PacketByteBuf, BaseMessageClient> etc) {
        ClientPlayNetworking.registerGlobalReceiver(GrappleMod.id(channelId), NetworkManager.generateClientPacketHandler(etc));
    }

    public static void registerServer(String channelId, Function<PacketByteBuf, BaseMessageServer> etc) {
        ServerPlayNetworking.registerGlobalReceiver(GrappleMod.id(channelId), NetworkManager.generateServerPacketHandler(etc));
    }

    public static void packetToServer(BaseMessageServer server) {
        PacketByteBuf buf = PacketByteBufs.create();
        server.encode(buf);
        ClientPlayNetworking.send(server.getChannel(), buf);
    }

    public static void packetToClient(BaseMessageClient client, ServerPlayerEntity... players) {
        if(players.length == 0) {
            GrappleMod.LOGGER.warn("Missing any players to send a packet to!");
            return;
        }

        PacketByteBuf buf = PacketByteBufs.create();
        client.encode(buf);

        for(ServerPlayerEntity player: players)
            ServerPlayNetworking.send(player, client.getChannel(), buf);
    }

    public static void registerClientPacketListeners() {
        NetworkManager.registerClient("data", AddExtraDataMessage::new);
        NetworkManager.registerClient("detach_single_hook", DetachSingleHookMessage::new);
        NetworkManager.registerClient("grapple_attach", GrappleAttachMessage::new);
        NetworkManager.registerClient("grapple_attach_pos", GrappleAttachPosMessage::new);
        NetworkManager.registerClient("grapple_detach", GrappleDetachMessage::new);
        NetworkManager.registerClient("logged_in", LoggedInMessage::new);
        NetworkManager.registerClient("segment", SegmentMessage::new);
    }

    public static void registerPacketListeners() {
        NetworkManager.registerServer("grapple_end", GrappleEndMessage::new);
        NetworkManager.registerServer("grapple_modifier", GrappleModifierMessage::new);
        NetworkManager.registerServer("keypress", KeypressMessage::new);
        NetworkManager.registerServer("player_movement", PlayerMovementMessage::new);
    }
}
