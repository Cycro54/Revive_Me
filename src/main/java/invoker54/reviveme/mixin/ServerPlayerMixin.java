package invoker54.reviveme.mixin;

import com.mojang.authlib.GameProfile;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerMixin extends PlayerEntity {

    @Unique
    private ModLogger LOGGERT = ModLogger.getLogger(ServerPlayerMixin.class, ReviveMeConfig.debugMode);

    @Shadow @Final public PlayerInteractionManager gameMode;

    @Unique
    private FallenCapability revive_Me$cap;
    @Unique
    private FallenCapability revive_Me$getCap(){
        if (this.revive_Me$cap != null) return this.revive_Me$cap;

        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof PlayerEntity)) return null;
        revive_Me$cap = FallenCapability.GetFallCap((PlayerEntity)entity);

        return this.revive_Me$cap;
    }

    public ServerPlayerMixin(World p_i241920_1_, BlockPos p_i241920_2_, float p_i241920_3_, GameProfile p_i241920_4_) {
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

        if ((damageSource.getEntity() instanceof PlayerEntity)
                && damageSource.getEntity().isCrouching() && revive_Me$getCap().getKillTime(false) == 0) {
            revive_Me$getCap().setDamageSource(damageSource);
            revive_Me$getCap().kill((PlayerEntity) this.level.getEntity(this.getId()));
        }

        cir.setReturnValue(false);
    }
}
