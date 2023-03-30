package com.yyon.grapplinghook.client.keybind;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;

public class GrappleModKeyBindings {

    public static ArrayList<KeyBinding> keyBindings = new ArrayList<>();

    public static KeyBinding createKeyBinding(KeyBinding k) {
        keyBindings.add(k);
        return k;
    }

    public static KeyBinding key_boththrow = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.boththrow.desc", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_2, "key.grapplemod.category"));
    public static KeyBinding key_leftthrow = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.leftthrow.desc", InputUtil.UNKNOWN_KEY.getCode(), "key.grapplemod.category"));
    public static KeyBinding key_rightthrow = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.rightthrow.desc", InputUtil.UNKNOWN_KEY.getCode(), "key.grapplemod.category"));
    public static KeyBinding key_motoronoff = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.motoronoff.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.grapplemod.category"));
    public static KeyBinding key_jumpanddetach = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.jumpanddetach.desc", GLFW.GLFW_KEY_SPACE, "key.grapplemod.category"));
    public static KeyBinding key_slow = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.slow.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.grapplemod.category"));
    public static KeyBinding key_climb = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.climb.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.grapplemod.category"));
    public static KeyBinding key_climbup = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.climbup.desc", InputUtil.UNKNOWN_KEY.getCode(), "key.grapplemod.category"));
    public static KeyBinding key_climbdown = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.climbdown.desc", InputUtil.UNKNOWN_KEY.getCode(), "key.grapplemod.category"));
    public static KeyBinding key_enderlaunch = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.enderlaunch.desc", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, "key.grapplemod.category"));
    public static KeyBinding key_rocket = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.rocket.desc", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, "key.grapplemod.category"));
    public static KeyBinding key_slide = GrappleModKeyBindings.createKeyBinding(new KeyBinding("key.slide.desc", GLFW.GLFW_KEY_LEFT_SHIFT, "key.grapplemod.category"));


    public static void registerAll() {
        for(KeyBinding mapping: GrappleModKeyBindings.keyBindings) {
            KeyBindingHelper.registerKeyBinding(mapping);
        }
    }

}
