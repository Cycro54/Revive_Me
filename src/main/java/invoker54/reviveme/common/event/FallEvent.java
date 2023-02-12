package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    //First, stop the death event.
//    @SubscribeEvent(priority = EventPriority.LOWEST)
//    public static void InterruptDeath(LivingDamageEvent event) {
//        if (event.isCanceled()) return;
//        if (!(event.getEntityLiving() instanceof Player)) return;
//
//        Player player = (Player) event.getEntityLiving();
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
//        LOGGER.info("WAS IT CANCELLED? " + event.isCanceled());
        if (event.isCanceled()) return;
//        LOGGER.info("IS IT A PLAYER? " + (event.getEntityLiving() instanceof Player));
        if (!(event.getEntityLiving() instanceof Player)) return;

        Player player = (Player) event.getEntityLiving();

        //They are probs not allowed to die.
        event.setCanceled(cancelEvent(player, event.getSource()));
    }

    public static boolean cancelEvent(Player player, DamageSource source){
        FallenCapability instance = FallenCapability.GetFallCap(player);

        //If they are in creative mode, don't bother with any of this.
        if (player.isCreative()) return false;

        //If they have a totem of undying in their InteractionHand, dont cancel the events
        for(InteractionHand InteractionHand : InteractionHand.values()) {
            ItemStack itemstack1 = player.getItemInHand(InteractionHand);
            if (itemstack1.getItem() == Items.TOTEM_OF_UNDYING) {
                return false;
            }
        }

//        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
//            LOGGER.info("MAKING THEM FALLEN");
            for(Player player1 : ((ServerLevel)player.level).getServer().getPlayerList().getPlayers()){
                player1.sendMessage(new TextComponent(player.getName().getString())
                        .append(new TranslatableComponent("revive-me.chat.player_fallen")), Util.NIL_UUID);
            }

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
            player.setInvulnerable(true);

            player.removeAllEffects();

            //Make it so they can't move very fast.
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 99999, 3, false, false, false));

            //Dismount the player if riding something
            player.stopRiding();

            //stop them from using an item if they are using one
            player.stopUsingItem();

            //Finally send capability code to all players
            CompoundTag nbt = new CompoundTag();

            //System.out.println("Am I fallen?: " + FallenCapability.GetFallCap(player).isFallen());
            if (instance.getOtherPlayer() != null) {

                Player otherPlayer = player.level.getPlayerByUUID(instance.getOtherPlayer());
                if (otherPlayer != null) {
                    FallenCapability otherCap = FallenCapability.GetFallCap(otherPlayer);
                    otherCap.resumeFallTimer();
                    otherCap.setOtherPlayer(null);

                    nbt.put(otherPlayer.getStringUUID(), otherCap.writeNBT());
                }
                instance.setOtherPlayer(null);
            }
            nbt.put(player.getStringUUID(), instance.writeNBT());

            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncClientCapMsg(nbt));

            return true;
        }

        return false;
    }
}
