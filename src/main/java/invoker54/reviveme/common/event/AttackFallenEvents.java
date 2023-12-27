package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.InstaKillMsg;
import invoker54.reviveme.common.network.message.SyncServerCapMsg;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class AttackFallenEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void playerAttackFallen(LivingAttackEvent event){
        if (event.isCanceled()) return;

        if (event.getEntity().level.isClientSide) return;

        PlayerEntity player = (PlayerEntity) event.getEntity();
        FallenCapability cap = FallenCapability.GetFallCap(player);

        //If it's a source that goes through invulnerability, let it pass
        if (event.getSource() != null && event.getSource().isBypassInvul()){
            return;
        }

        //If they aren't fallen, let it pass.
        else if (!cap.isFallen()){
            return;
        }

        //If the damage source is a player that's crouching, let it pass
        else if (event.getSource() != null && (event.getSource().getEntity() instanceof PlayerEntity) && event.getSource().getEntity().isCrouching()){
            if (event.getEntity().level.isClientSide) {
                //Update the damage source
                cap.setDamageSource(event.getSource());
                CompoundNBT tag = new CompoundNBT();
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
    public static void CancelMobTarget(LivingSetAttackTargetEvent event){
        if (!(event.getEntity() instanceof MobEntity)) return;
        if (!(event.getTarget() instanceof PlayerEntity)) return;

        FallenCapability cap = FallenCapability.GetFallCap(event.getTarget());
        if (cap.isFallen()) {
            //Trick the target selector for the mob by making it so the player appears to be dead
            if (event.getEntityLiving() instanceof IAngerable) {
                ((IAngerable) event.getEntityLiving()).playerDied((PlayerEntity) event.getTarget());
            }
            ((MobEntity)event.getEntityLiving()).setTarget(null);
        }
    }

    @SubscribeEvent
    public static void fallenInvisible(LivingEvent.LivingVisibilityEvent event){
        if (!(event.getEntity() instanceof PlayerEntity)) return;
        if (!(FallenCapability.GetFallCap((LivingEntity) event.getEntity()).isFallen())) return;

        event.modifyVisibility(0);
    }
}



