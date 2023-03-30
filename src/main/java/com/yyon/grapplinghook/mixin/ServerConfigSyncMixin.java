package com.yyon.grapplinghook.mixin;

import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.network.NetworkManager;
import com.yyon.grapplinghook.network.clientbound.LoggedInMessage;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class ServerConfigSyncMixin {

    @Inject(method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V", at = @At("TAIL"))
    public void onLogin(ClientConnection netManager, ServerPlayerEntity player, CallbackInfo ci) {
        NetworkManager.packetToClient(new LoggedInMessage(GrappleConfig.getConf()), player);
    }

}
