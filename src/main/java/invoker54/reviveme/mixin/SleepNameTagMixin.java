package invoker54.reviveme.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.client.event.CallForHelpEvent;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.ContextKeyInit;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class SleepNameTagMixin {
    @Inject(
            remap = true,
            method = "submitNameDisplay(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;I)V",
            at = {
                    @At(value = "HEAD")
            }
    )
    private <S extends EntityRenderState> void onNameTagPre(S renderState, PoseStack stack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, int offset, CallbackInfo ci) {
        if (!(renderState instanceof AvatarRenderState)) return;
        AvatarRenderState avatarRenderState = (AvatarRenderState) renderState;
        Entity entity = ClientUtil.getMinecraft().level.getEntity(avatarRenderState.id);
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;

        FallenData capability = FallenData.get(player);
        if (!capability.isFallen()) return;
        if (ReviveMeConfig.fallenPose != ReviveMeConfig.FALLEN_POSE.SLEEP) return;

        float storedBodyRotation = renderState.getRenderDataOrDefault(ContextKeyInit.bodyRotContext, 0F);
        stack.mulPose(Axis.YP.rotationDegrees((storedBodyRotation + 90)));
        avatarRenderState.bodyRot = storedBodyRotation;
    }
}
