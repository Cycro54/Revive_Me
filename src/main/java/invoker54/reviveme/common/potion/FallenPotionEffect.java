package invoker54.reviveme.common.potion;

import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class FallenPotionEffect extends Effect {
    public static final int effectColor = new Color(35, 5, 5,255).getRGB();

    public FallenPotionEffect(EffectType category){
        super(category, effectColor);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return Arrays.asList(new ItemStack(Items.MILK_BUCKET));
    }

    @Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
    public static class PotionEvents{

        //This will set the used reviveMethod to none in the fallen capability (unless the player has been downed again)
        @SubscribeEvent
        public static void removeFallMethod(PotionEvent.PotionExpiryEvent event){
            removePenalties(event.getEntityLiving(), event.getPotionEffect(), true);
        }

        @SubscribeEvent
        public static void onRemove(PotionEvent.PotionRemoveEvent event){
            event.setCanceled(removePenalties(event.getEntityLiving(), event.getPotionEffect(), false));
        }

        public static boolean removePenalties(LivingEntity entity, EffectInstance effect, boolean completed){
            if (effect == null) return false;
            if (!(effect.getEffect() instanceof FallenPotionEffect)) return false;
            if (!(entity instanceof PlayerEntity)) return false;

            FallenCapability cap = FallenCapability.get(entity);
            if (cap.isFallen()) return false;

            if (!completed && !ReviveMeConfig.canRemovePenaltyTimer && !((PlayerEntity) entity).isCreative()){
                entity.sendMessage(InvoText.translate("effect.reviveme.fallen_effect.cant_remove").getText(),entity.getUUID());
                return true;
            }

            cap.resetSelfReviveCount();
            cap.setPenaltyMultiplier(0);
            return false;
        }
    }
}
