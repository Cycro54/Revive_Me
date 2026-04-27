package invoker54.reviveme.common.potion;

import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FallenPotionEffect extends MobEffect {
    public static List<UUID> warnedPlayers = new ArrayList<>();

    public static final int effectColor = new Color(35, 5, 5, 255).getRGB();

    public FallenPotionEffect(MobEffectCategory category) {
        super(category, effectColor);
    }

//    @Override
//    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
//        super.fillEffectCures(cures, effectInstance);
//    }

    @EventBusSubscriber(modid = ReviveMe.MOD_ID)
    public static class PotionEvents{

        //This will set the used reviveMethod to none in the fallen capability (unless the player has been downed again)
        @SubscribeEvent
        public static void removeFallMethod(MobEffectEvent.Expired event){
            removePenalties(event.getEntity(), event.getEffectInstance(), true);
        }

        @SubscribeEvent
        public static void onRemove(MobEffectEvent.Remove event){
            event.setCanceled(removePenalties(event.getEntity(), event.getEffectInstance(), false));
        }

        public static boolean removePenalties(LivingEntity entity, MobEffectInstance effect, boolean completed){
            if (effect == null) return false;
            if (!(effect.getEffect().value() instanceof FallenPotionEffect)) return false;
            if (!(entity instanceof Player)) return false;

            FallenData cap = FallenData.get(entity);
            if (cap.isFallen()) return false;

            if (!completed && !ReviveMeConfig.canRemovePenaltyTimer && !((Player) entity).isCreative()){
                if (!warnedPlayers.contains(entity.getUUID())){
                    ((Player) entity).sendSystemMessage(InvoText.translate("effect.reviveme.fallen_effect.cant_remove").getText());
                    warnedPlayers.add(entity.getUUID());
                }
                return true;
            }

            cap.resetReviveCount();
            cap.setPenaltyMultiplier(0);
            return false;
        }

        @SubscribeEvent
        public static void removeWarnedPlayer(PlayerEvent.PlayerLoggedOutEvent event){
            warnedPlayers.remove(event.getEntity().getUUID());
        }
    }
}
