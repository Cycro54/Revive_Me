package invoker54.reviveme.common.potion;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.init.EffectInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.system.CallbackI;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class KillRevivePotionEffect extends Effect {
    public static final int effectColor = new Color(35, 5, 5,255).getRGB();

    public KillRevivePotionEffect(EffectType category){
        super(category, effectColor);
    }

    public static DamageSource killReviveDamageSource = new DamageSource("revive-me.damage_source.kill_revive")
            .bypassArmor().bypassInvul().bypassMagic();

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    @Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
    public static class PotionEvents{

        @SubscribeEvent
        public static void killMobEvent(LivingDeathEvent event){
            Entity sourceEntity = event.getSource().getEntity();
            if (!(sourceEntity instanceof PlayerEntity)) return;
            LivingEntity entity = (LivingEntity) sourceEntity;
            EffectInstance instance = entity.getEffect(EffectInit.KILL_REVIVE_EFFECT);
            if (instance == null) return;
            entity.removeEffect(EffectInit.KILL_REVIVE_EFFECT);
            if (instance.getAmplifier() > 0) {
                entity.removeEffect(EffectInit.KILL_REVIVE_EFFECT);

                entity.addEffect(new EffectInstance(
                        EffectInit.KILL_REVIVE_EFFECT, instance.getDuration(), instance.getAmplifier() - 1));
            }
        }

        //This will set the used reviveMethod to none in the fallen capability (unless the player has been downed again)
        @SubscribeEvent
        public static void expireEvent(PotionEvent.PotionExpiryEvent event){
            removeEffect(event.getEntityLiving(), event.getPotionEffect(), false);
        }

        @SubscribeEvent
        public static void removeEvent(PotionEvent.PotionRemoveEvent event){
            removeEffect(event.getEntityLiving(), event.getPotionEffect(), true);
        }

        public static void removeEffect(LivingEntity entity, EffectInstance effect, boolean completed){
            if (effect == null) return;
            if (!(effect.getEffect() instanceof KillRevivePotionEffect)) return;
            if (!(entity instanceof PlayerEntity)) return;

            FallenCapability cap = FallenCapability.GetFallCap(entity);
            if (cap.isFallen()) return;

            if (completed) return;

            entity.setHealth(0.00000001F);
            entity.hurt(killReviveDamageSource, 1);
        }
    }
}

