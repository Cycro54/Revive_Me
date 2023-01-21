package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    //First, stop the death event.
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void InterruptDeath(LivingDamageEvent event) {
//        if (event.isCanceled()) return;
//        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
//
//        PlayerEntity player = (PlayerEntity) event.getEntityLiving();
//
//        LOGGER.info("Is it enough damage? " + (player.getHealth() - event.getAmount() <= 0));
//
//        if (player.getHealth() - event.getAmount() <= 0) {
//            //Cancel the event so the player doesn't end up being killed probs
//            event.setCanceled(beginFallenPhase(player, event.getSource()));
//        }
//
//    }

//    @SubscribeEvent
//    public static void playerTick(TickEvent.PlayerTickEvent event){
//        if (event.side == LogicalSide.CLIENT) return;
//        if (event.phase == TickEvent.Phase.END) return;
//        if (event.player.getHealth() > 0) return;
//
//        FallenCapability cap = FallenCapability.GetFallCap(event.player);
//        if (cap.isFallen()) return;
//
//        beginFallenPhase(event.player, event.player.getLastDamageSource());
//    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void StopDeath(LivingDeathEvent event){
        LOGGER.info("WAS IT CANCELLED? " + event.isCanceled());
        if (event.isCanceled()) return;
        LOGGER.info("IS IT A PLAYER? " + (event.getEntityLiving() instanceof PlayerEntity));
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;


        PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        //They are probs not allowed to die.
        event.setCanceled(beginFallenPhase(player, event.getSource()));
    }

    public static boolean beginFallenPhase(PlayerEntity player, DamageSource source){
        FallenCapability instance = FallenCapability.GetFallCap(player);

        //If they are in creative mode, don't bother with any of this.
        if (player.isCreative()) return false;

        //If they have a totem of undying in their hand, dont cancel the events
        for(Hand hand : Hand.values()) {
            ItemStack itemstack1 = player.getItemInHand(hand);
            if (itemstack1.getItem() == Items.TOTEM_OF_UNDYING) {
                return false;
            }
        }

        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
            LOGGER.info("MAKING THEM FALLEN");

            //Set to fallen state
            instance.setFallen(true);

            //Set health to 1
            player.setHealth(1);

            //Set food to 0
            player.getFoodData().setFoodLevel(0);

            //Set last damage source for later
            instance.setDamageSource(source);

            //Set time left to whatever is in config file
            instance.SetTimeLeft((int) player.level.getGameTime(), ReviveMeConfig.timeLeft);

            //Set penalty type and amount
            instance.setPenalty(ReviveMeConfig.penaltyType, ReviveMeConfig.penaltyAmount);
            //System.out.println(ReviveMeConfig.penaltyType);

            //Make them invulnerable to all damage (besides void and creative of course.)
//                    player.setInvulnerable(true);
            player.removeAllEffects();

            //Make it so they can't move very fast.
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 99999, 3, false, false, false));

            //Dismount the player if riding something
            player.stopRiding();

            //System.out.println("Am I fallen?: " + FallenCapability.GetFallCap(player).isFallen());

            //Finally send capability code to all players
            CompoundNBT nbt = new CompoundNBT();
            nbt.put(player.getStringUUID(),instance.writeNBT());
            NetworkHandler.INSTANCE.send((PacketDistributor.ALL.noArg()), new SyncClientCapMsg(nbt, ""));

            return true;
        }

        return false;
    }
}
