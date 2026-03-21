package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.PointOfView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameSettings.class)
public class OptionsMixin {

    @Inject(
            method = "getCameraType",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void PointOfView(CallbackInfoReturnable<PointOfView> cir){
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;
        switch (ReviveMeConfig.fallenPerspective){
            case THIRD_PERSON: {
                cir.setReturnValue(PointOfView.THIRD_PERSON_BACK);
                break;
            }
            case R_THIRD_PERSON:  {
                cir.setReturnValue(PointOfView.THIRD_PERSON_FRONT);
                break;
            }
            case FIRST_PERSON: {
                cir.setReturnValue(PointOfView.FIRST_PERSON);
                break;
            }
        }
    }
}
