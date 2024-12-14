package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void StopDeath(LivingDeathEvent event){
//        LOGGER.info("WAS IT CANCELLED? " + event.isCanceled());
        if (event.isCanceled()) return;
//        LOGGER.info("IS IT A PLAYER? " + (event.getEntityLiving() instanceof Player));
        if (!(event.getEntityLiving() instanceof Player player)) return;

        //If they are in creative mode, don't bother with any of this.
        if (player.isCreative()) return;

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
        FallenCapability instance = FallenCapability.GetFallCap(player);

        //Generate a sacrificial item list
        ArrayList<Item> playerItems = new ArrayList<>();
        for (ItemStack itemStack : player.getInventory().items) {
            if (playerItems.contains(itemStack.getItem())) continue;
            if (!itemStack.isStackable()) continue;
            if (itemStack.isEmpty()) continue;
            playerItems.add(itemStack.getItem());
        }
        //Remove all except 4
        while (playerItems.size() > 4) {
            playerItems.remove(player.level.random.nextInt(playerItems.size()));
        }

        //If they used both self-revive options, and they are not on a server, they should die immediately
        if (instance.usedChance() &&
                (instance.usedSacrificedItems() || playerItems.isEmpty()) &&
                (player.getServer() != null && player.getServer().getPlayerCount() < 2)) return false;

//        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
//            LOGGER.info("MAKING THEM FALLEN");
            for (Player player1 : ((ServerLevel) player.level).getServer().getPlayerList().getPlayers()) {
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

            //This will only happen if the player is in a single player world
            if (!instance.usedSacrificedItems()) {
                instance.setSacrificialItems(playerItems);
            }

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

            //Make all angerable enemies nearby forgive the player.
            for (Entity entity : ((ServerLevel) player.level).getAllEntities()) {
                if (!(entity instanceof Mob mob)) continue;

                if (mob instanceof NeutralMob neutralMob){
                    neutralMob.playerDied(player);
                }
                if (mob.getTarget() == null) continue;
                if (mob.getTarget().getId() == player.getId()) mob.setTarget(null);
            }


            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncClientCapMsg(nbt));

            return true;
        }

        return false;
    }

    public static void applyDownedEffects(Player player){
        for (String string : ReviveMeConfig.downedEffects){
            try {
                String[] array = string.split(":");
//                LOGGER.info("The effect split into pieces: " + Arrays.toString(array));
                ResourceLocation effectLocation = new ResourceLocation(array[0],array[1]);
                int tier = Integer.parseInt(array[2]);
//                LOGGER.info("The tier: " + tier);
                MobEffect effect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                if (effect == null){
                    LOGGER.error("Incorrect MOD ID or Potion Effect: " + string);
                    continue;
                }

                MobEffectInstance effectInstance = player.getEffect(effect);
                if (effectInstance == null || effectInstance.getAmplifier() < tier) {
                    player.addEffect(new MobEffectInstance(effect, Integer.MAX_VALUE, tier));
                }
            }
            catch (Exception e){
                LOGGER.error("This string couldn't be parsed: " + string);
            }
        }
    }
}
