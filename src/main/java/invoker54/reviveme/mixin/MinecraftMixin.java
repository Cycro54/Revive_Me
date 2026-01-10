package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.EffectInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
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

    @Shadow
    @Nullable
    public ClientPlayerEntity player;

    @Shadow
    @Nullable
    public Entity crosshairPickEntity;

    @Inject(
            method = "shouldEntityAppearGlowing",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void shouldPlayerGlow(Entity entity, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof PlayerEntity)) return;
        float distance = entity.distanceTo(ClientUtil.getPlayer());
        if (distance < 10 || distance > ReviveMeConfig.reviveGlowMaxDistance) return;
        FallenCapability cap = FallenCapability.get((LivingEntity) entity);
        if (!cap.isFallen()) return;

        BlockRayTraceResult rayResult = mC.player.level.clip(
                new RayTraceContext(mC.player.getEyePosition(1.0F), entity.getEyePosition(1.0F)
                        , RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
        boolean targetSeen = rayResult.getType() == RayTraceResult.Type.MISS;
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
        if (ClientUtil.getPlayer().getEffect(EffectInit.KILL_REVIVE_EFFECT) == null) return;
        if (mC.crosshairPickEntity != entity) return;

        cir.setReturnValue(true);
    }

    @Inject(
            method = "startAttack()V",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void startAttackAsFallen(CallbackInfo ci){
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;

        ci.cancel();
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
