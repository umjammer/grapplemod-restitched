package com.yyon.grapplinghook.network;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public final class NetworkContext {

    // Server
    private MinecraftServer server = null;
    private ServerPlayerEntity sender = null;
    private ServerPlayNetworkHandler serverHandle = null;

    // Client
    private MinecraftClient client = null;
    private ClientPlayNetworkHandler clientHandle = null;

    // Read Only
    private PacketSender respond = null;
    private LogicalSide destination = LogicalSide.NONE;
    private boolean packetHandled = false;



    // Server
    public MinecraftServer getServer() {
        return server;
    }

    public ServerPlayerEntity getSender() {
        return this.sender;
    }

    public ServerPlayNetworkHandler getServerHandle() {
        return serverHandle;
    }


    // Client
    public MinecraftClient getClient() {
        return client;
    }

    public ClientPlayNetworkHandler getClientHandle() {
        return clientHandle;
    }


    // Read Only
    public PacketSender respond() {
        return respond;
    }

    public LogicalSide getReceptionSide() {
        return this.destination;
    }

    public boolean isPacketHandled() {
        return packetHandled;
    }



    NetworkContext setSender(ServerPlayerEntity sender) {
        this.sender = sender;
        return this;
    }

    NetworkContext setDestination(LogicalSide destination) {
        this.destination = destination;
        return this;
    }

    NetworkContext setServer(MinecraftServer server) {
        this.server = server;
        return this;
    }

    NetworkContext setClient(MinecraftClient client) {
        this.client = client;
        return this;
    }

    NetworkContext setRespond(PacketSender respond) {
        this.respond = respond;
        return this;
    }

    NetworkContext setServerHandle(ServerPlayNetworkHandler serverHandle) {
        this.serverHandle = serverHandle;
        return this;
    }

    NetworkContext setClientHandle(ClientPlayNetworkHandler clientHandle) {
        this.clientHandle = clientHandle;
        return this;
    }

    public void handle() {
        this.packetHandled = true;
    }
}
