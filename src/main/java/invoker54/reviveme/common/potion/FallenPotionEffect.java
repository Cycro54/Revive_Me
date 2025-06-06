package invoker54.reviveme.common.potion;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FallenPotionEffect extends Effect {
    public static final int effectColor = new Color(35, 5, 5,255).getRGB();

    public FallenPotionEffect(EffectType category){
        super(category, effectColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    @Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
    public static class PotionEvents{

        //This will set the used reviveMethod to none in the fallen capability (unless the player has been downed again)
        @SubscribeEvent
        public static void removeFallMethod(PotionEvent.PotionExpiryEvent event){
            removePenalties(event.getEntityLiving(), event.getPotionEffect());
        }

        @SubscribeEvent
        public static void onRemove(PotionEvent.PotionRemoveEvent event){
            removePenalties(event.getEntityLiving(), event.getPotionEffect());
        }

        public static void removePenalties(LivingEntity entity, EffectInstance effect){
            if (effect == null) return;
            if (!(effect.getEffect() instanceof FallenPotionEffect)) return;
            if (!(entity instanceof PlayerEntity)) return;

            FallenCapability cap = FallenCapability.GetFallCap(entity);
            if (cap.isFallen()) return;

            cap.resetSelfReviveCount();
            cap.setPenaltyMultiplier(0);
        }

    }
}
