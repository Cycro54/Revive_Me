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
public class ForgeHooksClientMixin {

    @Inject(
            remap = false,
            method = "onMouseButtonPre(III)Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onMouseButtonPre(int button, int action, int mods, CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());

            if (cap.isFallen()) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(
            remap = false,
            method = "onMouseButtonPost(III)V",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onMouseButtonPost(int button, int action, int mods, CallbackInfo ci){
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());

            if (cap.isFallen()) {
                ci.cancel();
            }
        }
    }

    @Inject(
            remap = false,
            method = "onKeyInput(IIII)V",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onKeyInput(int key, int scanCode, int action, int modifiers, CallbackInfo ci){
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());

            if (cap.isFallen()) {
                ci.cancel();
            }
        }
    }


}