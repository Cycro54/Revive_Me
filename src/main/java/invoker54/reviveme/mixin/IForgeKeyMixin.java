package invoker54.reviveme.mixin;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraftforge.client.extensions.IForgeKeybinding;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Pseudo
@Mixin(KeyBinding.class)
public abstract class IForgeKeyMixin implements IForgeKeybinding {
    @Shadow @Final private static Map<String, KeyBinding> ALL;
    @Shadow @Final private String name;
    @Shadow private int clickCount;
    @Shadow
    boolean isDown;
    @Unique
    private static final Logger LOGGER = LogManager.getLogger();


    @Override
    public boolean isActiveAndMatches(InputMappings.Input keyCode) {
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
            KeyBinding KeyBinding = ALL.get(this.name);

            if (cap.isFallen() && !VanillaKeybindHandler.isVanillaKeybind(KeyBinding)) return false;
        }
        return IForgeKeybinding.super.isActiveAndMatches(keyCode);
    }



    @Inject(
            remap = true,
            method = "isDown()Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isDown(CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());

            KeyBinding KeyBinding = ALL.get(this.name);
//            if (cap.isFallen()){
//                //This will disable move keybinds if they are disallowed in the config
//                if (!ReviveMeConfig.canMove && VanillaKeybindHandler.isMovementKeybind(KeyBinding)){
//                    cir.setReturnValue(false);
//                }
//            }

            //This is strictly for the use key when reviving or being revived
            if (KeyBinding.equals(ClientUtil.mC.options.keyUse)) {
                VanillaKeybindHandler.useKeyDown = isDown;

                if (cap.getOtherPlayer() != null) {
                    this.clickCount = 0;
                    cir.setReturnValue(false);
                }
            }

        }
    }

}
