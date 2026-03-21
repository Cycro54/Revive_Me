package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class EventHooksMixin {

    @Shadow
    public abstract boolean removeAllEffects();

    //Possible fix for effects not being removed when being revived (false means the effect is removed btw)
    @Redirect(
            method = "removeAllEffects()Z",
            at = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraftforge/eventbus/api/IEventBus;post(Lnet/minecraftforge/eventbus/api/Event;)Z")
    )
    private boolean onEffectRemoved(IEventBus instance, Event event){
        boolean originalValue = MinecraftForge.EVENT_BUS.post(event);
        LivingEntity entity = (LivingEntity)(Object)this;

        if (!(entity instanceof PlayerEntity)) return originalValue;
        FallenCapability data = FallenCapability.get(entity);
        if (!data.isFallen()) return originalValue;
        return false;
    }

}
