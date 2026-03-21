package invoker54.reviveme.mixin;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin extends LivingEntity {
    @Shadow
    @Nullable
    private Pose forcedPose;
    @Unique
    private static final ModLogger LOGGERT =
            ModLogger.getLogger(PlayerMixin.class, ReviveMeConfig.debugMode);


    protected PlayerMixin(EntityType<? extends LivingEntity> p_i48577_1_, World p_i48577_2_) {
        super(p_i48577_1_, p_i48577_2_);
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
            method = "setForcedPose",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true, remap = false)
    private void setForcedPose(Pose pose, CallbackInfo ci){
        if (!FallenCapability.get(this).isFallen()) return;
        switch (ReviveMeConfig.fallenPose){
            case CROUCH: {
                this.forcedPose = Pose.CROUCHING;
                break;
            }
            case PRONE: {
                this.forcedPose = Pose.SWIMMING;
                break;
            }
            case SLEEP: {
                this.forcedPose = Pose.SLEEPING;
                break;
            }
        }
        ci.cancel();
    }
}
