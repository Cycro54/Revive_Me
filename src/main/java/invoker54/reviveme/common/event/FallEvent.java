package invoker54.reviveme.common.event;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveConfigData;
import invoker54.reviveme.init.MobEffectInit;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import invoker54.reviveme.init.NetworkInit;

public class FallEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallEvent.class, ReviveMeConfig.debugMode);

    public static boolean canBypassReviveMe(DamageSource damageSource, boolean isInitial){
        boolean isWhitelist = !ReviveMeConfig.damageSourceWhitelist.contains("//");
        boolean inList = false;
        String idString = damageSource.getMsgId();
        for (String listString : ReviveMeConfig.damageSourceWhitelist){
            if (listString.contains("//")) continue;

            String cleanListString = listString.replaceAll("[*;]", "");
            boolean canMatch = !listString.contains(";") || (idString.length() == (cleanListString.length()));
            if (!canMatch) continue;
            boolean shouldPass = (isWhitelist != isInitial) || listString.contains("*");

            inList = (cleanListString.contains("/") || idString.contains(cleanListString)) && shouldPass;
            if (inList) break;
        }

        return isWhitelist == inList;
    }

    public static boolean cancelEvent(Player player, DamageSource source) {
        FallenData instance = FallenData.get(player);
        if (canBypassReviveMe(source, true)) return false;

        //Set last damage source for later
        instance.setDamageSource(source);

        if (!instance.canSelfRevive() && !instance.canPlayerRevive()) return false;

        instance.refreshSelfReviveTypes();

//        if (!instance.canSelfRevive() && ((!player.getServer().isDedicatedServer() &&
//                player.getServer().getPlayerCount() == 1))) return false;

//        LOGGER.info("Are they fallen? " + instance.isFallen());
        if (!instance.isFallen()) {
//            LOGGER.info("MAKING THEM FALLEN");
            NetworkInit.sendMessage(InvoText.translate("revive_me.chat.player_fallen",
                    player.getDisplayName(), source.getLocalizedDeathMessage(player)).getText(), false, player);

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
            instance.removeOriginalEffects(true);

            //Give them all the downed effects.
            modifyPotionEffects(player);

            //Set time left to whatever is in config file
            instance.SetTimeLeft(player.level().getGameTime(), ReviveMeConfig.timeLeft, true);

            //Dismount the player if riding something
            player.stopRiding();

            //stop them from using an item if they are using one
            player.stopUsingItem();

            //Close any containers they have open as well.
            player.closeContainer();

            //Set the maxOverheal thingy
            instance.setMaxOverheal();

            //Also refresh revive item list
            instance.refreshReviveItemList();

//            //Finally send capability code to all players
//            CompoundTag nbt = new CompoundTag();

            //System.out.println("Am I fallen?: " + FallenData.GetFallCap(player).isFallen());
            if (instance.getOtherPlayer() != null) {
                Player otherPlayer = player.level().getPlayerByUUID(instance.getOtherPlayer());
                if (otherPlayer != null) {
                    FallenData otherCap = FallenData.get(otherPlayer);
                    otherCap.resumeFallTimer();
                    otherCap.setOtherPlayerAndItem(null, null);
                    otherCap.syncClient(true);
                }
                instance.setOtherPlayerAndItem(null, null);
            }
            instance.syncClient(true);

            player.setHealth(0);
            //Make all angerable enemies nearby forgive the player.
            for (Entity entity : ((ServerLevel) player.level()).getAllEntities()) {
                if (!(entity instanceof Mob mob)) continue;
                if (mob.getTarget() == null) continue;
                if (mob.getTarget().getId() != player.getId()) continue;
                if (mob instanceof NeutralMob){
                    ((NeutralMob)mob).playerDied((ServerLevel) player.level(), player);
                }
                mob.aiStep();
            }
            player.setHealth((float) maxHealth);

        }
        else instance.setFallen(false);

        return instance.isFallen();
    }

    public static void modifyPotionEffects(Player player){
        for (String string : ReviveMeConfig.downedEffects){
            try {
                MobEffectInstance effectInstance = ReviveConfigData.EffectFromString(string);
                if (effectInstance == null) continue;

                if (player.hasEffect(effectInstance.getEffect())) continue;
                player.addEffect(effectInstance);
            }
            catch (Exception e){
                LOGGER.error("This string couldn't be parsed: " + string);
            }
        }
    }
}
