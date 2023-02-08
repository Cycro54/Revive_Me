package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class attackFallenEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void playerAttackFallen(LivingAttackEvent event){
        if (event.isCanceled()) return;

        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        FallenCapability cap = FallenCapability.GetFallCap(player);


//        LOGGER.info("START OF ATTACKING");
//        LOGGER.info("What is the source? : " + event.getSource());
//        if (event.getSource() != null){
////            LOGGER.info("Does it go through invulnerability: " + event.getSource().isBypassInvul());
//            if (event.getSource().getEntity() != null) LOGGER.info("whats the entity? : " + event.getSource().getEntity().getName().getString());
//
//        }

        //If it's a source that goes through invulnerability, let it pass
        if (event.getSource() != null && event.getSource().isBypassInvul()){
//            LOGGER.info("Damage bypasses invulnerability, let it pass");
            return;
        }

        //If they aren't fallen, let it pass.
        else if (!cap.isFallen()){
//            LOGGER.info("They have not fallen, let it pass.");
            return;
        }

        //If the damage source is a player that's crouching, let it pass
        else if (event.getSource() != null && (event.getSource().getEntity() instanceof Player) && event.getSource().getEntity().isCrouching()){
            player.setInvulnerable(false);
            cap.setDamageSource(event.getSource());
            event.getSource().bypassInvul().bypassArmor();
//            LOGGER.info("It's a sneaking player! let it pass.");
            return;
        }

        //Else cancel the attack event
        else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void CancelMobTarget(LivingChangeTargetEvent event){
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(event.getNewTarget() instanceof Player player)) return;

        FallenCapability cap = FallenCapability.GetFallCap(player);
        if (cap.isFallen() && mob instanceof NeutralMob) {
            //Trick the target selector for the mob by making it so the player appears to be dead
            ((NeutralMob)mob).playerDied(player);
        }
//        scrubMobMemories((Mob) event.getEntity(), (Player) event.getNewTarget());
    }
//
//    @SubscribeEvent
//    public static void StopMobAttack(LivingAttackEvent event){
//        if (event.getSource() == null) return;
//        if (!(event.getSource().getEntity() instanceof Mob)) return;
//        if (!(event.getEntity() instanceof Player fallenPlayer)) return;
//
//        if (!FallenCapability.GetFallCap(fallenPlayer).isFallen()) return;
//
////        scrubMobMemories((Mob) event.getSource().getEntity(), (Player) event.getEntity());
//    }

//    public static void scrubMobMemories(Mob Mob, Player fallenPlayer){
////        if (!FallenCapability.GetFallCap(fallenPlayer).isFallen()) return;
////
////        //Forgive the player
////        if (Mob instanceof NeutralMob){
////            ((NeutralMob) Mob).playerDied(fallenPlayer);
////        }
////
////        //If the fallen player has hit this mob, remove the evidence.
////        if (Mob.getKillCredit() != null && Mob.getKillCredit().getUUID() == fallenPlayer.getUUID()){
////            Mob.setLastHurtByMob(null);
////            Mob.setLastHurtByPlayer(null);
////
////            // LOGGER.info("SETTING LAST HURT TO NULL");
////        }
////
////        Brain<?> brain = Mob.getBrain();
////        // LOGGER.info(brain.getClass());
////
////        // LOGGER.info("Whos the player: " + fallenPlayer.getName().getString());
////        // LOGGER.info("Whos the mob: " + Mob.getName().getString());
////        // LOGGER.info("What are there memories? ");
////
////        //Trick the target selector for the mob by making it so the player appears to be dead
////        fallenPlayer.setHealth(0);
////        Mob.targetSelector.tick();
////        Mob.goalSelector.tick();
////        fallenPlayer.setHealth(1);
////
////        //Finally set their target to null
////        Mob.setTarget(null);
//
////                if (brain.checkMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryStatus.REGISTERED)) {
////                    brain.setMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, 0L);
////// LOGGER.info("REMOVING FROM ATTACK TARGET");
////            brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((targetEntity) -> {
////                if (targetEntity.getId() == fallenPlayer.getId()) {
////                    brain.setMemory(MemoryModuleType.ATTACK_TARGET, (LivingEntity) null);
//////                    LOGGER.debug("Removing ATTACK TARGET MEMORY");
////                }
////            });
////        }
//
////        //If there attack target is the fallen player, scrub the mobs memory
////        if (brain.checkMemory(MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED)) {
////// LOGGER.info("REMOVING FROM ATTACK TARGET");
////            brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent((targetEntity) -> {
////                if (targetEntity.getId() == fallenPlayer.getId()) {
////                    brain.setMemory(MemoryModuleType.ATTACK_TARGET, (LivingEntity) null);
//////                    LOGGER.debug("Removing ATTACK TARGET MEMORY");
////                }
////            });
////        }
////
////        //If the nearest visible targetable player is fallen, scrub the mobs memory
////        if (brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, MemoryStatus.REGISTERED)) {
////// LOGGER.info("REMOVING FROM NEAREST VISIBLE TARGETABLE PLAYER");
////            brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER).ifPresent((targetEntity) -> {
////                if (targetEntity.getId() == fallenPlayer.getId()) {
////                    brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_ATTACKABLE_PLAYER, (Player) null);
//////                    LOGGER.debug("Removing NEAREST TARGETABLE PLAYER MEMORY");
////                }
////            });
////        }
////
////        //If the angering player is fallen, scrub the mobs memory
////        if (brain.checkMemory(MemoryModuleType.ANGRY_AT, MemoryStatus.REGISTERED)) {
////// LOGGER.info("REMOVING FROM ANGRY AT");
////            brain.getMemory(MemoryModuleType.ANGRY_AT).ifPresent((targetEntity) -> {
////                if (fallenPlayer.getUUID().equals(targetEntity)) {
////                    brain.setMemory(MemoryModuleType.ANGRY_AT, (UUID) null);
//////                    LOGGER.debug("Removing ANGER MEMORY");
////                }
////            });
////        }
////
////        //If the rude player is fallen, scrub the mobs memory
////        if (brain.checkMemory(MemoryModuleType.HURT_BY_ENTITY, MemoryStatus.REGISTERED)) {
////// LOGGER.info("REMOVING FROM HURT BY ENTITY");
////            brain.getMemory(MemoryModuleType.HURT_BY_ENTITY).ifPresent((targetEntity) -> {
////                if (targetEntity.getId() == fallenPlayer.getId()) {
////                    brain.setMemory(MemoryModuleType.HURT_BY_ENTITY, (LivingEntity) null);
//////                    LOGGER.debug("Removing HURT BY ENTITY MEMORY");
////                }
////            });
////        }
////
////        //If the rude player is fallen, scrub the mobs memory
////        if (brain.checkMemory(MemoryModuleType.HURT_BY, MemoryStatus.REGISTERED)) {
////// LOGGER.info("REMOVING FROM HURT BY");
////            brain.getMemory(MemoryModuleType.HURT_BY).ifPresent((source) -> {
////                if (!(source.getEntity() instanceof Player)) return;
////                if (source.getEntity().getId() == fallenPlayer.getId()) {
////                    brain.setMemory(MemoryModuleType.HURT_BY, (DamageSource) null);
//////                    LOGGER.debug("Removing HURT BY MEMORY");
////                }
////            });
////        }
////
////        //If the seen player is fallen, scrub the mobs memory
////        if (brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryStatus.REGISTERED)) {
////            brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER).ifPresent((targetEntity) -> {
////                if (targetEntity.getId() == fallenPlayer.getId()) {
////                    // LOGGER.info("REMOVING FROM NEAREST VISIBLE PLAYER");
////                    brain.setMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER, (Player) null);
//////                    LOGGER.debug("Removing VISIBLE PLAYER MEMORY");
////                }
////            });
////        }
////
////        if (brain.checkMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES, MemoryStatus.REGISTERED)) {
////            Optional<List<LivingEntity>> optional1 = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES);
////            if (optional1.isPresent()) {
////                 LOGGER.info("REMOVING FROM LIVING ENTITIES");
////                List<LivingEntity> list = optional1.get();
////                list.removeIf((entity) -> entity.getId() == fallenPlayer.getId());
////            }
////        }
////
////        if (brain.checkMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED)) {
////            // LOGGER.info("REMOVING FROM VISIBLE LIVING ENTITIES");
////            Optional<NearestVisibleLivingEntities> optional2 = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
////            if (optional2.isPresent()) {
////                NearestVisibleLivingEntities list = optional2.get();
////                if (list.contains(fallenPlayer)) brain.eraseMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES);
////            }
////        }
////
////        if (brain.checkMemory(MemoryModuleType.NEAREST_PLAYERS, MemoryStatus.REGISTERED)) {
////            // LOGGER.info("REMOVING FROM NEAREST PLAYERS");
////            Optional<List<Player>> optional3 = brain.getMemory(MemoryModuleType.NEAREST_PLAYERS);
////            if (optional3.isPresent()) {
////                List<Player> list = optional3.get();
////                list.removeIf((entity) -> entity.getId() == fallenPlayer.getId());
////            }
////        }
//////
//////        try {
//////            Mob = null;
//////            Mob.getId();
//////        }
//////        catch (Exception e){
//////            e.printStackTrace();
//////        }
//    }

    @SubscribeEvent
    public static void fallenInvisible(LivingEvent.LivingVisibilityEvent event){
        if (!(event.getEntity() instanceof Player)) return;
        if (!(FallenCapability.GetFallCap(event.getEntity()).isFallen())) return;

        event.modifyVisibility(0);
    }
}



