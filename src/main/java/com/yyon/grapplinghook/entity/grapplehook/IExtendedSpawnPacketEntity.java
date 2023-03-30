package com.yyon.grapplinghook.entity.grapplehook;

import net.minecraft.network.PacketByteBuf;

public interface IExtendedSpawnPacketEntity {

    void writeSpawnData(PacketByteBuf data);
    void readSpawnData(PacketByteBuf data);
}
