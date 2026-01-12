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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.awt.*;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public Level level;

    @Unique
    private FallenCapability revive_Me$cap;

    @Shadow public abstract int getId();

    @Shadow @Nullable public abstract Team getTeam();

    @Shadow
    public abstract Level level();

    @Unique
    private FallenCapability revive_Me$getCap(){
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player)) return null;
        revive_Me$cap = FallenCapability.get((Player)entity);

        return this.revive_Me$cap;
    }

    @Inject(
            method = "hasPose",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void hasPose(Pose pose, CallbackInfoReturnable<Boolean> cir){
        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player player)) return;

        if (!FallenCapability.get(player).isFallen()) return;

        switch (ReviveMeConfig.fallenPose){
            case CROUCH -> cir.setReturnValue(pose == Pose.CROUCHING);
            case PRONE -> cir.setReturnValue(pose == Pose.SWIMMING);
            case SLEEP -> cir.setReturnValue(pose == Pose.SLEEPING);
        }
    }

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
