package invoker54.reviveme.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.settings.KeyBindingMap;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(KeyBindingMap.class)
public class KeyBindingMapMixin {

    @Shadow @Final private static EnumMap<KeyModifier, Map<InputConstants.Key, Collection<KeyMapping>>> map;

    @Inject(
            method = "getBinding",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true,
            remap = false
    )
    private void getBinding(InputConstants.Key keyCode, KeyModifier keyModifier, CallbackInfoReturnable<KeyMapping> cir) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;

        Collection<KeyMapping> bindings = map.get(keyModifier).get(keyCode);
        if (bindings != null) {
            for (KeyMapping binding : bindings) {
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
    private void lookupAll(InputConstants.Key keyCode, CallbackInfoReturnable<List<KeyMapping>> cir){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;

        List<KeyMapping> matchingBindings = new ArrayList<KeyMapping>();
        for (Map<InputConstants.Key, Collection<KeyMapping>> bindingsMap : map.values())
        {
            Collection<KeyMapping> bindings = bindingsMap.get(keyCode);
            if (bindings != null)
            {

                matchingBindings.addAll(bindings);
            }
        }
        for (KeyMapping binding : new ArrayList<>(matchingBindings)){
            if (VanillaKeybindHandler.isAllowedKeybind(binding)) continue;

            matchingBindings.remove(binding);
        }
        cir.setReturnValue(matchingBindings);
    }
}
