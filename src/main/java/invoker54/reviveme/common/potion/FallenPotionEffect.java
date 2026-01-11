package invoker54.reviveme.common.potion;

import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class FallenPotionEffect extends MobEffect {
    public static final int effectColor = new Color(35, 5, 5,255).getRGB();

    public FallenPotionEffect(MobEffectCategory category){
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

        public static boolean removePenalties(LivingEntity entity, MobEffectInstance effect, boolean completed){
            if (effect == null) return false;
            if (!(effect.getEffect() instanceof FallenPotionEffect)) return false;
            if (!(entity instanceof Player)) return false;

            FallenCapability cap = FallenCapability.get(entity);
            if (cap.isFallen()) return false;

            if (!completed && !ReviveMeConfig.canRemovePenaltyTimer && !((Player) entity).isCreative()){
                entity.sendMessage(InvoText.translate("effect.reviveme.fallen_effect.cant_remove").getText(),entity.getUUID());
                return true;
            }

            cap.resetSelfReviveCount();
            cap.setPenaltyMultiplier(0);
            return false;
        }
    }
}
