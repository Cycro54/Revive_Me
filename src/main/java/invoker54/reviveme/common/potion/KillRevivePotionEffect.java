
package invoker54.reviveme.common.potion;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.DamageTypeInit;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import org.apache.commons.lang3.StringUtils;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KillRevivePotionEffect extends MobEffect {
    private static final ModLogger LOGGERT = ModLogger.getLogger(KillRevivePotionEffect.class, ReviveMeConfig.debugMode);

    public static final int effectColor = new Color(35, 5, 5, 255).getRGB();

    public KillRevivePotionEffect(MobEffectCategory category) {
        super(category, effectColor);
    }

//    @Override
//    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
//        cures.clear();
//    }

    public static boolean isAllowedEntity(Entity entity){
        if (entity == null) return false;

        boolean isWhitelist = ReviveMeConfig.reviveKillBlackList.contains("//");
        List<String> classificationList = ReviveMeConfig.reviveKillBlackList.stream()
                .filter(s -> StringUtils.countMatches(s, ";") == 2)
                .map(s -> s.replace(";", "")).collect(Collectors.toList());

        boolean hasMatch = classificationList.contains(entity.getType().getCategory().toString());

        if (!hasMatch){
            hasMatch = ReviveMeConfig.reviveKillBlackList.stream().anyMatch(listString ->
                    BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString().contains(listString));
        }

        return isWhitelist == hasMatch;
    }

    @EventBusSubscriber(modid = ReviveMe.MOD_ID)
    public static class PotionEvents {

        @SubscribeEvent
        public static void killMobEvent(LivingDeathEvent event) {
            Entity sourceEntity = event.getSource().getEntity();
            if (!(sourceEntity instanceof LivingEntity)) return;
            LivingEntity entity = (LivingEntity) sourceEntity;
            MobEffectInstance instance = entity.getEffect(MobEffectInit.KILL_REVIVE_EFFECT);
            if (instance == null) return;
            if (!isAllowedEntity(event.getEntity())) return;

            entity.removeEffect(MobEffectInit.KILL_REVIVE_EFFECT);

            if (instance.getAmplifier() > 0) {
                entity.removeEffect(MobEffectInit.KILL_REVIVE_EFFECT);

                entity.addEffect(new MobEffectInstance(
                        MobEffectInit.KILL_REVIVE_EFFECT, instance.getDuration(), instance.getAmplifier() - 1));
            }
        }

        //This will set the used reviveMethod to none in the fallen capability (unless the player has been downed again)
        @SubscribeEvent
        public static void expireEvent(MobEffectEvent.Expired event) {
            removeEffect(event.getEntity(), event.getEffectInstance(), false);
        }

        @SubscribeEvent
        public static void removeEvent(MobEffectEvent.Remove event) {
            removeEffect(event.getEntity(), event.getEffectInstance(), true);
        }

        public static void removeEffect(LivingEntity entity, MobEffectInstance effect, boolean completed) {
            if (effect == null) return;
            if (!(effect.getEffect() instanceof KillRevivePotionEffect)) return;

            DamageSource killSource = new DamageSource(entity.level().registryAccess()
                    .lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeInit.KILL_REVIVE));
            if (!(entity instanceof ServerPlayer)){
                if (completed) entity.hurt(killSource, 20 * (effect.getAmplifier() + 1));
                return;
            }

            FallenData cap = FallenData.get(entity);
            if (cap.isFallen()) return;

            if (completed) return;

            entity.setHealth(0.00000001F);
            entity.hurt(killSource, 1);
        }
    }
}

