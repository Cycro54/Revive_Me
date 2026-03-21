package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Options.class)
public class OptionsMixin {

    @Inject(
            method = "getCameraType",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void cameraType(CallbackInfoReturnable<CameraType> cir){
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;
        switch (ReviveMeConfig.fallenPerspective){
            case DEFAULT -> {}
            case THIRD_PERSON -> cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
            case R_THIRD_PERSON -> cir.setReturnValue(CameraType.THIRD_PERSON_FRONT);
            case FIRST_PERSON -> cir.setReturnValue(CameraType.FIRST_PERSON);
        }
    }
}
