package invoker54.reviveme.mixin;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRendererManager.class)
public class SleepRotatePlayerMixin {

    @Inject(
            remap = true,
            method = "render",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/entity/Entity;FFLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V")
            }
    )
    private <E extends Entity> void onRenderPre(E entity, double p_229084_2_, double p_229084_4_, double p_229084_6_, float p_229084_8_, float p_229084_9_, MatrixStack stack, IRenderTypeBuffer p_229084_11_, int p_229084_12_, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) entity;

        FallenCapability capability = FallenCapability.get(player);
        if (!capability.isFallen()) return;
        if (ReviveMeConfig.fallenPose != ReviveMeConfig.FALLEN_POSE.SLEEP) return;

        float cameraYaw = player.yRot;


        stack.last().pose().multiply(Vector3f.YP.rotationDegrees(-(cameraYaw + 90)));
        stack.translate(1, 0.1F, 0);
    }
}
