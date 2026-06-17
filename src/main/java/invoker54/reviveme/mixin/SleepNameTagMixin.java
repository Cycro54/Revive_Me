package invoker54.reviveme.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class SleepNameTagMixin {

    @Inject(
            remap = true,
            method = "submitNameTag",
            at = {
                    @At(value = "HEAD")
            }
    )
    private <T extends Entity, S extends EntityRenderState> void onNameTagPre(S renderState, PoseStack stack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState, CallbackInfo ci) {
//        if (!(renderState instanceof AvatarRenderState)) return;
//        AvatarRenderState avatarRenderState = (AvatarRenderState) renderState;
//        Entity entity = ClientUtil.getMinecraft().level.getEntity(avatarRenderState.id);
//        if (!(entity instanceof Player)) return;
//
//        Player player = (Player) entity;
//
//        FallenData capability = FallenData.get(player);
//        if (!capability.isFallen()) return;
//        if (ReviveMeConfig.fallenPose != ReviveMeConfig.FALLEN_POSE.SLEEP) return;
//
//        float cameraYaw = player.getYRot();
//
//        stack.rotateAround(Axis.YP.rotationDegrees((90 + cameraYaw)),0,0,0);
    }
}
