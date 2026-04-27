package invoker54.reviveme.mixin;

import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EventHooks.class)
public class EventHooksMixin {

    //TODO: REMOVE LATER...
//    //Possible fix for effects not being removed when being revived (false means the effect is removed btw)
//    @Inject(
//            method = "onEffectRemoved(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/effect/MobEffectInstance;)Z",
//            at = {
//                    @At(value = "RETURN")
//            }, cancellable = true)
//    private static void onEffectRemoved(LivingEntity entity, MobEffectInstance effectInstance, CallbackInfoReturnable<Boolean> cir){
//        if (!(entity instanceof Player)) return;
//        FallenData data = FallenData.get(entity);
//        if (!data.isFallen()) return;
//        cir.setReturnValue(false);
//    }

}
