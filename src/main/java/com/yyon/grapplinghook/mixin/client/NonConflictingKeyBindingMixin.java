package com.yyon.grapplinghook.mixin.client;

import com.google.common.collect.Maps;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

// Trying to avoid overwites and collisions by just making a new system
// entirely and forwarding things to that.
@Mixin(KeyBinding.class)
public abstract class NonConflictingKeyBindingMixin {

    // redirect away from the original to this
    private static final Map<InputUtil.Key, HashMap<String, KeyBinding>> FULL_SELECTION = Maps.newHashMap();

    @Final
    @Shadow
    private static Map<String, KeyBinding> KEYS_BY_ID;

    @Final
    @Shadow
    private static Map<InputUtil.Key, KeyBinding> KEY_TO_BINDINGS;

    @Shadow private int timesPressed;
    @Shadow @Final private InputUtil.Key defaultKey;

    @Shadow public abstract void setPressed(boolean value);

    @Shadow @Final private String translationKey;

    @Shadow public abstract String getTranslationKey();

    private static HashMap<String, KeyBinding> getOrCreateKeybindMapForKey(InputUtil.Key key) {
        HashMap<String, KeyBinding> map = FULL_SELECTION.get(key);
        if(map != null)
            return FULL_SELECTION.get(key);

        HashMap<String, KeyBinding> mappingGroup = Maps.newHashMap();
        FULL_SELECTION.put(key, mappingGroup);
        return mappingGroup;
    }




    @Inject(method = "onKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;)V", at = @At("RETURN"))
    private static void click(InputUtil.Key key, CallbackInfo ci) {
        HashMap<String, KeyBinding> keyMappings = FULL_SELECTION.get(key);

        if (keyMappings != null) {
            keyMappings.forEach((n, m) -> {
                KeyBinding primaryMapping = KEY_TO_BINDINGS.get(keyForMapping(m));
                if(primaryMapping == null || !primaryMapping.getTranslationKey().equals(n)) // don't double tap
                    ++((NonConflictingKeyBindingMixin) (Object) m).timesPressed;
            });
        }
    }

    @Inject(method = "setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V", at = @At("RETURN"))
    private static void set(InputUtil.Key key, boolean held, CallbackInfo ci) {
        HashMap<String, KeyBinding> keyMappings = FULL_SELECTION.get(key);

        if (keyMappings != null)
            keyMappings.forEach((n, m) -> ((NonConflictingKeyBindingMixin) (Object) m).setPressed(held));
    }

    @Inject(method = "unpressAll()V", at = @At("RETURN"))
    private static void resetMapping(CallbackInfo ci) {
        FULL_SELECTION.clear();

        KEYS_BY_ID.values().forEach(keyMapping -> {
            HashMap<String, KeyBinding> group = getOrCreateKeybindMapForKey(keyForMapping(keyMapping));
            group.put(keyMapping.getTranslationKey(), keyMapping);
        });
    }


    @Inject(method = "<init>(Ljava/lang/String;Lnet/minecraft/client/util/InputUtil$Type;ILjava/lang/String;)V",
            at = @At(value = "RETURN"))
    private void injectConstructor(String name, InputUtil.Type type, int keyCode, String category, CallbackInfo ci) {
        InputUtil.Key key = type.createFromCode(keyCode);
        KeyBinding olderMappingSharedName = KEYS_BY_ID.get(name); // Older mapping for this mapping name

        // If a keybinding for this name already exists, remove it.
        if(olderMappingSharedName != null) {
            InputUtil.Key olderMappingKey = keyForMapping(olderMappingSharedName);

            if(FULL_SELECTION.containsKey(olderMappingKey)) {
                FULL_SELECTION.get(olderMappingKey)
                        .remove(olderMappingSharedName.getTranslationKey());
            }
        }

        HashMap<String, KeyBinding> group = getOrCreateKeybindMapForKey(key);
        group.put(name, (KeyBinding) (Object) this);
    }

    @Inject(method = "setBoundKey", at = @At("HEAD"))
    public void updateKey(InputUtil.Key key, CallbackInfo ci) {
        InputUtil.Key oldKey = this.defaultKey;

        if(FULL_SELECTION.containsKey(oldKey)) {
            FULL_SELECTION.get(oldKey).remove(this.translationKey);
        }

        KeyBinding oldMap = KEY_TO_BINDINGS.get(oldKey);

        if(oldMap != null && this.getTranslationKey().equals(oldMap.getTranslationKey())) {
            KEY_TO_BINDINGS.remove(oldKey);
        }
    }

    private static InputUtil.Key keyForMapping(KeyBinding mapping) {
        return ((NonConflictingKeyBindingMixin) (Object) mapping).defaultKey;
    }
}
