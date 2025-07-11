package invoker54.reviveme.mixin;

import invoker54.invocore.common.MathUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
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
    @Shadow public World level;

    @Unique
    private FallenCapability revive_Me$cap;

    @Unique
    private boolean revive_Me$grabOriginal = false;

    @Shadow public abstract int getId();

    @Shadow public abstract String toString();

    @Shadow @Nullable public abstract Team getTeam();

    @Shadow public abstract boolean equals(Object p_equals_1_);

    @Unique
    private FallenCapability revive_Me$getCap(){
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof PlayerEntity)) return null;
        revive_Me$cap = FallenCapability.GetFallCap((PlayerEntity)entity);

        return this.revive_Me$cap;
    }

    @Inject(
            method = "getTeamColor",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getTeamColor(CallbackInfoReturnable<Integer> cir){
        if (!this.level.isClientSide) return;
        FallenCapability cap = revive_Me$getCap();
        if (cap == null) return;
        if (!cap.isFallen()) return;
        double timePassed = (cap.callForHelpTicks()/20d);

        Team team = this.getTeam();
        int preColor;

        if (timePassed < 3 && timePassed % 1 < 0.5F){
            preColor = 16777215;
        }
        else if (team != null && team.getColor().getColor() != null){
            preColor = team.getColor().getColor();
        }
        else {
            preColor = new Color(248, 80, 29,255).getRGB();
        }

        Color postColor = new Color(preColor);
        if (ReviveMeConfig.timeLeft != 0 && preColor != 16777215) {
            float percentLeft = Math.max(0, Math.min(cap.GetTimeLeft(true), 1));
            postColor = new Color(
                    Math.round(postColor.getRed() * percentLeft),
                    Math.round(postColor.getGreen() * percentLeft),
                    Math.round(postColor.getBlue() * percentLeft));
        }

        cir.setReturnValue(postColor.getRGB());
    }

    @Inject(
            method = "getPose",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getPose(CallbackInfoReturnable<Pose> cir){
        if (this.revive_Me$grabOriginal)return;
        if (!this.level.isClientSide) return;

        if (revive_Me$getCap() == null) return;
        if (!revive_Me$getCap().isFallen()) return;

        switch (ReviveMeConfig.fallenPose) {
            case CROUCH:
                cir.setReturnValue(Pose.CROUCHING);
                break;
            case PRONE:
                cir.setReturnValue(Pose.SWIMMING);
                break;
            case SLEEP:
                cir.setReturnValue(Pose.SLEEPING);
                break;
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
