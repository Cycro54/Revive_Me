package invoker54.reviveme.common.potion;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;

import java.awt.*;
import java.util.Set;

public class FallenPotionEffect extends MobEffect {
    public static final int effectColor = new Color(35, 5, 5,255).getRGB();

    public FallenPotionEffect(MobEffectCategory category){
        super(category, effectColor);
    }


    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        cures.clear();
    }

    @EventBusSubscriber(modid = ReviveMe.MOD_ID)
    public static class PotionEvents{

        //This will set the used reviveMethod to none in the fallen capability (unless the player has been downed again)
        @SubscribeEvent
        public static void removeFallMethod(MobEffectEvent.Expired event){

            removePenalties(event.getEntity(), event.getEffectInstance());
        }

        @SubscribeEvent
        public static void onRemove(MobEffectEvent.Remove event){
            removePenalties(event.getEntity(), event.getEffectInstance());
        }

        public static void removePenalties(LivingEntity entity, MobEffectInstance effect){
            if (effect == null) return;
            if (!(effect.getEffect().value() instanceof FallenPotionEffect)) return;
            if (!(entity instanceof Player player)) return;

            FallenData cap = FallenData.get(player);
            if (cap.isFallen()) return;

            cap.resetSelfReviveCount();
            cap.setPenaltyMultiplier(0);
        }

    }
}
