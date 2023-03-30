package com.yyon.grapplinghook.client;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.blockentity.GrappleModifierBlockEntity;
import com.yyon.grapplinghook.client.keybind.GrappleKeys;
import com.yyon.grapplinghook.client.keybind.MCKeys;
import com.yyon.grapplinghook.gui.GrappleModiferBlockGUI;
import com.yyon.grapplinghook.client.keybind.GrappleModKeyBindings;
import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.controller.AirfrictionController;
import com.yyon.grapplinghook.controller.ForcefieldController;
import com.yyon.grapplinghook.controller.GrappleController;
import com.yyon.grapplinghook.entity.grapplehook.GrapplehookEntity;
import com.yyon.grapplinghook.entity.grapplehook.GrapplehookEntityRenderer;
import com.yyon.grapplinghook.network.NetworkContext;
import com.yyon.grapplinghook.network.NetworkManager;
import com.yyon.grapplinghook.network.clientbound.BaseMessageClient;
import com.yyon.grapplinghook.registry.GrappleModEntities;
import com.yyon.grapplinghook.registry.GrappleModEntityRenderLayers;
import com.yyon.grapplinghook.registry.GrappleModItems;
import com.yyon.grapplinghook.util.GrappleCustomization;
import com.yyon.grapplinghook.util.GrappleModUtils;
import com.yyon.grapplinghook.util.Vec;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ModelPredicateProviderRegistry;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class GrappleModClient implements ClientModInitializer {

    private static GrappleModClient clientInstance;


    private static final Identifier SOUND_DOUBLE_JUMP = new Identifier("grapplemod", "doublejump");
    private static final  Identifier SOUND_SLIDE = new Identifier("grapplemod", "slide");

    private ClientControllerManager clientControllerManager;


    @Override
    public void onInitializeClient() {
        GrappleModClient.clientInstance = this;
        GrappleModClientCheck.clientLoaded = true;

        EntityRendererRegistry.register(GrappleModEntities.GRAPPLE_HOOK.get(), new GrapplehookEntityRenderFactory());

        GrappleModKeyBindings.registerAll();
        GrappleModEntityRenderLayers.registerAll();

        NetworkManager.registerClientPacketListeners();

        this.clientControllerManager = new ClientControllerManager();
        this.registerPropertyOverride();
        this.registerResourcePacks();
    }

    public static GrappleModClient get() {
        return GrappleModClient.clientInstance;
    }



    public void registerPropertyOverride() {
        ModelPredicateProviderRegistry.register(GrappleModItems.GRAPPLING_HOOK.get(), new Identifier("rocket"), (stack, world, entity, seed) -> GrappleModItems.GRAPPLING_HOOK.get().getPropertyRocket(stack, world, entity) ? 1 : 0);
        ModelPredicateProviderRegistry.register(GrappleModItems.GRAPPLING_HOOK.get(), new Identifier("double"), (stack, world, entity, seed) -> GrappleModItems.GRAPPLING_HOOK.get().getPropertyDouble(stack, world, entity) ? 1 : 0);
        ModelPredicateProviderRegistry.register(GrappleModItems.GRAPPLING_HOOK.get(), new Identifier("motor"), (stack, world, entity, seed) -> GrappleModItems.GRAPPLING_HOOK.get().getPropertyMotor(stack, world, entity) ? 1 : 0);
        ModelPredicateProviderRegistry.register(GrappleModItems.GRAPPLING_HOOK.get(), new Identifier("smart"), (stack, world, entity, seed) -> GrappleModItems.GRAPPLING_HOOK.get().getPropertySmart(stack, world, entity) ? 1 : 0);
        ModelPredicateProviderRegistry.register(GrappleModItems.GRAPPLING_HOOK.get(), new Identifier("enderstaff"), (stack, world, entity, seed) -> GrappleModItems.GRAPPLING_HOOK.get().getPropertyEnderstaff(stack, world, entity) ? 1 : 0);
        ModelPredicateProviderRegistry.register(GrappleModItems.GRAPPLING_HOOK.get(), new Identifier("magnet"), (stack, world, entity, seed) -> GrappleModItems.GRAPPLING_HOOK.get().getPropertyMagnet(stack, world, entity) ? 1 : 0);
        ModelPredicateProviderRegistry.register(GrappleModItems.GRAPPLING_HOOK.get(), new Identifier("attached"), (stack, world, entity, seed) -> {
            if (entity == null) return 0;
            return (ClientControllerManager.controllers.containsKey(entity.getId()) && !(ClientControllerManager.controllers.get(entity.getId()) instanceof AirfrictionController)) ? 1 : 0;
        });
        ModelPredicateProviderRegistry.register(GrappleModItems.FORCE_FIELD.get(), new Identifier("attached"), (stack, world, entity, seed) -> {
            if (entity == null) return 0;
            return (ClientControllerManager.controllers.containsKey(entity.getId()) && ClientControllerManager.controllers.get(entity.getId()) instanceof ForcefieldController) ? 1 : 0;
        });
        ModelPredicateProviderRegistry.register(GrappleModItems.GRAPPLING_HOOK.get(), new Identifier("hook"), (stack, world, entity, seed) -> GrappleModItems.GRAPPLING_HOOK.get().getPropertyHook(stack, world, entity) ? 1 : 0);
    }

    public void registerResourcePacks() {
        Optional<ModContainer> cont = FabricLoader.getInstance().getModContainer(GrappleMod.MODID);

        if(cont.isEmpty()) {
            GrappleMod.LOGGER.error("Unable to register resource packs! This mod technically doesn't exist!!");
            return;
        }

        ModContainer container = cont.get();

        GrappleModUtils.registerPack("original_textures", Text.translatable("pack.grapplemod.original"), container, ResourcePackActivationType.NORMAL);
    }


    public void startRocket(PlayerEntity player, GrappleCustomization custom) {
        ClientControllerManager.instance.startRocket(player, custom);
    }

    public String getKeyname(MCKeys keyEnum) {
        GameOptions gs = MinecraftClient.getInstance().options;

        KeyBinding binding = switch (keyEnum) {
            case keyBindUseItem -> gs.useKey;
            case keyBindForward -> gs.forwardKey;
            case keyBindLeft -> gs.leftKey;
            case keyBindBack -> gs.backKey;
            case keyBindRight -> gs.rightKey;
            case keyBindJump -> gs.jumpKey;
            case keyBindSneak -> gs.sneakKey;
            case keyBindAttack -> gs.attackKey;
        };

        String displayName = binding.getBoundKeyLocalizedText().getString();
        return switch (displayName) {
            case "Button 1" -> "Left Click";
            case "Button 2" -> "Right Click";
            default -> displayName;
        };
    }

    public void openModifierScreen(GrappleModifierBlockEntity tile) {
        MinecraftClient.getInstance().setScreen(new GrappleModiferBlockGUI(tile));
    }

    public void onMessageReceivedClient(BaseMessageClient msg, NetworkContext ctx) {
        msg.processMessage(ctx);
    }


    public void playSlideSound() {
        this.playSound(GrappleModClient.SOUND_SLIDE, GrappleConfig.getClientConf().sounds.slide_sound_volume);
    }

    public void playDoubleJumpSound() {
        this.playSound(GrappleModClient.SOUND_DOUBLE_JUMP, GrappleConfig.getClientConf().sounds.doublejump_sound_volume * 0.7F);
    }

    public void playWallrunJumpSound() {
        this.playSound(GrappleModClient.SOUND_DOUBLE_JUMP, GrappleConfig.getClientConf().sounds.wallrunjump_sound_volume * 0.7F);
    }

    public void resetLauncherTime(int playerId) {
        ClientControllerManager.instance.resetLauncherTime(playerId);
    }

    public void launchPlayer(PlayerEntity player) {
        ClientControllerManager.instance.launchPlayer(player);
    }

    public void updateRocketRegen(double rocketActiveTime, double rocketRefuelRatio) {
        ClientControllerManager.instance.updateRocketRegen(rocketActiveTime, rocketRefuelRatio);
    }

    public double getRocketFunctioning() {
        return ClientControllerManager.instance.getRocketFunctioning();
    }

    public boolean isWallRunning(Entity entity, Vec motion) {
        return ClientControllerManager.instance.isWallRunning(entity, motion);
    }

    public boolean isSliding(Entity entity, Vec motion) {
        return ClientControllerManager.instance.isSliding(entity, motion);
    }

    public GrappleController createControl(int id, int hookEntityId, int entityId, World world, Vec pos, BlockPos blockpos, GrappleCustomization custom) {
        return ClientControllerManager.instance.createControl(id, hookEntityId, entityId, world, blockpos, custom);
    }

    public boolean isKeyDown(GrappleKeys key) {
        return switch (key) {
            case key_boththrow -> GrappleModKeyBindings.key_boththrow.isPressed();
            case key_leftthrow -> GrappleModKeyBindings.key_leftthrow.isPressed();
            case key_rightthrow -> GrappleModKeyBindings.key_rightthrow.isPressed();
            case key_motoronoff -> GrappleModKeyBindings.key_motoronoff.isPressed();
            case key_jumpanddetach -> GrappleModKeyBindings.key_jumpanddetach.isPressed();
            case key_slow -> GrappleModKeyBindings.key_slow.isPressed();
            case key_climb -> GrappleModKeyBindings.key_climb.isPressed();
            case key_climbup -> GrappleModKeyBindings.key_climbup.isPressed();
            case key_climbdown -> GrappleModKeyBindings.key_climbdown.isPressed();
            case key_enderlaunch -> GrappleModKeyBindings.key_enderlaunch.isPressed();
            case key_rocket -> GrappleModKeyBindings.key_rocket.isPressed();
            case key_slide -> GrappleModKeyBindings.key_slide.isPressed();
        };
    }

    public GrappleController unregisterController(int entityId) {
        return ClientControllerManager.unregisterController(entityId);
    }

    public double getTimeSinceLastRopeJump(World world) {
        return GrappleModUtils.getTime(world) - ClientControllerManager.prevRopeJumpTime;
    }

    public void resetRopeJumpTime(World world) {
        ClientControllerManager.prevRopeJumpTime = GrappleModUtils.getTime(world);
    }

    public boolean isKeyDown(MCKeys keyEnum) {

        GameOptions options = MinecraftClient.getInstance().options;

        return switch (keyEnum) {
            case keyBindUseItem ->  options.useKey.isPressed();
            case keyBindForward -> options.forwardKey.isPressed();
            case keyBindLeft -> options.leftKey.isPressed();
            case keyBindBack -> options.backKey.isPressed();
            case keyBindRight -> options.rightKey.isPressed();
            case keyBindJump -> options.jumpKey.isPressed();
            case keyBindSneak -> options.sneakKey.isPressed();
            case keyBindAttack -> options.attackKey.isPressed();
        };
    }

    public boolean isMovingSlowly(Entity entity) {
        if (entity instanceof ClientPlayerEntity player) {
            return player.shouldSlowDown();
        }

        return false;
    }

    public void playSound(Identifier loc, float volume) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if(player == null) return;

        MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(loc, SoundCategory.PLAYERS, volume, 1.0F, Random.create(),false, 0, SoundInstance.AttenuationType.NONE, player.getX(), player.getY(), player.getZ(), false));
    }

    public int getWallrunTicks() {
        return ClientControllerManager.instance.ticksWallRunning;
    }

    public void setWallrunTicks(int newWallrunTicks) {
        ClientControllerManager.instance.ticksWallRunning = newWallrunTicks;
    }


    public ClientControllerManager getClientControllerManager() {
        return clientControllerManager;
    }

    private static class GrapplehookEntityRenderFactory implements EntityRendererFactory<GrapplehookEntity> {

        @Override
        @NotNull
        public EntityRenderer<GrapplehookEntity> create(Context manager) {
            return new GrapplehookEntityRenderer<>(manager, GrappleModItems.GRAPPLING_HOOK.get());
        }

    }
}
