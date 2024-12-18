package invoker54.reviveme.common.capability;

import invoker54.reviveme.common.api.FallenProvider;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FallenCapability {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String FALLEN_BOOL = "isFallenREVIVE";
    public static final String FELL_START_INT = "fellStartREVIVE";
    public static final String FELL_END_FLOAT = "fellEndREVIVE";
    public static final String REVIVE_START_INT = "revStartREVIVE";
    public static final String REVIVE_END_INT = "revEndREVIVE";
    public static final String PENALTY_ENUM = "penaltyTypeREVIVE";
    public static final String PENALTY_DOUBLE = "penaltyDoubleREVIVE";
    public static final String PENALTY_ITEM = "penaltyItemREVIVE";
    public static final String OTHERPLAYER_UUID = "otherPlayerREVIVE";
    public static final String SACRIFICEITEMS_STRING = "sacrificeItemStringREVIVE";
    public static final String SACRIFICEITEMS_BOOL = "sacrificeItemsBoolREVIVE";
    public static final String REVIVECHANCE_BOOL = "reviveChanceBoolREVIVE";
    public static final String PENALTY_MULTIPLIER_INT = "penaltyMultiplierIntREVIVE";
    //endregion

    public FallenCapability(Level level){
        this.level = level;
    }
    public FallenCapability() {

    }

    protected Level level;
    protected int revStart = 0;
    protected int revEnd = 0;
    protected int fellStart = 0;
    protected float fellEnd = 0;
    protected DamageSource damageSource = DamageSource.OUT_OF_WORLD;
    protected boolean isFallen = false;
    protected boolean isDying = false;
    protected UUID otherPlayer = null;
    protected Double penaltyAmount = 0D;
    protected ItemStack penaltyItem = ItemStack.EMPTY;
    protected PENALTYPE penaltyType = PENALTYPE.HEALTH;

    protected List<Item> sacrificialItems = new ArrayList<>();
    protected boolean sacrificedItemsUsed = false;
    protected boolean reviveChanceUsed = false;
    protected int penaltyMultiplier = 0;
    public enum PENALTYPE  {
        NONE,
        HEALTH,
        EXPERIENCE,
        FOOD,
        ITEM
    }

    public static FallenCapability GetFallCap(LivingEntity player){
        return player.getCapability(FallenProvider.FALLENDATA).orElseGet(FallenCapability::new);
    }

    public void setPenalty(PENALTYPE type, Double amount, String penaltyItem){
        this.penaltyItem = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(penaltyItem)));
        this.setPenalty(type, amount);
    }
    public void setPenalty(PENALTYPE type, Double amount){
        this.penaltyAmount = amount;
        this.penaltyType = type;
    }

    public float getPenaltyAmount(Player player){
        Double actualAmount = penaltyAmount;
        if (actualAmount > 0 && actualAmount < 1){
            actualAmount *= countReviverPenaltyAmount(player);
        }
        if (player.isCreative()) actualAmount = 0D;

        return Math.round(actualAmount);
    }
    public ItemStack getPenaltyItem() {return this.penaltyItem;}
    public PENALTYPE getPenaltyType(){
        return penaltyType;
    }

    public void kill(Player player){
        this.setDying();
        player.hurt(this.damageSource, Float.MAX_VALUE);
        if (!player.isDeadOrDying() || Float.isNaN(player.getHealth())) {
            player.getCombatTracker().recordDamage(this.getDamageSource(), 1,1);
            player.setHealth(0);
            player.die(this.damageSource);
        }
    }

    public double countReviverPenaltyAmount(Player reviver){
        switch (penaltyType) {
            case NONE: return 0;
            case HEALTH: return reviver.getHealth() + reviver.getAbsorptionAmount();
            case EXPERIENCE: return reviver.experienceLevel;
            case FOOD: return (reviver.getFoodData().getFoodLevel() + Math.max(reviver.getFoodData().getSaturationLevel(), 0));
            case ITEM: return reviver.getInventory().countItem(this.penaltyItem.getItem());
        }
        return 0;
    }
    public boolean hasEnough(Player reviver){
        if (reviver.isCreative()) return true;
        if (this.penaltyType == PENALTYPE.NONE) return true;
        return countReviverPenaltyAmount(reviver) - this.getPenaltyAmount(reviver) >= 0;
    }

    public void setDamageSource(DamageSource damageSource){
        this.damageSource = damageSource;
    }

    public DamageSource getDamageSource(){
        return damageSource;
    }

    public float GetTimeLeft(boolean divideByMax) {
        float maxSeconds = getPenaltyTicks(fellEnd);
        if (ReviveMeConfig.timeLeft == 0) maxSeconds = 0;
        getKillTime();

        if (divideByMax)
            return 1 - ((level.getGameTime() - fellStart)/ maxSeconds);

        return ((fellStart + maxSeconds) - level.getGameTime())/20f;
    }

    public int getKillTime(){
        if (ReviveMeConfig.pvpTimer == -1) return -1;
        float maxSeconds = getPenaltyTicks(ReviveMeConfig.pvpTimer * 20);

        return (int) Math.max (0, ((fellStart + maxSeconds) - level.getGameTime())/20f);
    }

    public boolean shouldDie(){
        return ReviveMeConfig.timeLeft != 0 && GetTimeLeft(false) <= 0;
    }

    public void SetTimeLeft(int timeStart, float maxSeconds) {
        this.fellStart = timeStart;
        this.fellEnd = maxSeconds * 20;
        //System.out.println("Time left is!: " + (a/20f));
    }

    public void resumeFallTimer(){
        fellStart = (int) (level.getGameTime() - (revStart - fellStart));
    }

    public boolean isFallen() {
        return isFallen;
    }
    public boolean isDying() {
        return this.isDying;
    }

    public void setDying(){
        this.isDying = true;
    }

    public void setFallen(boolean fallen) {
        this.isFallen = fallen;

        if (!fallen){
            setProgress(0, 1);
            SetTimeLeft(0, 1);
            setOtherPlayer(null);
        }
    }

    public UUID getOtherPlayer() {
        return otherPlayer;
    }

    public boolean isReviver(UUID targUUID){
        if (targUUID == null) return false;

        if (getOtherPlayer() == null) return false;

        return getOtherPlayer().equals(targUUID);
    }

    public void setOtherPlayer(UUID playerID){
        otherPlayer = playerID;
    }

    public void setProgress(int timeStart, int seconds){
        this.revStart = timeStart;
        this.revEnd = seconds * 20;
    }

    public float getProgress() {
        return (level.getGameTime() - revStart) /(float)revEnd;
    }

    public void setSacrificialItems(@Nonnull ArrayList<Item> itemList){
        this.sacrificialItems = itemList;
    }

    public ArrayList<Item> getItemList(){
        return new ArrayList<>(this.sacrificialItems);
    }

    public boolean usedSacrificedItems(){
        return this.sacrificedItemsUsed;
    }
    public void setSacrificedItemsUsed(boolean flag){
        this.sacrificedItemsUsed = flag;
    }
    public boolean usedChance(){
        return this.reviveChanceUsed;
    }
    public void setReviveChanceUsed(boolean flag){
        this.reviveChanceUsed= flag;
    }
    public int getPenaltyMultiplier(){
        return this.penaltyMultiplier;
    }
    public int getPenaltyTicks(float ticks){
        float multiplier = (float) (getPenaltyMultiplier() * ReviveMeConfig.timeReductionPenalty);
        if (ReviveMeConfig.timeReductionPenalty == -1) multiplier = (float) (getPenaltyMultiplier() * ticks);
        else if (ReviveMeConfig.timeReductionPenalty < 1) multiplier *= ticks;
        else if (ReviveMeConfig.timeReductionPenalty >= 1) multiplier *= 20F;

        return Math.max(0, (int) (ticks - multiplier));
    }
    public void setPenaltyMultiplier(int newMultiplier){
        this.penaltyMultiplier = newMultiplier;
    }

    public Tag writeNBT(){
        CompoundTag cNBT = new CompoundTag();
        cNBT.putInt(FELL_START_INT, this.fellStart);
        cNBT.putFloat(FELL_END_FLOAT, this.fellEnd/20);
        cNBT.putBoolean(FALLEN_BOOL, this.isFallen);
        cNBT.putInt(REVIVE_START_INT, this.revStart);
        cNBT.putInt(REVIVE_END_INT, this.revEnd/20);
        cNBT.putString(PENALTY_ENUM, this.penaltyType.name());
        cNBT.putDouble(PENALTY_DOUBLE, this.penaltyAmount);
        cNBT.putString(PENALTY_ITEM, ForgeRegistries.ITEMS.getKey(this.penaltyItem.getItem()).toString());

        //The saved sacrificial items
        StringBuilder ItemList = new StringBuilder();
        for (Item item : sacrificialItems){
            ItemList.append(ForgeRegistries.ITEMS.getKey(item)).append(",");
        }
        cNBT.putString(SACRIFICEITEMS_STRING, ItemList.toString());
        //If the player sacrificed items
        cNBT.putBoolean(SACRIFICEITEMS_BOOL, this.sacrificedItemsUsed);
        //If the player used the chance
        cNBT.putBoolean(REVIVECHANCE_BOOL, this.reviveChanceUsed);
        //How many times the player fell with penalty timer active
        cNBT.putInt(PENALTY_MULTIPLIER_INT, this.penaltyMultiplier);

        if(this.otherPlayer != null)
            cNBT.putUUID(OTHERPLAYER_UUID, this.otherPlayer);
        return cNBT;
    }
    public void readNBT(Tag nbt){
        CompoundTag cNBT = (CompoundTag) nbt;
        this.SetTimeLeft(cNBT.getInt(FELL_START_INT), cNBT.getInt(FELL_END_FLOAT));
        this.setFallen(cNBT.getBoolean(FALLEN_BOOL));
        this.setProgress(cNBT.getInt(REVIVE_START_INT), cNBT.getInt(REVIVE_END_INT));
        this.setPenalty(PENALTYPE.valueOf(cNBT.getString(PENALTY_ENUM)), cNBT.getDouble(PENALTY_DOUBLE), cNBT.getString(PENALTY_ITEM));

        sacrificialItems.clear();
        for (String itemString : cNBT.getString(SACRIFICEITEMS_STRING).split(",")){
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemString));
            if (item != Items.AIR){
                sacrificialItems.add(item);
            }
        }
        this.sacrificedItemsUsed = cNBT.getBoolean(SACRIFICEITEMS_BOOL);
        this.reviveChanceUsed = cNBT.getBoolean(REVIVECHANCE_BOOL);
        this.penaltyMultiplier = cNBT.getInt(PENALTY_MULTIPLIER_INT);

        if(cNBT.hasUUID(OTHERPLAYER_UUID)) {
            this.setOtherPlayer(cNBT.getUUID(OTHERPLAYER_UUID));
        }
        else {
            this.setOtherPlayer(null);
        }
    }
}
