package invoker54.reviveme.mixin;

import com.mojang.authlib.GameProfile;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Collectors;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Unique
    private FallenCapability revive_Me$cap;
    @Unique
    private FallenCapability revive_Me$getCap(){
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player)) return null;
        revive_Me$cap = FallenCapability.get((Player)entity);

        return this.revive_Me$cap;
    }

    public ServerPlayerMixin(Level p_i241920_1_, BlockPos p_i241920_2_, float p_i241920_3_, GameProfile p_i241920_4_) {
        super(p_i241920_1_, p_i241920_2_, p_i241920_3_, p_i241920_4_);
    }

    @Inject(

            method = "isCreative",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isCreative(CallbackInfoReturnable<Boolean> cir){
        if (this.gameMode.getGameModeForPlayer() == GameType.CREATIVE) return;
        FallenCapability cap = FallenCapability.get(this);
        if (!cap.isFallen()) return;
        boolean isTargetable = ReviveMeConfig.fallenIsTargetable;
        if (revive_Me$getCap().timeRanOut() && ReviveMeConfig.timerType == ReviveMeConfig.TIMER_TYPE.TARGETABLE) isTargetable = !isTargetable;
        if (isTargetable) return;

        cir.setReturnValue(FallenCapability.FALLEN_HAS_CREATIVE);
    }

    @Inject(

            method = "hurt",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private synchronized void hurtStart(DamageSource damageSource, float damage, CallbackInfoReturnable<Boolean> cir) throws InterruptedException {
        if (FallEvent.canBypassReviveMe(damageSource, false)) return;
        if (revive_Me$getCap() == null) return;
        if (!revive_Me$getCap().isFallen()) return;

        boolean sourceIsPlayer = (damageSource.getEntity() instanceof Player);
        boolean playerIsCrouching = (sourceIsPlayer && damageSource.getEntity().isShiftKeyDown());
        boolean killTimerIsExpired = revive_Me$getCap().getKillTime(false) == 0;
        boolean actualDamage = damage > 0;
        boolean isTargetable = ReviveMeConfig.fallenIsTargetable;
        if (revive_Me$getCap().timeRanOut() && ReviveMeConfig.timerType == ReviveMeConfig.TIMER_TYPE.TARGETABLE) isTargetable = !isTargetable;
        if (!actualDamage) return;
        boolean canDoOverkill = this.revive_Me$doOverkill(damageSource, this);
        revive_Me$getCap().setCanDoOverkill(canDoOverkill);

        //Holding sneak bypasses the hurt thing.
        if (playerIsCrouching && killTimerIsExpired) return;
        if (!sourceIsPlayer && isTargetable) return;
        if ((sourceIsPlayer || damageSource.getEntity() == null) && canDoOverkill) return;

        cir.setReturnValue(false);
    }

    @Unique
    private boolean revive_Me$doOverkill(DamageSource damageSource, Player damaged){
        if (ReviveMeConfig.overkillAmount == 0) return false;

        Entity attacker = damageSource.getEntity();
        boolean foundType = false;
        boolean foundSource = false;
        boolean isWhitelist = !ReviveMeConfig.overkillWhitelist.contains("//");
        List<String> typeList = ReviveMeConfig.overkillWhitelist.stream().filter(a -> a.startsWith(";") && a.endsWith(";"))
                .map(a -> a.replaceAll(";","")).collect(Collectors.toList());
        //';NON_ENTITY;, ';NON_TEAM;', ';TEAM;', ';SELF;', ';MOB;', ';PLAYER;'
        if (typeList.isEmpty()) foundType = true;
        else if (attacker == null) foundType = typeList.contains("NON_ENTITY");
        else if (typeList.contains("NON_TEAM") && attacker.getTeam() == null || attacker.getTeam() != damaged.getTeam()) foundType = true;
        else if (typeList.contains("TEAM") && attacker.getTeam() != null && attacker.getTeam() == damaged.getTeam()) foundType = true;
        else if (typeList.contains("SELF") && attacker == damaged) foundType = true;
        else if (typeList.contains("MOB") && attacker instanceof Mob) foundType = true;
        else if (typeList.contains("PLAYER") && attacker instanceof Player) foundType = true;

        List<String> sourceList = ReviveMeConfig.overkillWhitelist.stream().filter(a -> !a.contains(".*;.*;.*") && !a.contains("//")).collect(Collectors.toList());
        if (sourceList.contains("/")) foundSource = true;
        else if (foundType){
            String idString = damageSource.getMsgId();
            for (String listString : sourceList){
                if (!idString.contains(listString)) continue;
                foundSource = true;
                break;
            }
        }

        boolean isMatch = foundType && foundSource;

        FallenCapability cap = FallenCapability.get(damaged);
        boolean canRevive = (cap.canSelfRevive() && (damageSource.getEntity() == null || damageSource.getEntity() == damaged))
                || (cap.canPlayerRevive() && damageSource.getEntity() != damaged && damageSource.getEntity() != null);

        return isMatch == isWhitelist && canRevive;
    }

}
