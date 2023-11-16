package invoker54.reviveme.mixin;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ForgeHooksClient.class)
public class KeyInputMixin {

    @Inject(
            remap = false,
            method = "onKeyInput(IIII)V",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void fireKeyPress(int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
            if (cap.isFallen()) {
//                ReviveMe.LOGGER.error("keys disabled");
                ci.cancel();
            }
        }
    }
}
