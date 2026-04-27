package invoker54.reviveme.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public abstract class EventHooksMixin {

    //TODO: REMOVE LATER...
//    @Shadow
//    public abstract boolean removeAllEffects();
//
//    //Possible fix for effects not being removed when being revived (false means the effect is removed btw)
//    @Redirect(
//            method = "removeAllEffects()Z",
//            at = @At(
//                            value = "INVOKE",
//                            target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z")
//    )
//    private boolean onEffectRemoved(IEventBus instance, Event event){
//        boolean originalValue = MinecraftForge.EVENT_BUS.post(event);
//        LivingEntity entity = (LivingEntity)(Object)this;
//
//        if (!(entity instanceof Player)) return originalValue;
//        FallenCapability data = FallenCapability.get(entity);
//        if (!data.isFallen()) return originalValue;
//        return false;
//    }

}
