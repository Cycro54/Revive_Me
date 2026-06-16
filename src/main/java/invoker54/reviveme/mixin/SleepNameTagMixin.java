package invoker54.reviveme.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class SleepNameTagMixin {

    @Inject(
            remap = true,
            method = "render",
            at = {
                    @At(value = "HEAD", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
            }
    )
    private <T extends Entity> void onNameTagPre(T entity, float p_225623_2_, float p_225623_3_, MatrixStack stack, IRenderTypeBuffer p_225623_5_, int p_225623_6_, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) entity;

        FallenCapability capability = FallenCapability.get(player);
        if (!capability.isFallen()) return;
        if (ReviveMeConfig.fallenPose != ReviveMeConfig.FALLEN_POSE.SLEEP) return;

        float cameraYaw = player.yRot;

        stack.last().pose().multiply(Vector3f.YP.rotationDegrees((90 + cameraYaw)));
    }
}
