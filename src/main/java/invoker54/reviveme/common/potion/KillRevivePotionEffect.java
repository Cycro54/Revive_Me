package invoker54.reviveme.common.potion;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class KillRevivePotionEffect extends MobEffect {
    public static final int effectColor = new Color(35, 5, 5,255).getRGB();

    public KillRevivePotionEffect(MobEffectCategory category){
        super(category, effectColor);
    }

    public static DamageSource killReviveDamageSource = new DamageSource("revive-me.damage_source.kill_revive")
            .bypassArmor().bypassInvul().bypassMagic();

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>();
    }

    public static boolean isAllowedEntity(Entity entity){
        if (entity == null) return false;

        boolean isWhitelist = ReviveMeConfig.reviveKillBlackList.contains("//");
        List<String> classificationList = ReviveMeConfig.reviveKillBlackList.stream()
                .filter(s -> StringUtils.countMatches(s, ";") == 2)
                .map(s -> s.replace(";", "")).collect(Collectors.toList());

        boolean hasMatch = classificationList.contains(entity.getType().getCategory().toString());

        if (!hasMatch){
            hasMatch = ReviveMeConfig.reviveKillBlackList.stream().anyMatch(listString ->
                    entity.getType().getRegistryName().toString().contains(listString));
        }

        return isWhitelist == hasMatch;
    }

    @Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
    public static class PotionEvents{

        @SubscribeEvent
        public static void killMobEvent(LivingDeathEvent event){
            Entity sourceEntity = event.getSource().getEntity();
            if (!(sourceEntity instanceof LivingEntity)) return;
            LivingEntity entity = (LivingEntity) sourceEntity;
            MobEffectInstance instance = entity.getEffect(MobEffectInit.KILL_REVIVE_EFFECT);
            if (instance == null) return;
            if (!isAllowedEntity(event.getEntityLiving())) return;

            entity.removeEffect(MobEffectInit.KILL_REVIVE_EFFECT);

            if (instance.getAmplifier() > 0) {
                entity.removeEffect(MobEffectInit.KILL_REVIVE_EFFECT);

                entity.addEffect(new MobEffectInstance(
                        MobEffectInit.KILL_REVIVE_EFFECT, instance.getDuration(), instance.getAmplifier() - 1));
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

        public static void removeEffect(LivingEntity entity, MobEffectInstance effect, boolean completed){
            if (effect == null) return;
            if (!(effect.getEffect() instanceof KillRevivePotionEffect)) return;
            if (!(entity instanceof ServerPlayer)){
                if (completed) entity.hurt(killReviveDamageSource, 20 * (effect.getAmplifier() + 1));
                return;
            }

            FallenCapability cap = FallenCapability.get(entity);
            if (cap.isFallen()) return;

            if (completed) return;

            entity.setHealth(0.00000001F);
            entity.hurt(killReviveDamageSource, 1);
        }
    }
}

