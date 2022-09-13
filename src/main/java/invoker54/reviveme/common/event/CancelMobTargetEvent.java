package invoker54.reviveme.common.event;

import invoker54.reviveme.common.api.FallenProvider;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class CancelMobTargetEvent {

    @SubscribeEvent
    public static void CancelMobTarget(LivingSetAttackTargetEvent event){
        MobEntity mob = (MobEntity) event.getEntityLiving();

        if (mob.getTarget() instanceof PlayerEntity && event.getTarget().getCapability(FallenProvider.FALLENDATA)
                .orElseThrow(NullPointerException::new).isFallen()){

            mob.setTarget(null);

        }
    }

    @SubscribeEvent
    public static void StopMobAttack(LivingAttackEvent event){
        if (event.getSource().getEntity() instanceof MobEntity &&
        event.getEntityLiving() instanceof PlayerEntity &&
                event.getEntityLiving().getCapability(FallenProvider.FALLENDATA)
                        .orElseThrow(NullPointerException::new).isFallen()){

            MobEntity mobEntity = (MobEntity) event.getSource().getEntity();
            mobEntity.setTarget(null);

        }
    }
}



