package invoker54.reviveme.common.event;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber
public class CancelMobTargetEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void CancelMobTarget(LivingChangeTargetEvent event){
        if (!(event.getEntityLiving() instanceof Mob)) return;
        if (!(event.getNewTarget() instanceof Player)) return;

        scrubMobMemories((Mob) event.getEntityLiving(), (Player) event.getNewTarget());
    }

    @SubscribeEvent
    public static void StopMobAttack(LivingAttackEvent event){
        if (event.getSource() == null) return;
        if (!(event.getSource().getEntity() instanceof Mob)) return;
        if (!(event.getEntityLiving() instanceof Player)) return;

        scrubMobMemories((Mob) event.getSource().getEntity(), (Player) event.getEntityLiving());
    }

    public static void scrubMobMemories(Mob Mob, Player fallenPlayer){
        if (!FallenCapability.GetFallCap(fallenPlayer).isFallen()) return;

        //Forgive the player
        if (Mob instanceof NeutralMob){
            ((NeutralMob) Mob).playerDied(fallenPlayer);
        }

        //If the fallen player has hit this mob, remove the evidence.
        if (Mob.getKillCredit() != null && Mob.getKillCredit().getId() == fallenPlayer.getId()){
            Mob.setLastHurtByMob(null);
            Mob.setLastHurtByPlayer(null);
            // LOGGER.info("SETTING LAST HURT TO NULL");
        }

        //Finally set their target to null
        Mob.setTarget(null);

        Brain<?> brain = Mob.getBrain();
        // LOGGER.info(brain.getClass());

        // LOGGER.info("Whos the player: " + fallenPlayer.getName().getString());
        // LOGGER.info("Whos the mob: " + Mob.getName().getString());
        // LOGGER.info("What are there memories? ");

        //Trick the target selector for the mob by making it so the player appears to be dead
        fallenPlayer.setHealth(0);
        Mob.targetSelector.tick();
        Mob.goalSelector.tick();
        fallenPlayer.setHealth(1);
//
//        //If there attack target is the fallen player, scrub the mobs memory
//        if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)) {
//// LOGGER.info("REMOVING FROM ATTACK TARGET");
//            brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((targetEntity) -> {
//                if (targetEntity.getId() == fallenPlayer.getId()) {
//                    brain.setMemory(MemoryModuleType.ATTACK_TARGET, (LivingEntity) null);
////                    LOGGER.debug("Removing ATTACK TARGET MEMORY");
//                }
//            });
//        }
//
//        //If the nearest visible targetable player is fallen, scrub the mobs memory
//        if (brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryStatus.REGISTERED)) {
//// LOGGER.info("REMOVING FROM NEAREST VISIBLE TARGETABLE PLAYER");
//            brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER).ifPresent((targetEntity) -> {
//                if (targetEntity.getId() == fallenPlayer.getId()) {
//                    brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, (Player) null);
////                    LOGGER.debug("Removing NEAREST TARGETABLE PLAYER MEMORY");
//                }
//            });
//        }
//
//        //If the angering player is fallen, scrub the mobs memory
//        if (brain.checkMemory(MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED)) {
//// LOGGER.info("REMOVING FROM ANGRY AT");
//            brain.getMemory(MemoryModuleType.ANGRY_AT).ifPresent((targetEntity) -> {
//                if (targetEntity == fallenPlayer.getUUID()) {
//                    brain.setMemory(MemoryModuleType.ANGRY_AT, (UUID) null);
////                    LOGGER.debug("Removing ANGER MEMORY");
//                }
//            });
//        }
//
//        //If the rude player is fallen, scrub the mobs memory
//        if (brain.checkMemory(MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.REGISTERED)) {
//// LOGGER.info("REMOVING FROM HURT BY ENTITY");
//            brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent((targetEntity) -> {
//                if (targetEntity.getId() == fallenPlayer.getId()) {
//                    brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, (LivingEntity) null);
////                    LOGGER.debug("Removing HURT BY ENTITY MEMORY");
//                }
//            });
//        }
//
//        //If the rude player is fallen, scrub the mobs memory
//        if (brain.checkMemory(MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED)) {
//// LOGGER.info("REMOVING FROM HURT BY");
//            brain.getMemory(MemoryModuleType.HURT_BY).ifPresent((source) -> {
//                if (!(source.getEntity() instanceof Player)) return;
//                if (source.getEntity().getId() == fallenPlayer.getId()) {
//                    brain.setMemory(MemoryModuleType.HURT_BY, (DamageSource) null);
////                    LOGGER.debug("Removing HURT BY MEMORY");
//                }
//            });
//        }
//
//        //If the seen player is fallen, scrub the mobs memory
//        if (brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.REGISTERED)) {
//            // LOGGER.info("REMOVING FROM NEAREST VISIBLE PLAYER");
//            brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).ifPresent((targetEntity) -> {
//                if (targetEntity.getId() == fallenPlayer.getId()) {
//                    brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, (Player) null);
////                    LOGGER.debug("Removing VISIBLE PLAYER MEMORY");
//                }
//            });
//        }
//
//        if (brain.checkMemory(MemoryModuleType.LIVING_ENTITIES, MemoryStatus.REGISTERED)) {
//            // LOGGER.info("REMOVING FROM LIVING ENTITIES");
//            Optional<List<LivingEntity>> optional1 = brain.getMemory(MemoryModuleType.LIVING_ENTITIES);
//            if (optional1.isPresent()) {
//                List<LivingEntity> list = optional1.get();
//                list.removeIf((entity) -> entity.getId() == fallenPlayer.getId());
//            }
//        }
//
//        if (brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED)) {
//            // LOGGER.info("REMOVING FROM VISIBLE LIVING ENTITIES");
//            Optional<NearestVisibleLivingEntities> optional2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
//            if (optional2.isPresent()) {
//                NearestVisibleLivingEntities list = optional2.get();
//                list.
//                list.removeIf((entity) -> entity.getId() == fallenPlayer.getId());
//            }
//        }
//
//        if (brain.checkMemory(MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.REGISTERED)) {
//            // LOGGER.info("REMOVING FROM NEAREST PLAYERS");
//            Optional<List<Player>> optional3 = brain.getMemory(MemoryModuleType.NEAREST_PLAYERS);
//            if (optional3.isPresent()) {
//                List<Player> list = optional3.get();
//                list.removeIf((entity) -> entity.getId() == fallenPlayer.getId());
//            }
//        }
////
////        try {
////            Mob = null;
////            Mob.getId();
////        }
////        catch (Exception e){
////            e.printStackTrace();
////        }
    }

    @SubscribeEvent
    public static void fallenInvisible(LivingEvent.LivingVisibilityEvent event){
        if (!(event.getEntityLiving() instanceof Player)) return;
        if (!(FallenCapability.GetFallCap(event.getEntityLiving()).isFallen())) return;

        event.modifyVisibility(0);
    }
}



