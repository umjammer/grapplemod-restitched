package com.yyon.grapplinghook.client.debug;

import com.yyon.grapplinghook.client.GrappleModClient;
import com.yyon.grapplinghook.client.physics.ClientPhysicsControllerTracker;
import com.yyon.grapplinghook.client.physics.controller.GrapplingHookPhysicsController;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

/**
 * F3 debug lines for the client physics controller. Replaces the old
 * DebugScreenOverlay#getGameInformation mixin; toggle it from the debug options screen.
 */
@Environment(EnvType.CLIENT)
public class GrappleModDebugEntry implements DebugScreenEntry {

    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        displayer.addLine("-- GrappleMod Debug");

        Player player = Minecraft.getInstance().player;
        int playerId = player != null
                ? player.getId()
                : -1;

        ClientPhysicsControllerTracker physManager = GrappleModClient.get().getClientControllerManager();
        int controllerCount = physManager.controllers.size();
        GrapplingHookPhysicsController ctx = physManager.controllers.get(playerId);

        if(ctx == null) {
            displayer.addLine("Controllers: c=%s".formatted(controllerCount));
            return;
        }

        String controllerID = ctx.getType().toString();
        String inactiveNotice = ctx.isControllerActive()
                ? ""
                : " (inactive)";

        displayer.addLine("Controller: c=%s, t=%s%s".formatted(controllerCount, controllerID, inactiveNotice));
        displayer.addLine("Duplicates: %s".formatted(ctx.getDuplicates()));
        displayer.addLine("Motion: %s".formatted(ctx.getCopyOfMotion()));
    }
}
