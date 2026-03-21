package invoker54.reviveme.mixin;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow
    @Nullable
    private Pose forcedPose;
    @Unique
    private static final ModLogger LOGGERT =
            ModLogger.getLogger(PlayerMixin.class, ReviveMeConfig.debugMode);

    protected PlayerMixin(EntityType<? extends LivingEntity> p_20966_, Level p_20967_) {
        super(p_20966_, p_20967_);
    }

    @Inject(
            method = "canEat",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void canEat(boolean canEat, CallbackInfoReturnable<Boolean> cir){
        if (!FallenCapability.get(this).isFallen()) return;

        cir.setReturnValue(false);
    }

    @Inject(
            method = "setForcedPose(Lnet/minecraft/world/entity/Pose;)V",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true, remap = false)
    private void setForcedPose(Pose pose, CallbackInfo ci){
        if (!FallenCapability.get(this).isFallen()) return;
        switch (ReviveMeConfig.fallenPose){
            case CROUCH -> {
                this.forcedPose = Pose.CROUCHING;
            }
            case PRONE -> {
                this.forcedPose = Pose.SWIMMING;
            }
            case SLEEP -> {
                this.forcedPose = Pose.SLEEPING;
            }
        }
        ci.cancel();
    }
}
