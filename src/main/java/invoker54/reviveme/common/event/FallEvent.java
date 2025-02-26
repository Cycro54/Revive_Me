package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.SyncClientCapMsg;
import invoker54.reviveme.init.MobEffectInit;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void StopDeath(LivingDeathEvent event){
//        LOGGER.info("WAS IT CANCELLED? " + event.isCanceled());
        if (event.isCanceled()) return;
//        LOGGER.info("IS IT A PLAYER? " + (event.getEntityLiving() instanceof Player));
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        //If they are in creative mode, don't bother with any of this.
        if (player.gameMode.getGameModeForPlayer() == GameType.CREATIVE) return;

        //If they have a totem of undying in their InteractionHand, don't cancel the events
        for (InteractionHand InteractionHand : InteractionHand.values()) {
            ItemStack itemstack1 = player.getItemInHand(InteractionHand);
            if (itemstack1.getItem() == Items.TOTEM_OF_UNDYING) {
                return;
            }
        }

        //They are probs not allowed to die.
        event.setCanceled(cancelEvent(player, event.getSource()));
    }

    public static boolean cancelEvent(Player player, DamageSource source) {
        FallenData instance = FallenData.get(player);

        //Generate a sacrificial item list
        if (!instance.usedSacrificedItems()) instance.setSacrificialItems(player.getInventory());

        //If they used both self-revive options, and they are not on a server, they should die immediately
        if (instance.usedChance() &&
                (instance.usedSacrificedItems() || instance.getItemList().isEmpty()) &&
                (player.getServer() != null && player.getServer().getPlayerCount() < 2)) return false;

//        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
//            LOGGER.info("MAKING THEM FALLEN");
            NetworkInit.sendMessage(Component.literal(player.getName().getString())
                    .append(Component.translatable("revive-me.chat.player_fallen")), false, player);

            //Set to fallen state
            instance.setFallen(true);

            //Set health to 1
            player.setHealth(1);

            //Set food to 0
            player.getFoodData().setFoodLevel(0);

            //Set last damage source for later
            instance.setDamageSource(source);

            //Set time left to whatever is in config file
            instance.SetTimeLeft(player.level().getGameTime(), ReviveMeConfig.timeLeft);

            //Set penalty type and amount
            instance.setPenalty(ReviveMeConfig.penaltyType, ReviveMeConfig.penaltyAmount, ReviveMeConfig.penaltyItem);
            //System.out.println(ReviveMeConfig.penaltyType);

            //grab the FALLEN EFFECT amplifier for later use
            if (player.hasEffect(MobEffectInit.FALLEN_EFFECT)){
                instance.setPenaltyMultiplier(player.getEffect(MobEffectInit.FALLEN_EFFECT).getAmplifier() + 1);
            }

            player.removeAllEffects();

            //Give them all the downed effects.
            applyDownedEffects(player);

            //Dismount the player if riding something
            player.stopRiding();

            //stop them from using an item if they are using one
            player.stopUsingItem();

            //Close any containers they have open as well.
            player.closeContainer();

            //Take away xp levels
            if (ReviveMeConfig.fallenXpPenalty > 0){
                double xpToRemove = ReviveMeConfig.fallenXpPenalty;
                if (xpToRemove < 1) xpToRemove = Math.round(player.experienceLevel * xpToRemove);
                player.giveExperienceLevels((int) -xpToRemove);
            }

            //Finally send capability code to all players
//            CompoundTag nbt = new CompoundTag();

            //System.out.println("Am I fallen?: " + FallenData.get(player).isFallen());
            if (instance.getOtherPlayer() != null) {

                Player otherPlayer = player.level().getPlayerByUUID(instance.getOtherPlayer());
                if (otherPlayer != null) {
                    FallenData otherCap = FallenData.get(otherPlayer);
                    otherCap.resumeFallTimer();
                    otherCap.setOtherPlayer(null);

                    PacketDistributor.sendToPlayersTrackingEntityAndSelf(otherPlayer, new SyncClientCapMsg(otherPlayer.getUUID(), otherCap.writeNBT()));
                }
                instance.setOtherPlayer(null);
            }
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncClientCapMsg(player.getUUID(), instance.writeNBT()));

            player.setHealth(0);
            //Make all angerable enemies nearby forgive the player.
            for (Entity entity : ((ServerLevel) player.level()).getAllEntities()) {
                if (!(entity instanceof Mob mob)) continue;
                if (mob.getTarget() == null) continue;
                if (mob.getTarget().getId() != player.getId()) continue;
                if (mob instanceof NeutralMob){
                    ((NeutralMob)mob).playerDied(player);
                }
                mob.aiStep();
            }
            player.setHealth(1);

            return true;
        }

        return false;
    }

    public static void applyDownedEffects(Player player){
        for (String string : ReviveMeConfig.downedEffects){
            try {
                String[] array = string.split(":");
//                LOGGER.info("The effect split into pieces: " + Arrays.toString(array));
                ResourceLocation effectLocation = ResourceLocation.fromNamespaceAndPath(array[0],array[1]);
                int tier = Integer.parseInt(array[2]);
//                LOGGER.info("The tier: " + tier);
                Optional<Holder.Reference<MobEffect>> effect = BuiltInRegistries.MOB_EFFECT.getHolder(effectLocation);
                if (effect.isEmpty()){
                    LOGGER.error("Incorrect MOD ID or Potion Effect: " + string);
                    continue;
                }

                MobEffectInstance effectInstance = player.getEffect(effect.get());
                if (effectInstance == null || effectInstance.getAmplifier() < tier) {
                    player.addEffect(new MobEffectInstance(effect.get(), Integer.MAX_VALUE, tier));
                }
            }
            catch (Exception e){
                LOGGER.error("This string couldn't be parsed: " + string);
            }
        }
    }
}
