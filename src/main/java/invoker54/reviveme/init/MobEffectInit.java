package invoker54.reviveme.init;

import invoker54.reviveme.common.potion.FallenPotionEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import static invoker54.reviveme.ReviveMe.MOD_ID;

public class MobEffectInit {

    private static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(
            BuiltInRegistries.MOB_EFFECT,
            MOD_ID
    );


    public static final Holder<MobEffect> FALLEN_EFFECT =
            MOB_EFFECTS.register("fallen_effect", () -> new FallenPotionEffect(MobEffectCategory.NEUTRAL));

    public static void registerEffects(IEventBus eventBus){
        MOB_EFFECTS.register(eventBus);
    }
}
