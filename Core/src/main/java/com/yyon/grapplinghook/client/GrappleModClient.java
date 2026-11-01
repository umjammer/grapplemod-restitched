package com.yyon.grapplinghook.client;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.client.debug.GrappleModDebugEntry;
import com.yyon.grapplinghook.client.item.GrappleItemStateProperty;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import com.yyon.grapplinghook.client.network.ClientNetworkReceivers;
import com.yyon.grapplinghook.client.physics.ClientPhysicsControllerTracker;
import com.yyon.grapplinghook.client.render.entity.GrapplinghookEntityRenderer;
import com.yyon.grapplinghook.client.render.entity.GrapplinghookRenderState;
import com.yyon.grapplinghook.config.GrappleModClientConfig;
import com.yyon.grapplinghook.content.entity.grapplinghook.GrapplinghookEntity;
import com.yyon.grapplinghook.content.registry.internal.ModEntities;
import com.yyon.grapplinghook.content.registry.internal.ModEntityLayerIdentifiers;
import com.yyon.grapplinghook.content.registry.internal.ModItems;
import com.yyon.grapplinghook.content.customization.data.HookCustomization;
import com.yyon.grapplinghook.content.registry.internal.ModMenuScreens;
import com.yyon.grapplinghook.util.GrappleModUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class GrappleModClient implements ClientModInitializer {

    private static GrappleModClient clientInstance;

    private ClientPhysicsControllerTracker clientPhysicsControllerTracker;


    @Override
    public void onInitializeClient() {
        GrappleModClient.clientInstance = this;

        try {
            this.initConfig();
        } catch (Exception e) {
            GrappleMod.LOGGER.info(e);
        }

        EntityRendererRegistry.register(ModEntities.GRAPPLE_HOOK.get(), new GrapplehookEntityRenderFactory());

        ClientNetworkReceivers.registerAll();

        ModKeys.registerAll();
        ModEntityLayerIdentifiers.registerAll();
        ModMenuScreens.registerAll();
        DebugScreenEntries.register(GrappleMod.id("physics"), new GrappleModDebugEntry());

        this.clientPhysicsControllerTracker = new ClientPhysicsControllerTracker();
        this.registerPropertyOverride();
        this.registerResourcePacks();
    }

    public static GrappleModClient get() {
        return GrappleModClient.clientInstance;
    }

    public void initConfig() {
        GrappleModClientConfig.HANDLER.defaults().saveDefaults();
        GrappleModClientConfig.HANDLER.load();
    }

    public void registerPropertyOverride() {
        // Item model predicates are now data-driven: see assets/grapplemod/items/*.json
        GrappleItemStateProperty.register();
    }

    public void registerResourcePacks() {
        Optional<ModContainer> cont = FabricLoader.getInstance().getModContainer(GrappleMod.MOD_ID);

        if(cont.isEmpty()) {
            GrappleMod.LOGGER.error("Unable to register resource packs! This mod technically doesn't exist!!");
            return;
        }

        ModContainer container = cont.get();

        GrappleModUtils.registerPack("original_textures", Component.translatable("pack.grapplemod.original"), container, ResourcePackActivationType.NORMAL);
    }


    public void startRocket(Player player, HookCustomization custom) {
        this.getClientControllerManager().startRocket(player, custom);
    }

    public void resetLauncherTime(int playerId) {
        this.getClientControllerManager().resetLauncherTime(playerId);
    }

    public void launchPlayer(Player player) {
        this.getClientControllerManager().launchPlayer(player);
    }

    public void updateRocketRegen(double rocketActiveTime, double rocketRefuelRatio) {
        this.getClientControllerManager().updateRocketRegen(rocketActiveTime, rocketRefuelRatio);
    }

    public double getRocketFunctioning() {
        return this.getClientControllerManager().getRocketFunctioning();
    }

    public long getTimeSinceLastRopeJump(Level world) {
        return world.getGameTime() - ClientPhysicsControllerTracker.prevRopeJumpTime;
    }

    public void resetRopeJumpTime(Level world) {
        ClientPhysicsControllerTracker.prevRopeJumpTime = world.getGameTime();
    }

    public boolean isMovingSlowly(Entity entity) {
        if (entity instanceof LocalPlayer player) {
            return player.isMovingSlowly();
        }

        return false;
    }

    public void playSound(Identifier loc, float volume) {
        Player player = Minecraft.getInstance().player;
        if(player == null) return;

        SimpleSoundInstance sound = new SimpleSoundInstance(
                loc, SoundSource.PLAYERS, volume, 1.0F, RandomSource.create(),
                false, 0,
                SoundInstance.Attenuation.NONE,
                player.getX(), player.getY(), player.getZ(),
                false
        );

        Minecraft.getInstance()
                .getSoundManager()
                .play(sound);
    }

    public ClientPhysicsControllerTracker getClientControllerManager() {
        return this.clientPhysicsControllerTracker;
    }

    private static class GrapplehookEntityRenderFactory implements EntityRendererProvider<GrapplinghookEntity> {

        @Override
        @NotNull
        public EntityRenderer<GrapplinghookEntity, GrapplinghookRenderState> create(Context manager) {
            return new GrapplinghookEntityRenderer<>(manager, ModItems.GRAPPLING_HOOK.get());
        }

    }
}
