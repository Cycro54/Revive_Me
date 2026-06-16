package invoker54.reviveme.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
    private <T extends Entity> void onNameTagPre(T entity, float p_114486_, float p_114487_, PoseStack stack, MultiBufferSource p_114489_, int p_114490_, CallbackInfo ci) {
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;

        FallenCapability capability = FallenCapability.get(player);
        if (!capability.isFallen()) return;
        if (ReviveMeConfig.fallenPose != ReviveMeConfig.FALLEN_POSE.SLEEP) return;

        float cameraYaw = player.getYRot();

        stack.rotateAround(Axis.YP.rotationDegrees((90 + cameraYaw)),0,0,0);
    }
}
