package invoker54.reviveme.mixin;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.init.KeyInit;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientHooks.class)
public class ClientHooksMixin {

    @Inject(
            remap = false,
            method = "onMouseButtonPre(III)Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onMouseButtonPre(int button, int action, int mods, CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getWorld() != null) {
            FallenData cap = FallenData.get(ClientUtil.getPlayer());

            if (cap.isFallen()) {
                cir.setReturnValue(false);
                if (button == KeyInit.callForHelpKey.keyBind.getKey().getValue()) KeyInit.callForHelpKey.pressed(action);
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
            FallenData cap = FallenData.get(ClientUtil.getPlayer());

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
            FallenData cap = FallenData.get(ClientUtil.getPlayer());

            if (cap.isFallen()) {
                ci.cancel();
                if (key == KeyInit.callForHelpKey.keyBind.getKey().getValue()) KeyInit.callForHelpKey.pressed(action);
            }
        }
    }


}