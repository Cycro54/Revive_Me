package invoker54.reviveme.mixin;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeHooksClient.class)
public class KeyInputMixin {

    @Inject(
            remap = false,
            method = "fireKeyInput(IIII)V",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void fireKeyPress(int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
            if (cap.isFallen()) {
                if (!ReviveMeConfig.canMove){
                    ClientUtil.mC.options.keyShift.setDown(false);
                    ClientUtil.mC.options.keyLeft.setDown(false);
                    ClientUtil.mC.options.keyUp.setDown(false);
                    ClientUtil.mC.options.keyRight.setDown(false);
                    ClientUtil.mC.options.keyDown.setDown(false);
                }

//                ReviveMe.LOGGER.error("keys disabled");
                ci.cancel();
            }
        }
    }
}
