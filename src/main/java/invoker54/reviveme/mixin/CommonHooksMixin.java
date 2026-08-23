package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {

    @Inject(
            remap = false,
            method = "onLivingDeath",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onLivingDeath(LivingEntity entity, DamageSource src, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof ServerPlayer)) return;
        if ((((ServerPlayer) entity).gameMode.getGameModeForPlayer() == GameType.CREATIVE)) return;
        if (!ReviveMeConfig.reviveMeEnabled){
            ((ServerPlayer) entity).displayClientMessage(InvoText.translate("revive_me.disabled").getText(), false);
            return;
        }

        boolean cancelled;

        if (!FallenData.get(entity).canDieThisTick()){
            entity.setHealth(1);
            cancelled = true;
        }
        else if (ReviveMeConfig.runDeathEventFirst){
            cancelled = NeoForge.EVENT_BUS.post(new LivingDeathEvent(entity, src)).isCanceled();
            if (!cancelled) cancelled = FallEvent.cancelEvent((Player) entity, src);
        }
        else {
            cancelled = FallEvent.cancelEvent((Player) entity, src);
            if (!cancelled) cancelled = NeoForge.EVENT_BUS.post(new LivingDeathEvent(entity, src)).isCanceled();
        }

        cir.setReturnValue(cancelled);
    }

    @Inject(
            remap = false,
            method = "onLivingDamagePre",
            at = {
                    @At(value = "RETURN")
            },
            cancellable = true)
    private static void onLivingHurt(LivingEntity entity, DamageContainer container, CallbackInfoReturnable<Float> cir) {
    if (!(entity instanceof Player)) return;
        FallenData cap = FallenData.get(entity);
        if (!cap.isFallen()) return;
        if (cir.getReturnValue() <= 0) return;
        DamageSource src = container.getSource();
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

            entity.animateHurt ((float)(Mth.atan2(d0, d1) * (double)(180F / (float)Math.PI) - (double)entity.getYRot()));
            entity.knockback(0.4F, d1, d0);
        } else {
            entity.animateHurt ((float)((int)(Math.random() * 2.0D) * 180));
        }

        container.setNewDamage(0);
        cir.setReturnValue(0f);
        if (!isComplete) return;

        cap.incrementReviveCount(src.getEntity() == null ? entity : src.getEntity());
        ReviveMeConfig.configReviveData.revivePlayer((Player) entity, false,
                src.getEntity() == null ? entity : src.getEntity(), "overkill");
    }
}
