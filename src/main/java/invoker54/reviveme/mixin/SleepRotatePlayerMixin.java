package invoker54.reviveme.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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
            method = "render",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;render(Lnet/minecraft/world/entity/Entity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
            }
    )
    private <E extends Entity> void onRenderPre(E entity, double p_114386_, double p_114387_, double p_114388_, float p_114389_, float p_114390_, PoseStack stack, MultiBufferSource p_114392_, int p_114393_, CallbackInfo ci) {
        if (!(entity instanceof Player)) return;
        Player player = (Player) entity;

        FallenCapability capability = FallenCapability.get(player);
        if (!capability.isFallen()) return;
        if (ReviveMeConfig.fallenPose != ReviveMeConfig.FALLEN_POSE.SLEEP) return;

        float cameraYaw = player.getYRot();


        stack.last().pose().multiply(Vector3f.YP.rotationDegrees(-(cameraYaw + 90)));
        stack.translate(1, 0.1F, 0);
    }
}
