package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.potion.KillRevivePotionEffect;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
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
public abstract class ClientEntityMixin {
    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(ClientEntityMixin.class, ReviveMeConfig.debugMode);

    @Shadow
    public Level level;

    @Unique
    private FallenData revive_Me$cap;

    @Shadow public abstract int getId();

    @Shadow public abstract String toString();

    @Shadow public abstract boolean equals(Object p_equals_1_);

    @Shadow
    @Nullable
    public abstract PlayerTeam getTeam();

    @Unique
    private FallenData revive_Me$getCap(){
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player)) return null;
        revive_Me$cap = FallenData.get((Player)entity);

        return this.revive_Me$cap;
    }

    @Inject(
            method = "getTeamColor",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getFallenColor(CallbackInfoReturnable<Integer> cir){
        FallenData cap = revive_Me$getCap();
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
        if (ReviveMeConfig.timeLeft > 0 && preColor != 16777215) {
            float percentLeft = Math.max(0, Math.min(cap.getTimeLeft(true), 1));
            postColor = new Color(
                    Math.round(postColor.getRed() * percentLeft),
                    Math.round(postColor.getGreen() * percentLeft),
                    Math.round(postColor.getBlue() * percentLeft));
        }

        cir.setReturnValue(postColor.getRGB());
    }

    @Inject(
            method = "getTeamColor",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getKillReviveColor(CallbackInfoReturnable<Integer> cir){
//        LOGGERT.warn("The start");
        Player player = ClientUtil.getPlayer();
        if (player == null) return;
//        LOGGERT.warn("Does player have effect...");
        if (player.getEffect(MobEffectInit.KILL_REVIVE_EFFECT) == null) return;
        Entity entity = this.level.getEntity(this.getId());
//        LOGGERT.warn("What's the entity? " + entity.getName());
        if (ClientUtil.getMinecraft().crosshairPickEntity != entity) return;

        cir.setReturnValue(
                KillRevivePotionEffect.isAllowedEntity(entity) ?
                        new Color(165, 239, 80, 255).getRGB() : new Color(48, 11, 10, 255).getRGB());
    }
}
