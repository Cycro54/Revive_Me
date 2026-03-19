package invoker54.reviveme.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Unique
    private static ModLogger LOGGERT = ModLogger.getLogger(LevelRendererMixin.class, ReviveMeConfig.debugMode);

    @Unique
    private Pose revive_Me$originalPose = Pose.STANDING;

    @Inject(
            method = "renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void renderPlayerStart(Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci){
        if (!(entity instanceof Player)) return;
        FallenData data = FallenData.get((LivingEntity) entity);
        if (!data.isFallen()) return;
        Player player = (Player) entity;
        this.revive_Me$originalPose = entity.getPose();

        switch (ReviveMeConfig.fallenPose){
            case CROUCH -> {
                entity.setPose(Pose.CROUCHING);
                if (player instanceof LocalPlayer) ((LocalPlayerMixin)player).setCrouching(true);
            }
            case PRONE -> {
                entity.setPose(Pose.SWIMMING);
                //2 is needed due to the way the method updates...
                //It will cause the player to jitter
                player.updateSwimAmount();
                player.updateSwimAmount();
            }
            case SLEEP -> entity.setPose(Pose.SLEEPING);
        }
    }

    @Inject(
            method = "renderEntity(Lnet/minecraft/world/entity/Entity;DDDFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;)V",
            at = {
                    @At(value = "TAIL")
            }, cancellable = true
    )
    private void renderPlayerEnd(Entity entity, double camX, double camY, double camZ, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci){
        if (!(entity instanceof Player)) return;
        FallenData data = FallenData.get((LivingEntity) entity);
        if (!data.isFallen()) return;
        Player player = (Player) entity;

        switch (ReviveMeConfig.fallenPose){
            case CROUCH -> {
                if (player instanceof LocalPlayer) ((LocalPlayerMixin)player).setCrouching(false);
            }
            case PRONE -> player.setSwimming(false);
        }

        entity.setPose(this.revive_Me$originalPose);

        switch (this.revive_Me$originalPose){
            case CROUCHING -> {
                if (player instanceof LocalPlayer) ((LocalPlayerMixin)player).setCrouching(true);
            }
            case SWIMMING -> player.setSwimming(true);
//            case SLEEPING -> ;
        }
//        player.aiStep();
    }
}
