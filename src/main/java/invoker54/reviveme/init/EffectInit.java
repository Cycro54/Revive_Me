package invoker54.reviveme.init;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.potion.FallenPotionEffect;
import invoker54.reviveme.common.potion.KillRevivePotionEffect;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EffectInit {

    private static final ModLogger LOGGER = ModLogger.getLogger(EffectInit.class, ReviveMeConfig.debugMode);

    public static final Effect FALLEN_EFFECT = new FallenPotionEffect(EffectType.NEUTRAL)
            .setRegistryName(new ResourceLocation(ReviveMe.MOD_ID, "fallen_effect"));

    public static final Effect KILL_REVIVE_EFFECT = new KillRevivePotionEffect(EffectType.HARMFUL)
            .setRegistryName(new ResourceLocation(ReviveMe.MOD_ID, "kill_revive_effect"));

    @SubscribeEvent
    public static void registerEffects(RegistryEvent.Register<Effect> event){
        event.getRegistry().registerAll(
                FALLEN_EFFECT,
                KILL_REVIVE_EFFECT
        );
    }
}
