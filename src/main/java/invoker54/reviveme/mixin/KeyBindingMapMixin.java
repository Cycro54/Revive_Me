package invoker54.reviveme.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import net.neoforged.neoforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(KeyMappingLookup.class)
public class KeyBindingMapMixin {

    @Shadow @Final private EnumMap<KeyModifier, Map<InputConstants.Key, Collection<KeyMapping>>> map;

    @Inject(
            method = "findKeybinds(Lcom/mojang/blaze3d/platform/InputConstants$Key;Lnet/neoforged/neoforge/client/settings/KeyModifier;)Ljava/util/List;",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true,
            remap = false
    )
    private void findKeybinds(InputConstants.Key keyCode, KeyModifier modifier, CallbackInfoReturnable<List<KeyMapping>> cir) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenData.get(ClientUtil.getPlayer()).isFallen()) return;

        Collection<KeyMapping> modifierBindings = (Collection)((Map)map.get(modifier)).get(keyCode);
        cir.setReturnValue(modifierBindings != null ? modifierBindings.stream().filter((binding) ->
                binding.isActiveAndMatches(keyCode) && VanillaKeybindHandler.isAllowedKeybind(binding)).toList() : List.of());
    }
}
