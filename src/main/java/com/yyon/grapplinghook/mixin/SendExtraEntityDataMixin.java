package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.entity.grapplehook.IExtendedSpawnPacketEntity;
import com.yyon.grapplinghook.network.NetworkManager;
import com.yyon.grapplinghook.network.clientbound.AddExtraDataMessage;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.EntityTrackerEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityTrackerEntry.class)
public class SendExtraEntityDataMixin {

    @Shadow @Final private Entity entity;

    @Inject(method = "startTracking(Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("TAIL"))
    public void appendDataChain(ServerPlayerEntity player, CallbackInfo ci) {
        if(this.entity instanceof IExtendedSpawnPacketEntity) {
            NetworkManager.packetToClient(new AddExtraDataMessage(this.entity), player);
        }
    }

}
