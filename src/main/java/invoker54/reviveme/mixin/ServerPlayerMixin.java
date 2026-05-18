package invoker54.reviveme.mixin;

import com.mojang.authlib.GameProfile;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
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

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow
    @Final
    public ServerPlayerGameMode gameMode;

    @Unique
    private FallenData revive_Me$cap;

    public ServerPlayerMixin(Level level, GameProfile gameProfile) {
        super(level, gameProfile);
    }

    @Unique
    private FallenData revive_Me$getCap(){
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level().getEntity(this.getId());
        if (!(entity instanceof Player)) return null;
        revive_Me$cap = FallenData.get((Player)entity);

        return this.revive_Me$cap;
    }

//    @Inject(
//
//            method = "isCreative",
//            at = {
//                    @At(value = "HEAD")
//            },
//            cancellable = true)
//    private void isCreative(CallbackInfoReturnable<Boolean> cir) {
//
//    }

    @Override
    public boolean isCreative() {
        if (this.gameMode.getGameModeForPlayer() == GameType.CREATIVE) return super.isCreative();
        FallenData cap = FallenData.get(this);
        if (!cap.isFallen()) return super.isCreative();
        if (!ReviveMeConfig.dieWhenTimerEnds && cap.timeRanOut()) return super.isCreative();

        return FallenData.FALLEN_HAS_CREATIVE;
    }

    @Inject(

            method = "hurtServer",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private synchronized void hurtStart(ServerLevel level, DamageSource damageSource, float damage, CallbackInfoReturnable<Boolean> cir) throws InterruptedException {
        if (FallEvent.canBypassReviveMe(damageSource, false)) return;
        if (revive_Me$getCap() == null) return;
        if (!revive_Me$getCap().isFallen()) return;

        boolean sourceIsPlayer = (damageSource.getEntity() instanceof Player);
        boolean playerIsCrouching = (sourceIsPlayer && damageSource.getEntity().isShiftKeyDown());
        boolean killTimerIsExpired = revive_Me$getCap().getKillTime(false) == 0;
        boolean actualDamage = damage > 0;

        if (playerIsCrouching && killTimerIsExpired && actualDamage) {
            revive_Me$getCap().setDamageSource(damageSource);

            revive_Me$getCap().forceDeath();
        }

        if (!ReviveMeConfig.dieWhenTimerEnds && revive_Me$getCap().timeRanOut()) return;

        cir.setReturnValue(false);
    }
}
