package invoker54.reviveme.common.potion;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class FallenPotionEffect extends MobEffect {
    public static final int effectColor = new Color(35, 5, 5,255).getRGB();

    public FallenPotionEffect(MobEffectCategory category){
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
        public static void removeFallMethod(MobEffectEvent.Expired event){
            removePenalties(event.getEntity(), event.getEffectInstance());
        }

        @SubscribeEvent
        public static void onRemove(MobEffectEvent.Remove event){
            removePenalties(event.getEntity(), event.getEffectInstance());
        }

        public static void removePenalties(LivingEntity entity, MobEffectInstance effect){
            if (effect == null) return;
            if (!(effect.getEffect() instanceof FallenPotionEffect)) return;
            if (!(entity instanceof Player player)) return;

            FallenCapability cap = FallenCapability.GetFallCap(player);
            if (cap.isFallen()) return;

            cap.setSacrificedItemsUsed(false);
            cap.setReviveChanceUsed(false);
        }

    }
}
