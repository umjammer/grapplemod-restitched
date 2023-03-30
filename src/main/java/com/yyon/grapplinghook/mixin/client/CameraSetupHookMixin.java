package com.yyon.grapplinghook.mixin.client;

import com.yyon.grapplinghook.client.ClientControllerManager;
import com.yyon.grapplinghook.config.GrappleConfig;
import com.yyon.grapplinghook.controller.AirfrictionController;
import com.yyon.grapplinghook.controller.GrappleController;
import com.yyon.grapplinghook.util.Vec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class CameraSetupHookMixin {

    @Final
    @Shadow
    private Camera camera;

    protected float currentCameraTilt = 0;
    @Inject(method = "renderWorld(FJLnet/minecraft/client/util/math/MatrixStack;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/Camera;update(Lnet/minecraft/world/BlockView;Lnet/minecraft/entity/Entity;ZZF)V",
                    shift = At.Shift.AFTER
            ))
    public void postCameraSetup(float partialTicks, long finishTimeNano, MatrixStack matrixStack, CallbackInfo ci) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (!MinecraftClient.getInstance().isRunning() || player == null) {
            return;
        }

        int id = player.getId();
        int targetCameraTilt = 0;
        if (ClientControllerManager.controllers.containsKey(id)) {
            GrappleController controller = ClientControllerManager.controllers.get(id);
            if (controller instanceof AirfrictionController afcontroller) {
                if (afcontroller.wasWallrunning) {
                    Vec walldirection = afcontroller.getWallDirection();
                    if (walldirection != null) {
                        Vec lookdirection = Vec.lookVec(player);
                        int dir = lookdirection.cross(walldirection).y > 0 ? 1 : -1;
                        targetCameraTilt = dir;
                    }
                }
            }
        }

        if (currentCameraTilt != targetCameraTilt) {
            float cameraDiff = targetCameraTilt - currentCameraTilt;
            if (cameraDiff != 0) {
                float anim_s = GrappleConfig.getClientConf().camera.wallrun_camera_animation_s;
                float speed = (anim_s == 0) ? 9999 :  1.0f / (anim_s * 20.0f);
                if (speed > Math.abs(cameraDiff)) {
                    currentCameraTilt = targetCameraTilt;
                } else {
                    currentCameraTilt += speed * (cameraDiff > 0 ? 1 : -1);
                }
            }
        }

        if (currentCameraTilt != 0) {
            // Observing the forge hook, roll just isn't used.
            // Fix this.
            //mainCamera.(0 + currentCameraTilt*GrappleConfig.getClientConf().camera.wallrun_camera_tilt_degrees);
        }
    }

}
