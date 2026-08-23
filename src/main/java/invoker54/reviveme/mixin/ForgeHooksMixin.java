package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.GameType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooks.class)
public abstract class ForgeHooksMixin {

    @Unique
    private static ModLogger LOGGERT = ModLogger.getLogger(ForgeHooksMixin.class, ReviveMeConfig.debugMode);

    @Inject(
            remap = false,
            method = "onLivingDeath",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onLivingDeath(LivingEntity entity, DamageSource src, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof ServerPlayerEntity)) return;
        if ((((ServerPlayerEntity) entity).gameMode.getGameModeForPlayer() == GameType.CREATIVE)) return;
        if (!ReviveMeConfig.reviveMeEnabled) {
            ((ServerPlayerEntity) entity).displayClientMessage(InvoText.translate("revive_me.disabled").getText(), false);
            return;
        }

        boolean cancelled;

        if (!FallenCapability.get(entity).canDieThisTick()){
            entity.setHealth(1);
            cancelled = true;
        }
        else if (ReviveMeConfig.runDeathEventFirst){
            cancelled = MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
            if (!cancelled) cancelled = FallEvent.cancelEvent((PlayerEntity) entity, src);
        }
        else {
            cancelled = FallEvent.cancelEvent((PlayerEntity) entity, src);
            if (!cancelled) cancelled = MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
        }

        cir.setReturnValue(cancelled);
    }

    @Inject(
            remap = false,
            method = "Lnet/minecraftforge/common/ForgeHooks;onLivingHurt(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/util/DamageSource;F)F",
            at = {
                    @At(value = "RETURN")
            },
            cancellable = true)
    private static void onLivingHurt(LivingEntity entity, DamageSource src, float amount, CallbackInfoReturnable<Float> cir){
        if (!(entity instanceof PlayerEntity)) return;
        FallenCapability cap = FallenCapability.get(entity);
        if (!cap.isFallen()) return;
        if (cir.getReturnValue() <= 0) return;
        if (!cap.canDoOverkill(src)) return;
        boolean isComplete = cap.addOverkill(cir.getReturnValue());
        cap.syncClient(false);

        Entity entity1 = src.getEntity();

        if (entity1 != null) {
            double d1 = entity1.getX() - entity.getX();

            double d0;
            for(d0 = entity1.getZ() - entity.getZ(); d1 * d1 + d0 * d0 < 1.0E-4D; d0 = (Math.random() - Math.random()) * 0.01D) {
                d1 = (Math.random() - Math.random()) * 0.01D;
            }

            entity.hurtDir = (float)(MathHelper.atan2(d0, d1) * (double)(180F / (float)Math.PI) - (double)entity.yRot);
            entity.knockback(0.4F, d1, d0);
        } else {
            entity.hurtDir = (float)((int)(Math.random() * 2.0D) * 180);
        }

        cir.setReturnValue(0f);
        if (!isComplete) return;

        cap.incrementReviveCount(src.getEntity() == null ? entity : src.getEntity());
        ReviveMeConfig.configReviveData.revivePlayer((PlayerEntity) entity, false,
                src.getEntity() == null ? entity : src.getEntity(), "overkill");
    }
}
