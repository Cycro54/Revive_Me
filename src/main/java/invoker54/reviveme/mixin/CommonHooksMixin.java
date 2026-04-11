package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
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
            ((ServerPlayer) entity).sendSystemMessage(InvoText.translate("revive_me.disabled").getText());
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
}
