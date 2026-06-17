package invoker54.reviveme.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.ContextKeyInit;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class SleepNameTagMixin {
    private static ModLogger revive_me_LOGGER = ModLogger.getLogger(SleepNameTagMixin.class, ReviveMeConfig.debugMode);

    @Inject(
            remap = true,
            method = "submitNameTag(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/CameraRenderState;)V",
            at = {
                    @At(value = "HEAD")
            }
    )
    private <S extends EntityRenderState> void onNameTagPre(AvatarRenderState avatarRenderState, PoseStack stack, SubmitNodeCollector p_446248_, CameraRenderState p_451056_, CallbackInfo ci) {
        Entity entity = ClientUtil.getMinecraft().level.getEntity(avatarRenderState.id);
        if (!(entity instanceof Player)) return;

        Player player = (Player) entity;

        FallenData capability = FallenData.get(player);
        if (!capability.isFallen()) return;
        if (ReviveMeConfig.fallenPose != ReviveMeConfig.FALLEN_POSE.SLEEP) return;

        float storedBodyRotation = avatarRenderState.getRenderDataOrDefault(ContextKeyInit.bodyRotContext, 0F);
        stack.mulPose(Axis.YP.rotationDegrees((storedBodyRotation + 90)));
        avatarRenderState.bodyRot = storedBodyRotation;
    }
}
