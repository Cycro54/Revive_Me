package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.awt.*;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public Level level;

    @Unique
    private FallenCapability revive_Me$cap;

    @Unique
    private boolean revive_Me$grabOriginal = false;

    @Shadow public abstract int getId();

    @Shadow @Nullable public abstract Team getTeam();

    @Unique
    private FallenCapability revive_Me$getCap(){
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player)) return null;
        revive_Me$cap = FallenCapability.get((Player)entity);

        return this.revive_Me$cap;
    }

    //TODO: DELETE COMMENTED CODE
//    @Inject(
//            method = "getPose",
//            at = {
//                    @At(value = "HEAD")
//            }, cancellable = true)
//    private void getPose(CallbackInfoReturnable<Pose> cir){
//        if (this.revive_Me$grabOriginal)return;
//
//        if (revive_Me$getCap() == null) return;
//        if (!revive_Me$getCap().isFallen()) return;
//
//        switch (ReviveMeConfig.fallenPose){
//            case CROUCH -> cir.setReturnValue(Pose.CROUCHING);
//            case PRONE -> cir.setReturnValue(Pose.SWIMMING);
//            case SLEEP -> cir.setReturnValue(Pose.SLEEPING);
//        }
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

    @Inject(
            method = "refreshDimensions",
            at = {
                    @At(value = "HEAD")
            })
    private void refreshDimensionsHead(CallbackInfo ci){
        this.revive_Me$grabOriginal = true;
    }

    @Inject(
            method = "refreshDimensions",
            at = {
                    @At(value = "TAIL")
            })
    private void refreshDimensionsTail(CallbackInfo ci){
        this.revive_Me$grabOriginal = false;
    }

}
