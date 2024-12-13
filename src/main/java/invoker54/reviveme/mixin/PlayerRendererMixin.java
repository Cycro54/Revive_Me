package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.math.vector.Vector3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(
            
            method = "getRenderOffset(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;F)Lnet/minecraft/util/math/vector/Vector3d;",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getRenderOffset(AbstractClientPlayerEntity player, float p_225627_2_, CallbackInfoReturnable<Vector3d> cir){
        if (FallenCapability.GetFallCap(player).isFallen() && ReviveMeConfig.fallenPose == ReviveMeConfig.FALLEN_POSE.SLEEP) {
            cir.setReturnValue(new Vector3d(1, 0.1F, 0));
        }
    }
}
