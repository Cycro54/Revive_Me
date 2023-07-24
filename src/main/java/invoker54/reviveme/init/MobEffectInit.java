package invoker54.reviveme.init;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.potion.FallenPotionEffect;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class MobEffectInit {

    private static final Logger LOGGER = LogManager.getLogger();

    public static final MobEffect FALLEN_EFFECT = new FallenPotionEffect(MobEffectCategory.NEUTRAL);

    @SubscribeEvent
    public static void registerEffects(RegisterEvent event){
        event.register(ForgeRegistries.Keys.MOB_EFFECTS,
                helper -> {
            helper.register(new ResourceLocation(ReviveMe.MOD_ID, "fallen_effect"), FALLEN_EFFECT);

                });
    }
}
