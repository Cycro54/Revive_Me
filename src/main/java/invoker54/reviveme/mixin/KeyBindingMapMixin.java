package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(KeyBindingMap.class)
public class KeyBindingMapMixin {

    @Shadow @Final private static EnumMap<KeyModifier, Map<InputMappings.Input, Collection<KeyBinding>>> map;

    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(KeyBindingMapMixin.class, ReviveMeConfig.debugMode);

    @Inject(
            method = "getBinding",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true,
            remap = false
    )
    private void getBinding(InputMappings.Input keyCode, KeyModifier keyModifier, CallbackInfoReturnable<KeyBinding> cir) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;

        Collection<KeyBinding> bindings = map.get(keyModifier).get(keyCode);
        if (bindings != null) {
            for (KeyBinding binding : bindings) {
                if (!binding.isActiveAndMatches(keyCode)) continue;
                if (!VanillaKeybindHandler.isAllowedKeybind(binding)) continue;
                cir.setReturnValue(binding);
                return;
            }
        }
        cir.setReturnValue(null);
    }

    @Inject(
            method = "lookupAll",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true,
            remap = false
    )
    private void lookupAll(InputMappings.Input keyCode, CallbackInfoReturnable<List<KeyBinding>> cir){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;

        List<KeyBinding> matchingBindings = new ArrayList<KeyBinding>();
        for (Map<InputMappings.Input, Collection<KeyBinding>> bindingsMap : map.values())
        {
            Collection<KeyBinding> bindings = bindingsMap.get(keyCode);
            if (bindings != null)
            {

                matchingBindings.addAll(bindings);
            }
        }
        for (KeyBinding binding : new ArrayList<>(matchingBindings)){
            if (VanillaKeybindHandler.isAllowedKeybind(binding)) continue;

            matchingBindings.remove(binding);
        }
        cir.setReturnValue(matchingBindings);
    }
}
