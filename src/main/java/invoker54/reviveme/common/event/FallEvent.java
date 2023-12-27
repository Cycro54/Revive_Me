package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
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

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void StopDeath(LivingDeathEvent event){
//        LOGGER.info("WAS IT CANCELLED? " + event.isCanceled());
        if (event.isCanceled()) return;
//        LOGGER.info("IS IT A PLAYER? " + (event.getEntityLiving() instanceof PlayerEntity));
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getEntityLiving();

        //They are probs not allowed to die.
        event.setCanceled(cancelEvent(player, event.getSource()));
    }

    public static boolean cancelEvent(PlayerEntity player, DamageSource source){
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

//        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
//            LOGGER.info("MAKING THEM FALLEN");
            for(PlayerEntity player1 : ((ServerWorld)player.level).getServer().getPlayerList().getPlayers()){
                player1.sendMessage(new StringTextComponent(player.getName().getString())
                        .append(new TranslationTextComponent("revive-me.chat.player_fallen")), Util.NIL_UUID);
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
            player.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 99999, 3, false, false, false));

            //Dismount the player if riding something
            player.stopRiding();

            //stop them from using an item if they are using one
            player.stopUsingItem();

            //Close any containers they have open as well.
            player.closeContainer();

            //Finally send capability code to all players
            CompoundNBT nbt = new CompoundNBT();

            //System.out.println("Am I fallen?: " + FallenCapability.GetFallCap(player).isFallen());
            if (instance.getOtherPlayer() != null) {

                PlayerEntity otherPlayer = player.level.getPlayerByUUID(instance.getOtherPlayer());
                if (otherPlayer != null) {
                    FallenCapability otherCap = FallenCapability.GetFallCap(otherPlayer);
                    otherCap.resumeFallTimer();
                    otherCap.setOtherPlayer(null);

                    nbt.put(otherPlayer.getStringUUID(), otherCap.writeNBT());
                }
                instance.setOtherPlayer(null);
            }
            nbt.put(player.getStringUUID(), instance.writeNBT());

            //Make all angerable enemies nearby forgive the player.
            for (Entity entity : ((ServerWorld) player.level).getAllEntities()) {
                if (entity instanceof IAngerable) {
                    ((IAngerable) entity).playerDied(player);
                }
                if (!(entity instanceof MobEntity)) continue;
                LivingEntity target = ((MobEntity) entity).getTarget();

                if (target == null) continue;
                if (target.getId() == player.getId()) {
                    ((MobEntity) entity).setTarget(null);
                }
            }

            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncClientCapMsg(nbt));

            return true;
        }

        return false;
    }
}
