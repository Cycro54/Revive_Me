package invoker54.reviveme.mixin;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Unique
    private static ModLogger LOGGERT = ModLogger.getLogger(EntityMixin.class, ReviveMeConfig.debugMode);

    @Shadow public Level level;

    @Unique
    private FallenData revive_Me$cap;

    @Shadow public abstract int getId();

    @Shadow
    public abstract Level level();

    @Unique
    private FallenData revive_Me$getCap() {
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player)) return null;
        revive_Me$cap = FallenData.get((Player) entity);

        return this.revive_Me$cap;
    }


//TODO: REMOVE COMMENTED CODE LATER
//    @Inject(
//            method = "hasPose",
//            at = {
//                    @At(value = "HEAD")
//            }, cancellable = true)
//    private void hasPose(Pose pose, CallbackInfoReturnable<Boolean> cir){
//        if (!this.level.isClientSide) return;
//        if (!PlayerPoseHandler.changeOriginalPose((Entity)(Object)this)) return;
//
//        switch (ReviveMeConfig.fallenPose){
//            case CROUCH -> cir.setReturnValue(pose == Pose.CROUCHING);
//            case PRONE -> cir.setReturnValue(pose == Pose.SWIMMING);
//            case SLEEP -> cir.setReturnValue(pose == Pose.SLEEPING);
//        }
//    }
//
//    @Inject(
//            method = "getPose",
//            at = {
//                    @At(value = "HEAD")
//            }, cancellable = true)
//    private void getPose(CallbackInfoReturnable<Pose> cir){
//        if (!this.level.isClientSide) return;
//        if (!PlayerPoseHandler.changeOriginalPose((Entity)(Object)this)) return;
//
//        switch (ReviveMeConfig.fallenPose){
//            case CROUCH -> cir.setReturnValue(Pose.CROUCHING);
//            case PRONE -> cir.setReturnValue(Pose.SWIMMING);
//            case SLEEP -> cir.setReturnValue(Pose.SLEEPING);
//        }
//    }

//    @Inject(
//            method = "setPose",
//            at = {
//                    @At(value = "HEAD")
//            }, cancellable = true)
//    private void setPose(Pose pose, CallbackInfo ci) {
//        if (!this.level.isClientSide) return;
//
//    }

    @Inject(
            method = "isInvulnerable",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void isInvulnerable(CallbackInfoReturnable<Boolean> cir){
        if (revive_Me$getCap() == null) return;
        if (!revive_Me$getCap().isFallen()) return;
        if (this.level.isClientSide) return;
        if (!ReviveMeConfig.dieWhenTimerEnds && revive_Me$getCap().timeRanOut()) return;

        cir.setReturnValue(true);
    }
}
