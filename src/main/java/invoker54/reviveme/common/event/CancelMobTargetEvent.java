package invoker54.reviveme.common.event;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
public class CancelMobTargetEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void CancelMobTarget(LivingSetAttackTargetEvent event){
        if (event.getEntityLiving() == null) return;
        if (event.getTarget() == null) return;
        if (!(event.getTarget() instanceof PlayerEntity)) return;
        if (!FallenCapability.GetFallCap(event.getEntityLiving()).isFallen()) return;

        MobEntity mob = (MobEntity) event.getEntityLiving();
        mob.setTarget(null);

        if (mob.getLastHurtByMob() == null) return;
        if (mob.getLastHurtByMob().getId() == event.getTarget().getId()) mob.setLastHurtByPlayer(null);
    }

    @SubscribeEvent
    public static void StopMobAttack(LivingAttackEvent event){
        if (event.getSource() == null) return;
        if (!(event.getSource().getEntity() instanceof MobEntity)) return;
        if (event.getEntityLiving() instanceof PlayerEntity) return;
        if (!FallenCapability.GetFallCap(event.getEntityLiving()).isFallen()) return;

        MobEntity mobEntity = (MobEntity) event.getSource().getEntity();
        mobEntity.setTarget(null);

        if (mobEntity.getLastHurtByMob() == null) return;
        if (mobEntity.getLastHurtByMob().getId() == event.getEntityLiving().getId()) mobEntity.setLastHurtByPlayer(null);
    }

    @SubscribeEvent
    public static void fallenInvisible(LivingEvent.LivingVisibilityEvent event){
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        if (!(FallenCapability.GetFallCap(event.getEntityLiving()).isFallen())) return;

        LOGGER.debug("THIS IS A PLAYER, MAKING THEM INVISIBLE!");

        event.modifyVisibility(0);
    }
}



