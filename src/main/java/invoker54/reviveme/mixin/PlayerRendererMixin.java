package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(
            method = "getRenderOffset(Lnet/minecraft/client/player/AbstractClientPlayer;F)Lnet/minecraft/world/phys/Vec3;",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getRenderOffset(AbstractClientPlayer player, float p_117786_, CallbackInfoReturnable<Vec3> cir){
        if (FallenData.get(player).isFallen() && ReviveMeConfig.fallenPose == ReviveMeConfig.FALLEN_POSE.SLEEP) {
            cir.setReturnValue(new Vec3(1, 0.1F, 0));
        }
    }
}
