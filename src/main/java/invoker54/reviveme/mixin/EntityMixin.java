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
    @Shadow
    public Level level;

    @Unique
    private FallenCapability revive_Me$cap;

    @Shadow
    public abstract int getId();

    @Shadow @Nullable public abstract Team getTeam();

    @Unique
    private FallenCapability revive_Me$getCap() {
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player)) return null;
        revive_Me$cap = FallenCapability.GetFallCap((Player) entity);

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
            method = "hasPose",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void hasPose(Pose pose, CallbackInfoReturnable<Boolean> cir) {
        if (!this.level.isClientSide) return;
        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player player)) return;

        if (!FallenCapability.GetFallCap(player).isFallen()) return;

        switch (ReviveMeConfig.fallenPose) {
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
    private void isInvulnerable(CallbackInfoReturnable<Boolean> cir) {
        if (revive_Me$getCap() == null) return;
        if (!revive_Me$getCap().isFallen()) return;

        cir.setReturnValue(true);
    }

}
