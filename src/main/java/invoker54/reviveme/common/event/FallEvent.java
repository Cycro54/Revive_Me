package invoker54.reviveme.common.event;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.init.EffectInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallEvent.class, ReviveMeConfig.debugMode);

    public static boolean cancelEvent(PlayerEntity player, DamageSource source) {
        FallenCapability instance = FallenCapability.get(player);

        instance.refreshSelfReviveTypes(player);

//        if (!instance.canSelfRevive() && ((!player.getServer().isDedicatedServer() &&
//                player.getServer().getPlayerCount() == 1))) return false;

//        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
//            LOGGER.info("MAKING THEM FALLEN");
            NetworkHandler.sendMessage(InvoText.translate("revive-me.chat.player_fallen",
                    player.getName(), source.getLocalizedDeathMessage(player)).getText(), false, player);

            //Set to fallen state
            instance.setFallen(true);

            double maxHealth = ReviveMeConfig.fallenHealth;
            if (maxHealth <= 0) maxHealth = player.getMaxHealth();
            else if (maxHealth < 1) maxHealth = player.getMaxHealth() * maxHealth;
            maxHealth = Math.max(maxHealth, 1);
            //Set health to fallen health
            player.setHealth((float) maxHealth);

            //Set food to 0
            player.getFoodData().setFoodLevel(1);

            //Set last damage source for later
            instance.setDamageSource(source);

            //Set time left to whatever is in config file
            instance.SetTimeLeft(player.level.getGameTime(), ReviveMeConfig.timeLeft, true);

            //grab the FALLEN EFFECT amplifier for later use
            if (player.hasEffect(EffectInit.FALLEN_EFFECT)){
                instance.setPenaltyMultiplier(player.getEffect(EffectInit.FALLEN_EFFECT).getAmplifier() + 1);
                //Remove the FallenEffect so it doesn't get saved
                player.removeEffect(EffectInit.FALLEN_EFFECT);
            }

            //Save all of their potion effects
            if (ReviveMeConfig.revertEffectsOnRevive){
                instance.saveEffects(player);
            }
            instance.removeOriginalEffects(player);

            //Give them all the downed effects.
            modifyPotionEffects(player);

            //Dismount the player if riding something
            player.stopRiding();

            //stop them from using an item if they are using one
            player.stopUsingItem();

            //Close any containers they have open as well.
            player.closeContainer();

            //Set the maxOverheal thingy
            instance.setMaxOverheal();

            //Finally send capability code to all players

            //System.out.println("Am I fallen?: " + FallenCapability.GetFallCap(player).isFallen());
            if (instance.getOtherPlayer() != null) {
                PlayerEntity otherPlayer = player.level.getPlayerByUUID(instance.getOtherPlayer());
                if (otherPlayer != null) {
                    FallenCapability otherCap = FallenCapability.get(otherPlayer);
                    otherCap.resumeFallTimer();
                    otherCap.setOtherPlayerAndItem(null, null);

                    otherCap.syncClient(true);
                }
                instance.setOtherPlayerAndItem(null, null);
            }

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
            player.setHealth((float) maxHealth);

            instance.syncClient(true);
        }
        else instance.setFallen(false);

        return instance.isFallen();
    }

    public static void modifyPotionEffects(PlayerEntity player){
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
                 if (effectInstance == null) {
                    player.addEffect(new EffectInstance(effect, Integer.MAX_VALUE, tier));
                }
            }
            catch (Exception e){
                LOGGER.error("This string couldn't be parsed: " + string);
            }
        }
    }
}
