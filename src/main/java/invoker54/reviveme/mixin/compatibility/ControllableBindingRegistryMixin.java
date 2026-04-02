package invoker54.reviveme.mixin.compatibility;

import com.mrcrayfish.controllable.client.binding.BindingRegistry;
import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;

import static invoker54.reviveme.compatibility.controllable.client.events.ControllableModEvents.revive_Me_vanillaBindingList;

@Mixin(BindingRegistry.class)
public class ControllableBindingRegistryMixin {
    @Unique
    private static final ModLogger LOGGERT =
            ModLogger.getLogger(ReviveMeConfig.debugMode);

    @Inject(
            remap = false,
            method = "getBindingsForButton",
            at = {
                    @At(value = "RETURN")
            },
            cancellable = true)
    public void getBindings(int button, CallbackInfoReturnable<Collection<ButtonBinding>> cir) {
        if (ClientUtil.getPlayer() == null) return;
        boolean isFallen = FallenData.get(ClientUtil.getPlayer()).isFallen();
        boolean isLookingAtFallen = false;
        if (ClientUtil.getMinecraft().crosshairPickEntity instanceof Player) {
            isLookingAtFallen = FallenData.get((Player) ClientUtil.getMinecraft().crosshairPickEntity).isFallen();
        }
        if (!isFallen && !isLookingAtFallen) return;

        Collection<ButtonBinding> bindingList = new ArrayList<>(cir.getReturnValue());
        if (bindingList.isEmpty()) return;
        if (!isFallen && bindingList.stream().noneMatch(b ->
                b.getLabelKey().equals(KeyInit.rightOption.keyBind.getName()))){
            return;
        }

        if (isFallen){
            for (ButtonBinding binding : new ArrayList<>(bindingList)){
                KeyMapping keyBinding = VanillaKeybindHandler.getOrCreateKey(binding.getLabelKey());
                if (revive_Me_vanillaBindingList.contains(binding)) continue;
                if (VanillaKeybindHandler.canBeDown(keyBinding)) continue;
                binding.resetPressedState();
                bindingList.remove(binding);
            }
        }

        bindingList = bindingList.stream().sorted(Comparator.comparing(bell ->
                !bell.getLabelKey().contains("key.revive_me"))).toList();
        bindingList.forEach(b -> LOGGERT.warn(b.getLabelKey()));

        cir.setReturnValue(bindingList);
    }
}
