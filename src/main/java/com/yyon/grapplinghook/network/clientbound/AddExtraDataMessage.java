package com.yyon.grapplinghook.network.clientbound;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.entity.grapplehook.IExtendedSpawnPacketEntity;
import com.yyon.grapplinghook.network.NetworkContext;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public class AddExtraDataMessage extends BaseMessageClient {

    private Entity entity;

    private int entityId;
    private byte[] extraData;

    public AddExtraDataMessage(Entity entity) {
        this.entity = entity;
        this.extraData = new byte[0];
    }

    public AddExtraDataMessage(PacketByteBuf buf) {
        super(buf);
    }

    @Override
    public void decode(PacketByteBuf buf) {
        this.entityId = buf.readVarInt();

        int readableBytes = buf.readVarInt();
        this.extraData = new byte[readableBytes];
        buf.readBytes(this.extraData);
    }

    @Override
    public void encode(PacketByteBuf buf) {
        buf.writeVarInt(this.entity.getId());

        if (entity instanceof IExtendedSpawnPacketEntity entityAdditionalSpawnData) {
            final PacketByteBuf spawnDataBuffer = new PacketByteBuf(Unpooled.buffer());

            entityAdditionalSpawnData.writeSpawnData(spawnDataBuffer);

            int byteCount = spawnDataBuffer.readableBytes();
            buf.writeVarInt(byteCount);
            buf.writeBytes(spawnDataBuffer);

            spawnDataBuffer.release();

        } else {
            buf.writeVarInt(0);
        }
    }

    @Override
    public Identifier getChannel() {
        return GrappleMod.id("data");
    }

    @Environment(EnvType.CLIENT)
    @Override
    public void processMessage(NetworkContext ctx) {
        ctx.getClient().execute(() -> {
            if(MinecraftClient.getInstance().world == null)
                throw new IllegalStateException("World must not be null");

            this.entity = MinecraftClient.getInstance().world.getEntityById(this.entityId);

            if (this.entity instanceof IExtendedSpawnPacketEntity entityAdditionalSpawnData) {
                PacketByteBuf buf = new PacketByteBuf(Unpooled.wrappedBuffer(this.extraData));
                entityAdditionalSpawnData.readSpawnData(buf);
                buf.release();
            }
        });
    }
}
