package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class PlayerRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {

    @Unique
    public boolean revive_me_affectSleepPosition = false;

    @Inject(
            method = "getRenderOffset(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)Lnet/minecraft/world/phys/Vec3;",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getRenderOffset(AvatarRenderState avatarRenderState, CallbackInfoReturnable<Vec3> cir){
        if (!revive_me_affectSleepPosition) return;
        cir.setReturnValue(new Vec3(1, 0.1F, 0));
    }

    @Inject(
            method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void extractState(AvatarlikeEntity entity, AvatarRenderState p_446472_, float p_445702_, CallbackInfo ci){
        if (!(entity instanceof Player)) return;
        revive_me_affectSleepPosition = FallenData.get(entity).isFallen() && ReviveMeConfig.fallenPose == ReviveMeConfig.FALLEN_POSE.SLEEP;
    }
}
