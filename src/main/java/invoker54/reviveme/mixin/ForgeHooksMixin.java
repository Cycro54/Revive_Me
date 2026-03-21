package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
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
}
