package invoker54.reviveme.common.data;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.MathUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.MobEffectInit;
import invoker54.reviveme.init.NetworkInit;
import invoker54.reviveme.init.SoundInit;
import invoker54.reviveme.mixin.FoodMixin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;

import java.util.ArrayList;
import java.util.List;

public class ReviveConfigData {
    private static final ModLogger LOGGER = ModLogger.getLogger(ReviveConfigData.class, ReviveMeConfig.debugMode);
    private double revivedHealth;
    private double revivedFood;
    private double fallenPenaltyTimer;
    private List<MobEffectInstance> reviveEffects;
    public static final InvoText reviveText = InvoText.translate("revive_me.commands.revive_pass");

    public ReviveConfigData copy(){
        return new ReviveConfigData(this.revivedHealth,
                this.revivedFood,
                this.fallenPenaltyTimer,
                new ArrayList<>(this.reviveEffects));
    }

    public ReviveConfigData(double revivedHealth, double revivedFood, double fallenPenaltyTimer, List<MobEffectInstance> reviveEffects) {
        this.revivedHealth = revivedHealth;
        this.revivedFood = revivedFood;
        this.fallenPenaltyTimer = fallenPenaltyTimer;
        this.reviveEffects = reviveEffects;
    }

    public ReviveConfigData setRevivedHealth(double revivedHealth) {
        this.revivedHealth = revivedHealth;
        return this;
    }
    public double getRevivedHealth(){
        return this.revivedHealth;
    }

    public ReviveConfigData setRevivedFood(double revivedFood) {
        this.revivedFood = revivedFood;
        return this;
    }
    public double getRevivedFood(){
        return this.revivedFood;
    }

    public ReviveConfigData setFallenPenaltyTimer(double fallenPenaltyTimer) {
        this.fallenPenaltyTimer = fallenPenaltyTimer;
        return this;
    }
    public double getFallenPenaltyTimer(){
        return this.fallenPenaltyTimer;
    }

    public static MobEffectInstance EffectFromString(String effectString) {
        try {
            String[] array = effectString.split(":");
            ResourceLocation effectLocation = ResourceLocation.fromNamespaceAndPath(array[0],array[1]);
            int tier = Integer.parseInt(array[2]);
            int ticks = Integer.MAX_VALUE;
            try {ticks = Integer.parseInt(array[3]);}
            catch (Exception ignored){}
            boolean isVisibleBool = array[array.length-1].equalsIgnoreCase("true") || array[array.length-1].equalsIgnoreCase("false");
            boolean isVisible = !isVisibleBool || !Boolean.parseBoolean(array[array.length-1]);
            MobEffect effect = BuiltInRegistries.MOB_EFFECT.get(effectLocation);
            if (effect == null){
                LOGGER.error("Incorrect MOD ID or Potion MobEffect: " + effectString);
                return null;
            }

            return new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), ticks, tier, false, isVisible);
        }
        catch (Exception e){
            if (!effectString.equals("PARENT")) LOGGER.error("Invalid string: " + effectString);
        }

        return null;
    }

    public ReviveConfigData setReviveEffects(List<MobEffectInstance> reviveEffects){
        this.reviveEffects = reviveEffects;
        return this;
    }

    public List<MobEffectInstance> getReviveEffects(){
        return new ArrayList<>(this.reviveEffects);
    }

    public void revivePlayer(Player fallen, boolean isCommand, Player reviver, FallenData.PENALTYPE penaltype){
        String reviveString = "none";
        switch (penaltype){
            case NONE: reviveString = "none"; break;
            case HEALTH: reviveString = "health"; break;
            case EXPERIENCE: reviveString = "experience"; break;
            case FOOD: reviveString = "food"; break;
        }
        if (reviver.isCreative()) reviveString = "creative";
        this.revivePlayer(fallen, isCommand, reviver, reviveString);
    }
    public void revivePlayer(Player fallen, boolean isCommand, Player reviver, FallenData.SELFREVIVETYPE selfrevivetype){
        String reviveString = "none";
        switch (selfrevivetype){
            case CHANCE: reviveString = "chance"; break;
            case RANDOM_ITEMS: reviveString = "random_items"; break;
            case KILL: reviveString = "kill"; break;
            case STATUS_EFFECTS: reviveString = "status_effects"; break;
            case EXPERIENCE: reviveString = "experience"; break;
            case CREATIVE: reviveString = "creative"; break;
        }
        this.revivePlayer(fallen, isCommand, reviver, reviveString);
    }

    public void revivePlayer(Player fallen, boolean isCommand, Player reviver, String reviveString){
        this.revivePlayer(fallen, isCommand, reviver, reviveText.setArgs(fallen.getDisplayName(),
                reviver == null ? InvoText.translate("revive_me.reviver.unknown").getText() : reviver.getDisplayName(),
                InvoText.translate("revive_me.revive_type."+reviveString).getText()));
    }
    
    public void revivePlayer(Player fallen, boolean isCommand, Player reviver, InvoText reviveText){
        FallenData cap = FallenData.get(fallen);

        //region Set the revived players health
        double healAmount;
        if (revivedHealth <= 0) {
            healAmount = fallen.getMaxHealth();
        }
        //Percentage
        else if (revivedHealth > 0 && revivedHealth < 1) {
            healAmount = (double) (fallen.getMaxHealth() * revivedHealth);
        }
        //Flat value
        else {
            healAmount = (double) revivedHealth;
        }
        fallen.setHealth((float) healAmount);
        //endregion

        //region Set the revived players Food
        double foodAmount;
        if (revivedFood < 0) {
            foodAmount = 40;
        }
        //Percentage
        else if (revivedFood >= 0 && revivedFood < 1) {
            foodAmount = (double) (40 * revivedFood);
        }
        //Flat value
        else {
            foodAmount = (double) revivedFood;
        }
        //Now set their food level
        fallen.getFoodData().setFoodLevel((int) Math.min(foodAmount,20));
        //Then their saturation
        ((FoodMixin)fallen.getFoodData()).setSaturationLevel((float) Math.max(0, foodAmount-20));
        //endregion

        //Remove all potion effects
        cap.removeOriginalEffects(true);
        //Load the saved effects
        cap.loadEffects(fallen);

        //Add the fallen potion effect
        if (getFallenPenaltyTimer() != 0) fallen.addEffect(new MobEffectInstance(MobEffectInit.FALLEN_EFFECT, (int) (fallenPenaltyTimer * 20), cap.getPenaltyMultiplier(), false,
                !ReviveMeConfig.hidePenaltyTimerEffect, true));

        if (this.reviveEffects != null) {
            for (MobEffectInstance instance : reviveEffects) {
                fallen.addEffect(new MobEffectInstance(instance.getEffect(), instance.getDuration(), instance.getAmplifier(), false, instance.isVisible()));
            }
        }

        cap.setFallen(false);

        fallen.level().playSound(null, fallen.getX(), fallen.getY(), fallen.getZ(),
                SoundInit.REVIVED, SoundSource.PLAYERS, 1.0F, MathUtil.randomFloat(0.7F, 1.0F));

        if (!fallen.level().isClientSide) {
            NetworkInit.sendMessage(reviveText.getText(), isCommand, fallen);

            cap.syncClient(true);
            if (reviver != null && reviver != fallen) FallenData.get(reviver).syncClient(true);
        }
    }

    public void takeFromReviver(Player reviver, Player fallen){
        if (reviver == null)return;
        if (reviver == fallen) return;

        FallenData cap = FallenData.get(fallen);

        //Take penalty amount from reviver
        if (!reviver.isCreative()) {
            int amount = (int) cap.getPenaltyAmount(reviver);
            int leftoverAmount = 0;
            switch (ReviveMeConfig.penaltyType) {
                case NONE:
                    break;
                case HEALTH:
                    leftoverAmount = Math.max(0, Math.round(amount - reviver.getAbsorptionAmount()));
                    reviver.setAbsorptionAmount(reviver.getAbsorptionAmount()-amount);
                    reviver.setHealth(Math.max(1, reviver.getHealth() - leftoverAmount));
                    break;
                case EXPERIENCE:
                    reviver.giveExperiencePoints(-amount);
                    break;
                case FOOD:
                    FoodData food = reviver.getFoodData();
                    leftoverAmount = (int) Math.max(0,Math.round(amount - food.getSaturationLevel()));
                    ((FoodMixin)food).setSaturationLevel(Math.max(0, food.getSaturationLevel() - amount));
                    food.setFoodLevel(Math.max(0,food.getFoodLevel() - leftoverAmount));
                    ((ServerPlayer)reviver).connection.send(new ClientboundSetHealthPacket(reviver.getHealth(),
                            reviver.getFoodData().getFoodLevel(), reviver.getFoodData().getSaturationLevel()));
                    break;
            }
        }

        cap = FallenData.get(reviver);
        cap.setOtherPlayerAndItem(null, null);
        cap.syncClient(true);
    }
}