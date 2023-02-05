package invoker54.reviveme.common.event;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Mod.EventBusSubscriber
public class CancelMobTargetEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void CancelMobTarget(LivingSetAttackTargetEvent event){
        if (!(event.getEntityLiving() instanceof MobEntity)) return;
        if (!(event.getTarget() instanceof PlayerEntity)) return;

        scrubMobMemories((MobEntity) event.getEntityLiving(), (PlayerEntity) event.getTarget());
    }

    @SubscribeEvent
    public static void StopMobAttack(LivingAttackEvent event){
        if (event.getSource() == null) return;
        if (!(event.getSource().getEntity() instanceof MobEntity)) return;
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        scrubMobMemories((MobEntity) event.getSource().getEntity(), (PlayerEntity) event.getEntityLiving());
    }

    public static void scrubMobMemories(MobEntity mobEntity, PlayerEntity fallenPlayer){
        if (!FallenCapability.GetFallCap(fallenPlayer).isFallen()) return;

        //Forgive the player
        if (mobEntity instanceof IAngerable){
            ((IAngerable) mobEntity).playerDied(fallenPlayer);
//            ((IAngerable) mobEntity).forgetCurrentTargetAndRefreshUniversalAnger();
        }

        //If the fallen player has hit this mob, remove the evidence.
        if (mobEntity.getKillCredit() != null && mobEntity.getKillCredit().getId() == fallenPlayer.getId()){
            mobEntity.setLastHurtByMob(null);
            mobEntity.setLastHurtByPlayer(null);
            // LOGGER.info("SETTING LAST HURT TO NULL");
        }

        //Finally set their target to null
        mobEntity.setTarget(null);

        Brain<?> brain = mobEntity.getBrain();
        // LOGGER.info(brain.getClass());

        // LOGGER.info("Whos the player: " + fallenPlayer.getName().getString());
        // LOGGER.info("Whos the mob: " + mobEntity.getName().getString());
        // LOGGER.info("What are there memories? ");

        //Trick the target selector for the mob by making it so the player appears to be dead
        fallenPlayer.setHealth(0);
        mobEntity.targetSelector.tick();
        mobEntity.goalSelector.tick();
        fallenPlayer.setHealth(1);

        //If there attack target is the fallen player, scrub the mobs memory
        if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryModuleStatus.REGISTERED)) {
// LOGGER.info("REMOVING FROM ATTACK TARGET");
            brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((targetEntity) -> {
                if (targetEntity.getId() == fallenPlayer.getId()) {
                    brain.setMemory(MemoryModuleType.ATTACK_TARGET, (LivingEntity) null);
//                    LOGGER.debug("Removing ATTACK TARGET MEMORY");
                }
            });
        }

        //If the nearest visible targetable player is fallen, scrub the mobs memory
        if (brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleStatus.REGISTERED)) {
// LOGGER.info("REMOVING FROM NEAREST VISIBLE TARGETABLE PLAYER");
            brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER).ifPresent((targetEntity) -> {
                if (targetEntity.getId() == fallenPlayer.getId()) {
                    brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, (PlayerEntity) null);
//                    LOGGER.debug("Removing NEAREST TARGETABLE PLAYER MEMORY");
                }
            });
        }

        //If the angering player is fallen, scrub the mobs memory
        if (brain.checkMemory(MemoryModuleType.ANGRY_AT, MemoryModuleStatus.REGISTERED)) {
// LOGGER.info("REMOVING FROM ANGRY AT");
            brain.getMemory(MemoryModuleType.ANGRY_AT).ifPresent((targetEntity) -> {
                if (targetEntity == fallenPlayer.getUUID()) {
                    brain.setMemory(MemoryModuleType.ANGRY_AT, (UUID) null);
//                    LOGGER.debug("Removing ANGER MEMORY");
                }
            });
        }

        //If the rude player is fallen, scrub the mobs memory
        if (brain.checkMemory(MemoryModuleType.HURT_BY_ENTITY, MemoryModuleStatus.REGISTERED)) {
// LOGGER.info("REMOVING FROM HURT BY ENTITY");
            brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent((targetEntity) -> {
                if (targetEntity.getId() == fallenPlayer.getId()) {
                    brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, (LivingEntity) null);
//                    LOGGER.debug("Removing HURT BY ENTITY MEMORY");
                }
            });
        }

        //If the rude player is fallen, scrub the mobs memory
        if (brain.checkMemory(MemoryModuleType.HURT_BY, MemoryModuleStatus.REGISTERED)) {
// LOGGER.info("REMOVING FROM HURT BY");
            brain.getMemory(MemoryModuleType.HURT_BY).ifPresent((source) -> {
                if (!(source.getEntity() instanceof PlayerEntity)) return;
                if (source.getEntity().getId() == fallenPlayer.getId()) {
                    brain.setMemory(MemoryModuleType.HURT_BY, (DamageSource) null);
//                    LOGGER.debug("Removing HURT BY MEMORY");
                }
            });
        }

        //If the seen player is fallen, scrub the mobs memory
        if (brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleStatus.REGISTERED)) {
            // LOGGER.info("REMOVING FROM NEAREST VISIBLE PLAYER");
            brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).ifPresent((targetEntity) -> {
                if (targetEntity.getId() == fallenPlayer.getId()) {
                    brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, (PlayerEntity) null);
//                    LOGGER.debug("Removing VISIBLE PLAYER MEMORY");
                }
            });
        }

        if (brain.checkMemory(MemoryModuleType.LIVING_ENTITIES, MemoryModuleStatus.REGISTERED)) {
            // LOGGER.info("REMOVING FROM LIVING ENTITIES");
            Optional<List<LivingEntity>> optional1 = brain.getMemory(MemoryModuleType.LIVING_ENTITIES);
            if (optional1.isPresent()) {
                List<LivingEntity> list = optional1.get();
                list.removeIf((entity) -> entity.getId() == fallenPlayer.getId());
            }
        }

        if (brain.checkMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES, MemoryModuleStatus.REGISTERED)) {
            // LOGGER.info("REMOVING FROM VISIBLE LIVING ENTITIES");
            Optional<List<LivingEntity>> optional2 = brain.getMemory(MemoryModuleType.VISIBLE_LIVING_ENTITIES);
            if (optional2.isPresent()) {
                List<LivingEntity> list = optional2.get();
                list.removeIf((entity) -> entity.getId() == fallenPlayer.getId());
            }
        }

        if (brain.checkMemory(MemoryModuleType.NEAREST_PLAYERS, MemoryModuleStatus.REGISTERED)) {
            // LOGGER.info("REMOVING FROM NEAREST PLAYERS");
            Optional<List<PlayerEntity>> optional3 = brain.getMemory(MemoryModuleType.NEAREST_PLAYERS);
            if (optional3.isPresent()) {
                List<PlayerEntity> list = optional3.get();
                list.removeIf((entity) -> entity.getId() == fallenPlayer.getId());
            }
        }
//
//        try {
//            mobEntity = null;
//            mobEntity.getId();
//        }
//        catch (Exception e){
//            e.printStackTrace();
//        }
    }

    @SubscribeEvent
    public static void fallenInvisible(LivingEvent.LivingVisibilityEvent event){
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        if (!(FallenCapability.GetFallCap(event.getEntityLiving()).isFallen())) return;

        event.modifyVisibility(0);
    }
}



