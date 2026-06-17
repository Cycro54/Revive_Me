package invoker54.reviveme.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class SleepRotatePlayerMixin {

    @Inject(
            remap = true,
            method = "submit",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V")
            }
    )
    private <S extends EntityRenderState> void onRenderPre(S renderState, CameraRenderState cameraRenderState, double camX, double camY, double camZ, PoseStack stack, SubmitNodeCollector nodeCollector, CallbackInfo ci) {
        if (!(renderState instanceof AvatarRenderState)) return;
        AvatarRenderState avatarRenderState = (AvatarRenderState) renderState;
        Entity entity = ClientUtil.getMinecraft().level.getEntity(avatarRenderState.id);
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;

        FallenData capability = FallenData.get(player);
        if (!capability.isFallen()) return;
        if (ReviveMeConfig.fallenPose != ReviveMeConfig.FALLEN_POSE.SLEEP) return;

        stack.mulPose(Axis.YP.rotationDegrees(-(avatarRenderState.bodyRot + 90)));
        avatarRenderState.bodyRot = 0;
        stack.translate(1,0.1f,0);
    }
}
