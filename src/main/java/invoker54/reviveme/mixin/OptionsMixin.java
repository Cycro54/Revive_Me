package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
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
        if (!FallenData.get(ClientUtil.getPlayer()).isFallen()) return;
        switch (ReviveMeConfig.fallenPerspective){
            case DEFAULT -> {}
            case THIRD_PERSON -> cir.setReturnValue(CameraType.THIRD_PERSON_BACK);
            case R_THIRD_PERSON -> cir.setReturnValue(CameraType.THIRD_PERSON_FRONT);
            case FIRST_PERSON -> cir.setReturnValue(CameraType.FIRST_PERSON);
        }
    }
}
