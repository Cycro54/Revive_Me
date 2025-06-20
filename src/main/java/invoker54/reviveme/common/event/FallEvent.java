package invoker54.reviveme.common.event;

import invoker54.invocore.common.ModLogger;
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
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Optional;

public class FallEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallEvent.class, ReviveMeConfig.debugMode);

    public static boolean cancelEvent(Player player, DamageSource source) {
        FallenData instance = FallenData.get(player);

        instance.refreshSelfReviveTypes(player);

        if (!instance.canSelfRevive() &&
                (player.getServer() == null || (player.getServer() != null && player.getServer().getPlayerCount() < 1)))
            return false;

//        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
//            LOGGER.info("MAKING THEM FALLEN");
            NetworkInit.sendMessage(Component.translatable("revive_me.chat.player_fallen", player.getDisplayName()), false, player);

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

        } else instance.setFallen(false);

        return instance.isFallen();
    }

    public static void modifyPotionEffects(Player player){
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
                if (effectInstance == null) {
                    player.addEffect(new MobEffectInstance(effect.get(), Integer.MAX_VALUE, tier));
                }
            }
            catch (Exception e){
                LOGGER.error("This string couldn't be parsed: " + string);
            }
        }
    }
}
