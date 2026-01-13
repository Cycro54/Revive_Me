package invoker54.reviveme.common.data;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.MathUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.init.EffectInit;
import invoker54.reviveme.init.SoundInit;
import invoker54.reviveme.mixin.FoodMixin;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SUpdateHealthPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReviveConfigData {
    private static final ModLogger LOGGER = ModLogger.getLogger(ReviveConfigData.class, ReviveMeConfig.debugMode);
    private double revivedHealth;
    private double revivedFood;
    private double fallenPenaltyTimer;
    private List<EffectInstance> reviveEffects;
    public static final InvoText reviveText = InvoText.translate("revive-me.commands.revive_pass");

    public ReviveConfigData copy(){
        return new ReviveConfigData(this.revivedHealth,
                this.revivedFood,
                this.fallenPenaltyTimer,
                new ArrayList<>(this.reviveEffects));
    }

    public ReviveConfigData(double revivedHealth, double revivedFood, double fallenPenaltyTimer, List<EffectInstance> reviveEffects) {
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

    public static EffectInstance EffectFromString(String effectString) {
        try {
            List<String> pieces = Arrays.asList(effectString.split(":"));
            ResourceLocation effectLocation = new ResourceLocation(pieces.get(0), pieces.get(1));
            int tier = Integer.parseInt(pieces.get(2));
            int ticks = Integer.parseInt(pieces.get(3));
            Effect effect = ForgeRegistries.POTIONS.getValue(effectLocation);
            if (effect == null){
                LOGGER.error("Incorrect MOD ID or Potion Effect: " + effectString);
                return null;
            }

            return new EffectInstance(effect, ticks, tier);
        }
        catch (Exception e){
            if (!effectString.equals("PARENT")) LOGGER.error("Invalid string: " + effectString);
        }

        return null;
    }

    public ReviveConfigData setReviveEffects(List<EffectInstance> reviveEffects){
        this.reviveEffects = reviveEffects;
        return this;
    }

    public List<EffectInstance> getReviveEffects(){
        return new ArrayList<>(this.reviveEffects);
    }

    public void revivePlayer(PlayerEntity fallen, boolean isCommand, PlayerEntity reviver, FallenCapability.PENALTYPE penaltype){
        String reviveString = "none";
        switch (penaltype){
            case NONE: reviveString = "none"; break;
            case HEALTH: reviveString = "health"; break;
            case EXPERIENCE: reviveString = "experience"; break;
            case FOOD: reviveString = "food"; break;
        }
        this.revivePlayer(fallen, isCommand, reviver, reviveString);
    }
    public void revivePlayer(PlayerEntity fallen, boolean isCommand, PlayerEntity reviver, FallenCapability.SELFREVIVETYPE selfrevivetype){
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

    public void revivePlayer(PlayerEntity fallen, boolean isCommand, PlayerEntity reviver, String reviveString){
        this.revivePlayer(fallen, isCommand, reviver, reviveText.setArgs(fallen.getDisplayName(),
                reviver == null ? InvoText.translate("revive-me.reviver.unknown").getText() : reviver.getDisplayName(),
                InvoText.translate("revive-me.revive_type."+reviveString).getText()));
    }
    
    public void revivePlayer(PlayerEntity fallen, boolean isCommand, PlayerEntity reviver, InvoText reviveText){
        FallenCapability cap = FallenCapability.get(fallen);

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
        fallen.removeAllEffects();
        //Load the saved effects
        cap.loadEffects(fallen);

        //Add the fallen potion effect
        if (getFallenPenaltyTimer() != 0) fallen.addEffect(new EffectInstance(EffectInit.FALLEN_EFFECT, (int) (fallenPenaltyTimer * 20), cap.getPenaltyMultiplier()));

        if (this.reviveEffects != null) {
            for (EffectInstance instance : reviveEffects) {
                fallen.addEffect(new EffectInstance(instance.getEffect(), instance.getDuration(), instance.getAmplifier()));
            }
        }

        cap.setFallen(false);

        fallen.level.playSound(null, fallen.getX(), fallen.getY(), fallen.getZ(),
                SoundInit.REVIVED, SoundCategory.PLAYERS, 1.0F, MathUtil.randomFloat(0.7F, 1.0F));

        if (!fallen.level.isClientSide) {
            NetworkHandler.sendMessage(reviveText.getText(), isCommand, fallen);

            cap.syncClient(true);
        }
    }

    public void takeFromReviver(PlayerEntity reviver, PlayerEntity fallen){
        if (reviver == null)return;
        if (reviver == fallen) return;

        FallenCapability cap = FallenCapability.get(fallen);

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
                    FoodStats food = reviver.getFoodData();
                    leftoverAmount = (int) Math.max(0,Math.round(amount - food.getSaturationLevel()));
                    ((FoodMixin)food).setSaturationLevel(Math.max(0, food.getSaturationLevel() - amount));
                    food.setFoodLevel(Math.max(0,food.getFoodLevel() - leftoverAmount));
                    ((ServerPlayerEntity)reviver).connection.send(new SUpdateHealthPacket(reviver.getHealth(),
                            reviver.getFoodData().getFoodLevel(), reviver.getFoodData().getSaturationLevel()));
                    break;
            }
        }

        cap = FallenCapability.get(reviver);
        cap.setOtherPlayerAndItem(null, null);

        cap.syncClient(true);
    }
}