package invoker54.reviveme.common.event;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallEvent.class, ReviveMeConfig.debugMode);

    public static boolean cancelEvent(Player player, DamageSource source) {
        FallenCapability instance = FallenCapability.GetFallCap(player);

        instance.refreshSelfReviveTypes(player);

        if (!instance.canSelfRevive() &&
                (player.getServer() == null || (player.getServer() != null && player.getServer().getPlayerCount() < 1)))
            return false;

//        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
//            LOGGER.info("MAKING THEM FALLEN");
            NetworkHandler.sendMessage(Component.literal(player.getName().getString())
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

            //grab the FALLEN EFFECT amplifier for later use
            if (player.hasEffect(MobEffectInit.FALLEN_EFFECT)){
                instance.setPenaltyMultiplier(player.getEffect(MobEffectInit.FALLEN_EFFECT).getAmplifier() + 1);
                //Remove the FallenEffect so it doesn't get saved
                player.removeEffect(MobEffectInit.FALLEN_EFFECT);
            }

            //Save all of their potion effects
            if (ReviveMeConfig.revertEffectsOnRevive){
                instance.saveEffects(player);
            }
            player.removeAllEffects();

            //Give them all the downed effects.
            modifyPotionEffects(player);

            //Dismount the player if riding something
            player.stopRiding();

            //stop them from using an item if they are using one
            player.stopUsingItem();

            //Close any containers they have open as well.
            player.closeContainer();

            //Finally send capability code to all players
            CompoundTag nbt = new CompoundTag();

            //System.out.println("Am I fallen?: " + FallenCapability.GetFallCap(player).isFallen());
            if (instance.getOtherPlayer() != null) {

                Player otherPlayer = player.level().getPlayerByUUID(instance.getOtherPlayer());
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


            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                    new SyncClientCapMsg(nbt));

        } else instance.setFallen(false);

        return instance.isFallen();
    }

    public static void modifyPotionEffects(Player player){
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
                if (effectInstance == null) {
                    player.addEffect(new MobEffectInstance(effect, Integer.MAX_VALUE, tier));
                }
            }
            catch (Exception e){
                LOGGER.error("This string couldn't be parsed: " + string);
            }
        }
    }
}
