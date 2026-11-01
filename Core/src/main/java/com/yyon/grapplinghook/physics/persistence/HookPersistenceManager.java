package com.yyon.grapplinghook.physics.persistence;

import com.yyon.grapplinghook.GrappleMod;
import com.yyon.grapplinghook.content.entity.grapplinghook.GrapplinghookEntity;
import com.yyon.grapplinghook.content.registry.internal.ModEntities;
import com.yyon.grapplinghook.network.NetworkManager;
import com.yyon.grapplinghook.network.clientbound.GrappleDetachS2CPayload;
import com.yyon.grapplinghook.physics.ServerHookEntityTracker;
import com.yyon.grapplinghook.physics.attach.HookAttachment;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class HookPersistenceManager {

    private static final String NBT_ROOT = "GrappleModPersistedHooks";
    private static final int GRACE_TICKS = 600;

    private static final Map<UUID, List<Pending>> pending = new ConcurrentHashMap<>();

    private HookPersistenceManager() {}

    // Player data is written through ValueOutput/ValueInput now; the on-disk layout
    // (a list of compound tags under NBT_ROOT) is unchanged.
    public static void saveForPlayer(ServerPlayer player, ValueOutput output) {
        Set<GrapplinghookEntity> hooks = ServerHookEntityTracker.getHooksThrownBy(player);
        List<CompoundTag> tags = new ArrayList<>();
        for (GrapplinghookEntity hook : hooks) {
            if (hook == null || !hook.isAlive()) continue;
            PersistedHook ph = PersistedHook.capture(hook);
            if (ph == null) continue;
            tags.add(ph.toNbt());
        }
        if (tags.isEmpty()) {
            output.discard(NBT_ROOT);
        } else {
            ValueOutput.TypedOutputList<CompoundTag> list = output.list(NBT_ROOT, CompoundTag.CODEC);
            tags.forEach(list::add);
        }
    }

    public static void loadForPlayer(ServerPlayer player, ValueInput input) {
        List<Pending> entries = new ArrayList<>();
        input.listOrEmpty(NBT_ROOT, CompoundTag.CODEC).stream().forEach(tag -> {
            PersistedHook ph = PersistedHook.fromNbt(tag);
            if (ph != null) entries.add(new Pending(ph));
        });
        if (entries.isEmpty()) pending.remove(player.getUUID());
        else pending.put(player.getUUID(), entries);
    }

    public static void tickServer(MinecraftServer server) {
        if (pending.isEmpty()) return;
        Iterator<Map.Entry<UUID, List<Pending>>> it = pending.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, List<Pending>> e = it.next();
            ServerPlayer player = server.getPlayerList().getPlayer(e.getKey());
            if (player == null) continue;
            List<Pending> entries = e.getValue();
            entries.removeIf(pr -> tryRestore(server, player, pr));
            if (entries.isEmpty()) it.remove();
        }
    }

    public static void clearAll() { pending.clear(); }

    private static boolean tryRestore(MinecraftServer server, ServerPlayer player, Pending pr) {
        PersistedHook ph = pr.hook;
        pr.ticks++;

        ServerLevel level = server.getLevel(ResourceKey.create(Registries.DIMENSION, ph.dimension()));
        if (level == null) {
            GrappleMod.LOGGER.warn("Dropping persisted hook: dimension {} not found", ph.dimension());
            return true;
        }
        if (player.level() != level) {
            if (pr.ticks > GRACE_TICKS) {
                GrappleMod.LOGGER.info("Dropping persisted hook: player {} not in dimension {} after {} ticks",
                        player.getGameProfile().name(), ph.dimension(), pr.ticks);
                return true;
            }
            return false;
        }

        if (pr.spawnedHook != null) {
            if (!pr.spawnedHook.isAlive()) return true;
            pr.spawnedHook.serverAttach(pr.resolvedTarget, true);
            return true;
        }

        HookAttachment resolved = ph.attachment().tryResolve(level);
        if (resolved == null) {
            if (pr.ticks <= GRACE_TICKS && ph.attachment().mayBecomeAvailableLater(level)) return false;
            GrappleMod.LOGGER.info("Dropping persisted hook for {}: anchor no longer resolvable",
                    player.getGameProfile().name());
            notifyClientDetach(player);
            return true;
        }

        pr.spawnedHook = spawnHook(level, player, ph);
        pr.resolvedTarget = resolved;
        return false;
    }

    private static GrapplinghookEntity spawnHook(ServerLevel level, ServerPlayer player, PersistedHook ph) {
        GrapplinghookEntity hook = new GrapplinghookEntity(ModEntities.GRAPPLE_HOOK.get(), level);
        hook.initForRestore(player, ph.mainHand(), ph.inDoublePair(), ph.customization(), ph.ropeLength(), ph.hookPos());

        level.addFreshEntity(hook);
        hook.getSegmentHandler().loadFromSnapshot(ph.ropeSnapshot());
        ServerHookEntityTracker.addGrappleEntity(player, hook);

        com.yyon.grapplinghook.content.item.GrapplehookItem grappleItem = com.yyon.grapplinghook.content.registry.internal.ModItems.GRAPPLING_HOOK.get();
        if (ph.mainHand()) grappleItem.setHookEntityMainHand(player, hook);
        else grappleItem.setHookEntityOffHand(player, hook);

        return hook;
    }

    private static void notifyClientDetach(ServerPlayer player) {
        NetworkManager.packetToClient(new GrappleDetachS2CPayload(player.getId()), player);
    }

    private static final class Pending {
        final PersistedHook hook;
        int ticks;
        GrapplinghookEntity spawnedHook;
        HookAttachment resolvedTarget;
        Pending(PersistedHook hook) { this.hook = hook; this.ticks = 0; }
    }
}
