package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class LocalPlayerMixin {

    @Inject(
            
            method = "isMovingSlowly",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void isMovingSlowly(CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.GetFallCap(ClientUtil.getPlayer()).isFallen()) return;
        cir.setReturnValue(ClientUtil.getPlayer().isCrouching());
    }
}
