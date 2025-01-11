package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import invoker54.reviveme.init.EffectInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.GameType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
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
//        LOGGER.info("IS IT A PLAYER? " + (event.getEntityLiving() instanceof PlayerEntity));
        if (!(event.getEntityLiving() instanceof ServerPlayerEntity)) return;
        
        ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();

        //If they are in creative mode, don't bother with any of this.
        if ((player.gameMode.getGameModeForPlayer() == GameType.CREATIVE)) return;

        //If they have a totem of undying in their InteractionHand, don't cancel the events
        for (Hand InteractionHand : Hand.values()) {
            ItemStack itemstack1 = player.getItemInHand(InteractionHand);
            if (itemstack1.getItem() == Items.TOTEM_OF_UNDYING) {
                return;
            }
        }

        //They are probs not allowed to die.
        event.setCanceled(cancelEvent(player, event.getSource()));
    }

    public static boolean cancelEvent(PlayerEntity player, DamageSource source) {
        FallenCapability instance = FallenCapability.GetFallCap(player);

        //Generate a sacrificial item list
        ArrayList<Item> playerItems = new ArrayList<>();
        for (ItemStack itemStack : player.inventory.items) {
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
            NetworkHandler.sendMessage(new StringTextComponent(player.getName().getString())
                    .append(new TranslationTextComponent("revive-me.chat.player_fallen")), false, player);

            //Set to fallen state
            instance.setFallen(true);

            //Set health to 1
            player.setHealth(1);

            //Set food to 0
            player.getFoodData().setFoodLevel(0);

            //Set last damage source for later
            instance.setDamageSource(source);

            //Set time left to whatever is in config file
            instance.SetTimeLeft(player.level.getGameTime(), ReviveMeConfig.timeLeft);

            //Set penalty type and amount
            instance.setPenalty(ReviveMeConfig.penaltyType, ReviveMeConfig.penaltyAmount, ReviveMeConfig.penaltyItem);
            //System.out.println(ReviveMeConfig.penaltyType);

            //grab the FALLEN EFFECT amplifier for later use
            if (player.hasEffect(EffectInit.FALLEN_EFFECT)){
                instance.setPenaltyMultiplier(player.getEffect(EffectInit.FALLEN_EFFECT).getAmplifier() + 1);
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

            player.setHealth(0);
            //Make all angerable enemies nearby forgive the player.
            for (Entity entity : ((ServerWorld) player.level).getAllEntities()) {
                if (!(entity instanceof MobEntity)) continue;
                MobEntity mob = (MobEntity) entity;
                if (mob.getTarget() == null) continue;
                if (mob.getTarget().getId() != player.getId()) continue;
                if (mob instanceof IAngerable){
                    ((IAngerable)mob).playerDied(player);
                }
                mob.aiStep();
            }
            player.setHealth(1);

            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncClientCapMsg(nbt));

            return true;
        }

        return false;
    }

    public static void applyDownedEffects(PlayerEntity player){
        for (String string : ReviveMeConfig.downedEffects){
            try {
                String[] array = string.split(":");
//                LOGGER.info("The effect split into pieces: " + Arrays.toString(array));
                ResourceLocation effectLocation = new ResourceLocation(array[0],array[1]);
                int tier = Integer.parseInt(array[2]);
//                LOGGER.info("The tier: " + tier);
                Effect effect = ForgeRegistries.POTIONS.getValue(effectLocation);
                if (effect == null){
                    LOGGER.error("Incorrect MOD ID or Potion Effect: " + string);
                    continue;
                }

                EffectInstance effectInstance = player.getEffect(effect);
                if (effectInstance == null || effectInstance.getAmplifier() < tier) {
                    player.addEffect(new EffectInstance(effect, Integer.MAX_VALUE, tier));
                }
            }
            catch (Exception e){
                LOGGER.error("This string couldn't be parsed: " + string);
            }
        }
    }
}
