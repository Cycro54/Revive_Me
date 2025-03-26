package invoker54.reviveme.common.capability;

import invoker54.reviveme.common.api.FallenProvider;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FallenCapability {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String FALLEN_BOOL = "isFallenREVIVE";
    public static final String FELL_START_LONG = "fellStartREVIVE";
    public static final String FELL_END_DOUBLE = "fellEndREVIVE";
    public static final String REVIVE_START_LONG = "revStartREVIVE";
    public static final String REVIVE_END_INT = "revEndREVIVE";
    public static final String PENALTY_ENUM = "penaltyTypeREVIVE";
    public static final String PENALTY_DOUBLE = "penaltyDoubleREVIVE";
    public static final String PENALTY_ITEM = "penaltyItemREVIVE";
    public static final String OTHERPLAYER_UUID = "otherPlayerREVIVE";
    public static final String SACRIFICEITEMS_COMPOUND = "sacrificeItemCompoundREVIVE";
    public static final String SACRIFICEITEMS_BOOL = "sacrificeItemsBoolREVIVE";
    public static final String REVIVECHANCE_BOOL = "reviveChanceBoolREVIVE";
    public static final String PENALTY_MULTIPLIER_INT = "penaltyMultiplierIntREVIVE";
    public static final String CALLED_FOR_HELP_LONG = "calledForHelpLong";
    public static final String SAVED_EFFECTS_TAG = "savedEffectsTag";
    //endregion

    public FallenCapability(Level level){
        this.level = level;
    }
    public FallenCapability() {

    }

    protected Level level;
    protected long revStart = 0;
    protected int revEnd = 0;
    protected long fellStart = 0;
    protected double fellEnd = 0;
    protected DamageSource damageSource = DamageSource.OUT_OF_WORLD;
    protected boolean isFallen = false;
    protected boolean isDying = false;
    protected UUID otherPlayer = null;
    protected Double penaltyAmount = 0D;
    protected ItemStack penaltyItem = ItemStack.EMPTY;
    protected PENALTYPE penaltyType = PENALTYPE.HEALTH;
    protected long calledForHelpTime = 0;

    protected List<ItemStack> sacrificialItems = new ArrayList<>();
    protected boolean sacrificedItemsUsed = false;
    protected boolean reviveChanceUsed = false;
    protected int penaltyMultiplier = 0;

    protected CompoundTag savedEffectsTag = new CompoundTag();

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

    public void callForHelp(){
        this.calledForHelpTime = this.level.getGameTime();
    }
    public boolean isCallingForHelp(){
        return this.level.getGameTime() < (this.calledForHelpTime + (ReviveMeConfig.reviveHelpDuration*20));
    }

    public double callForHelpCooldown(){
        long timePassed = this.level.getGameTime() - this.calledForHelpTime;
        return Math.min(timePassed/(ReviveMeConfig.reviveHelpCooldown*20),1);
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
        double maxSeconds = getPenaltyTicks(fellEnd);
        if (ReviveMeConfig.timeLeft == 0) maxSeconds = 0;
        getKillTime();

        if (divideByMax)
            return (float) (1 - ((level.getGameTime() - fellStart)/ maxSeconds));

        return (float) (((fellStart + maxSeconds) - level.getGameTime())/20);
    }

    public int getKillTime(){
        if (ReviveMeConfig.pvpTimer == -1) return -1;
        float maxSeconds = getPenaltyTicks(ReviveMeConfig.pvpTimer * 20);

        return (int) Math.max (0, ((fellStart + maxSeconds) - level.getGameTime())/20f);
    }

    public boolean shouldDie(){
        return ReviveMeConfig.timeLeft != 0 && GetTimeLeft(false) <= 0;
    }

    public void SetTimeLeft(long timeStart, double maxSeconds) {
        this.fellStart = timeStart;
        this.fellEnd = (long) (maxSeconds * 20);
        //System.out.println("Time left is!: " + (a/20f));
    }

    public void resumeFallTimer(){
        fellStart = (level.getGameTime() - (revStart - fellStart));
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

    public void setProgress(long timeStart, int seconds){
        this.revStart = timeStart;
        this.revEnd = seconds * 20;
    }

    public float getProgress() {
        return Math.min (1, (level.getGameTime() - revStart) /(float) revEnd);
    }

    public void setSacrificialItems(Inventory inventory){
        if (inventory == null){
            this.sacrificialItems.clear();
            return;
        }

        //Generate a sacrificial item list
        ArrayList<ItemStack> playerItems = new ArrayList<>();
        for (ItemStack newStack : inventory.items) {
            if (!newStack.isStackable()) continue;
            if (playerItems.stream().anyMatch(listStack ->
                    listStack.sameItem(newStack) && ItemStack.tagMatches(listStack, newStack))) continue;
            if (newStack.isEmpty()) continue;
            playerItems.add(this.level.random.nextInt(Math.max(1,playerItems.size())), newStack);
        }
        //Remove all except 4
        while (playerItems.size() > 4) {
            playerItems.remove(this.level.random.nextInt(playerItems.size()));
        }

        this.sacrificialItems = playerItems;
    }

    public ArrayList<ItemStack> getItemList(){
        return new ArrayList<>(this.sacrificialItems);
    }

    public boolean isSacrificialItem(ItemStack mainStack) {
        return this.sacrificialItems.stream().anyMatch(
                sacrificialStack -> sacrificialStack.sameItem(mainStack) &&
                        ItemStack.tagMatches(mainStack, sacrificialStack));
    }

    public static int countItem(Inventory inventory, ItemStack sacrificialStack) {
        int count = 0;

        for (int a = 0; a < inventory.getContainerSize(); a++) {
            ItemStack containerStack = inventory.getItem(a);
            if (!sacrificialStack.sameItem(containerStack)) continue;
            if (!ItemStack.tagMatches(sacrificialStack, containerStack)) continue;
            count += containerStack.getCount();
        }
        return count;
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
    public long getPenaltyTicks(double ticks){
        double multiplier = getPenaltyMultiplier() * ReviveMeConfig.timeReductionPenalty;
        if (ReviveMeConfig.timeReductionPenalty == -1) multiplier = getPenaltyMultiplier() * ticks;
        else if (ReviveMeConfig.timeReductionPenalty < 1) multiplier *= ticks;
        else if (ReviveMeConfig.timeReductionPenalty >= 1) multiplier *= 20F;

        return (long) Math.max(0, ticks - multiplier);
    }
    public void setPenaltyMultiplier(int newMultiplier){
        this.penaltyMultiplier = newMultiplier;
    }

    public void saveEffects(Player player){
        CompoundTag effectsTag = new CompoundTag();
        for (MobEffectInstance effectInstance : player.getActiveEffects()){
            if (effectInstance.getDuration() <= 1*20) continue;
            CompoundTag savedEffectTag = new CompoundTag();
            effectInstance.save(savedEffectTag);
            effectsTag.put(effectsTag.size()+"", savedEffectTag);
        }
        this.savedEffectsTag = effectsTag;
    }

    public void loadEffects(Player player){
        for (String key : this.savedEffectsTag.getAllKeys()){
            MobEffectInstance instance = MobEffectInstance.load(this.savedEffectsTag.getCompound(key));
            if (instance == null) continue;
            player.addEffect(instance);
        }
        this.savedEffectsTag = new CompoundTag();
    }

    public Tag writeNBT(){
        CompoundTag cNBT = new CompoundTag();
        cNBT.putLong(FELL_START_LONG, this.fellStart);
        cNBT.putDouble(FELL_END_DOUBLE, this.fellEnd/20);
        cNBT.putBoolean(FALLEN_BOOL, this.isFallen);
        cNBT.putLong(REVIVE_START_LONG, this.revStart);
        cNBT.putInt(REVIVE_END_INT, this.revEnd/20);
        cNBT.putString(PENALTY_ENUM, this.penaltyType.name());
        cNBT.putDouble(PENALTY_DOUBLE, this.penaltyAmount);
        cNBT.putString(PENALTY_ITEM, ForgeRegistries.ITEMS.getKey(this.penaltyItem.getItem()).toString());

        //The saved sacrificial items
        CompoundTag itemCompound = new CompoundTag();
        for (ItemStack item : sacrificialItems){
            itemCompound.put(itemCompound.size()+"", item.serializeNBT());
//            ItemList.append(ForgeRegistries.ITEMS.getKey(item)).append(",");
        }
        cNBT.put(SACRIFICEITEMS_COMPOUND, itemCompound);
        //If the player sacrificed items
        cNBT.putBoolean(SACRIFICEITEMS_BOOL, this.sacrificedItemsUsed);
        //If the player used the chance
        cNBT.putBoolean(REVIVECHANCE_BOOL, this.reviveChanceUsed);
        //How many times the player fell with penalty timer active
        cNBT.putInt(PENALTY_MULTIPLIER_INT, this.penaltyMultiplier);

        cNBT.putLong(CALLED_FOR_HELP_LONG, this.calledForHelpTime);

        cNBT.put(SAVED_EFFECTS_TAG, this.savedEffectsTag);
        if(this.otherPlayer != null)
            cNBT.putUUID(OTHERPLAYER_UUID, this.otherPlayer);
        return cNBT;
    }
    public void readNBT(Tag nbt){
        CompoundTag cNBT = (CompoundTag) nbt;
        this.SetTimeLeft(cNBT.getLong(FELL_START_LONG), cNBT.getDouble(FELL_END_DOUBLE));
        this.setFallen(cNBT.getBoolean(FALLEN_BOOL));
        this.setProgress(cNBT.getLong(REVIVE_START_LONG), cNBT.getInt(REVIVE_END_INT));
        this.setPenalty(PENALTYPE.valueOf(cNBT.getString(PENALTY_ENUM)), cNBT.getDouble(PENALTY_DOUBLE), cNBT.getString(PENALTY_ITEM));

        sacrificialItems.clear();
        CompoundTag itemCompound = cNBT.getCompound(SACRIFICEITEMS_COMPOUND);
        if (!itemCompound.isEmpty()){
            for (String key: itemCompound.getAllKeys()){
                ItemStack sacrificeStack = ItemStack.of(itemCompound.getCompound(key));
                if (sacrificeStack.isEmpty()) continue;
                sacrificialItems.add(sacrificeStack);
            }
        }
        this.sacrificedItemsUsed = cNBT.getBoolean(SACRIFICEITEMS_BOOL);
        this.reviveChanceUsed = cNBT.getBoolean(REVIVECHANCE_BOOL);
        this.penaltyMultiplier = cNBT.getInt(PENALTY_MULTIPLIER_INT);

        this.calledForHelpTime = cNBT.getLong(CALLED_FOR_HELP_LONG);

        this.savedEffectsTag = cNBT.getCompound(SAVED_EFFECTS_TAG);

        if(cNBT.hasUUID(OTHERPLAYER_UUID)) {
            this.setOtherPlayer(cNBT.getUUID(OTHERPLAYER_UUID));
        }
        else {
            this.setOtherPlayer(null);
        }
    }
}
