package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

import static invoker54.invocore.client.util.ClientUtil.mC;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(
            method = "shouldEntityAppearGlowing",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void shouldPlayerGlow(Entity entity, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof Player)) return;
        float distance = entity.distanceTo(ClientUtil.getPlayer());
        if (distance < 10 || distance > ReviveMeConfig.reviveGlowMaxDistance) return;
        FallenCapability cap = FallenCapability.get((LivingEntity) entity);
        if (!cap.isFallen()) return;
        if (entity.getTeam() != ClientUtil.getPlayer().getTeam()) return;

        HitResult rayResult = mC.player.level.clip(
                new ClipContext(mC.player.getEyePosition(1.0F), entity.getEyePosition(1.0F)
                        , ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        boolean targetSeen = rayResult.getType() == HitResult.Type.MISS;
        if (!targetSeen && !cap.isCallingForHelp()) return;


        cir.setReturnValue(true);
    }

    @Inject(
            method = "shouldEntityAppearGlowing",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void shouldGlowKillRevive(Entity entity, CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getPlayer() == null) return;
        if (ClientUtil.getPlayer().getEffect(MobEffectInit.KILL_REVIVE_EFFECT) == null) return;
        if (mC.crosshairPickEntity != entity) return;

        cir.setReturnValue(true);
    }

    @Inject(
            method = "startAttack()Z",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void startAttackAsFallen(CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;

        cir.setReturnValue(false);
    }

    @Inject(
            method = "continueAttack(Z)V",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void continueAttackAsFallen(CallbackInfo ci){
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;

        ci.cancel();
    }
}
