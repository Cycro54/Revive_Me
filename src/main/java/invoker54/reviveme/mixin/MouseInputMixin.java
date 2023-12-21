package invoker54.reviveme.mixin;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraftforge.client.ForgeHooksClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooksClient.class)
public class MouseInputMixin {
    @Inject(
            remap = false,
            method = "onRawMouseClicked",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void mousePressEvent(int button, int action, int mods, CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
            if (cap.isFallen() || cap.getOtherPlayer() != null) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
            remap = false,
            method = "fireMouseInput",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void rawMouseEvent(int button, int action, int mods, CallbackInfo ci) {
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
            if (cap.isFallen() || cap.getOtherPlayer() != null) {
                ci.cancel();
            }
        }
    }
}
