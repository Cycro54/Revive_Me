package invoker54.reviveme.init;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.potion.FallenPotionEffect;
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

    private static final Logger LOGGER = LogManager.getLogger();

    public static final Effect FALLEN_EFFECT = new FallenPotionEffect(EffectType.NEUTRAL)
            .setRegistryName(new ResourceLocation(ReviveMe.MOD_ID, "fallen_effect"));

    @SubscribeEvent
    public static void registerEffects(RegistryEvent.Register<Effect> event){
        event.getRegistry().registerAll(
                FALLEN_EFFECT
        );
    }
}
