package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.InstaKillMsg;
import invoker54.reviveme.common.network.message.SyncServerCapMsg;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
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
        LOGGER.info("IS THE EVENT CANCELLED? " + event.isCanceled());
        if (event.isCanceled()) return;

        LOGGER.info("IS IT CLIENTSIDE? " + event.getEntityLiving().level.isClientSide);
        if (event.getEntityLiving().level.isClientSide) return;

        LOGGER.info("IS THE ATTACKED ENTITY A PLAYER? " + (event.getEntity() instanceof Player));
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();
        FallenCapability cap = FallenCapability.GetFallCap(player);

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
            if (event.getEntityLiving().level.isClientSide) {
                //Update the damage source
                cap.setDamageSource(event.getSource());
                CompoundTag tag = new CompoundTag();
                tag.put(player.getStringUUID(), cap.writeNBT());
                NetworkHandler.INSTANCE.sendToServer(new SyncServerCapMsg(tag));

                //Send a kill message
                NetworkHandler.INSTANCE.sendToServer(new InstaKillMsg(event.getEntity().getUUID()));
                event.setCanceled(true);
                return;
            }
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
    }

    @SubscribeEvent
    public static void fallenInvisible(LivingEvent.LivingVisibilityEvent event){
        if (!(event.getEntity() instanceof Player)) return;
        if (!(FallenCapability.GetFallCap((LivingEntity) event.getEntity()).isFallen())) return;

        event.modifyVisibility(0);
    }
}


