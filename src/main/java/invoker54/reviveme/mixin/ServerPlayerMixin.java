package invoker54.reviveme.mixin;

import com.mojang.authlib.GameProfile;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.core.BlockPos;
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

    @Shadow @Final public ServerPlayerGameMode gameMode;

    @Unique
    private FallenCapability revive_Me$cap;
    @Unique
    private FallenCapability revive_Me$getCap(){
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player)) return null;
        revive_Me$cap = FallenCapability.GetFallCap((Player)entity);

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
        FallenCapability cap = FallenCapability.GetFallCap(this);
        if (!cap.isFallen()) return;

        cir.setReturnValue(true);
    }

    @Inject(

            method = "hurt",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private synchronized void hurtStart(DamageSource damageSource, float damage, CallbackInfoReturnable<Boolean> cir) throws InterruptedException {
        if (damageSource.isBypassInvul()) return;
        if (revive_Me$getCap() == null) return;
        if (!revive_Me$getCap().isFallen()) return;

        if ((damageSource.getEntity() instanceof Player)
                && damageSource.getEntity().isCrouching() && revive_Me$getCap().getKillTime(false) == 0) {
            revive_Me$getCap().setDamageSource(damageSource);
            revive_Me$getCap().kill((Player) this.level.getEntity(this.getId()));
        }

        cir.setReturnValue(false);
    }
}
