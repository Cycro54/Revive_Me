package invoker54.reviveme.init;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.potion.FallenPotionEffect;
import invoker54.reviveme.common.potion.KillRevivePotionEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MobEffectInit {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final MobEffect FALLEN_EFFECT = new FallenPotionEffect(MobEffectCategory.NEUTRAL)
            .setRegistryName(new ResourceLocation(ReviveMe.MOD_ID, "fallen_effect"));

    public static final MobEffect KILL_REVIVE_EFFECT = new KillRevivePotionEffect(MobEffectCategory.HARMFUL)
            .setRegistryName(new ResourceLocation(ReviveMe.MOD_ID, "kill_revive_effect"));
    @SubscribeEvent
    public static void registerEffects(RegistryEvent.Register<MobEffect> event){
        event.getRegistry().registerAll(
                FALLEN_EFFECT,
                KILL_REVIVE_EFFECT
        );
    }
}
