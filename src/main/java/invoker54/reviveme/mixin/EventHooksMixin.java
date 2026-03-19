package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EventHooks.class)
public class EventHooksMixin {

    //Possible fix for effects not being removed when being revived (false means the effect is removed btw)
    @Inject(
            method = "onEffectRemoved(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/effect/MobEffectInstance;Lnet/neoforged/neoforge/common/EffectCure;)Z",
            at = {
                    @At(value = "RETURN")
            }, cancellable = true)
    private static void onEffectRemoved(LivingEntity entity, MobEffectInstance effectInstance, EffectCure cure, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof Player)) return;
        FallenData data = FallenData.get(entity);
        if (!data.isFallen()) return;
        cir.setReturnValue(false);
    }

}
