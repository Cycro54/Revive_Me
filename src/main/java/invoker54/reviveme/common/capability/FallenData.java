package invoker54.reviveme.common.capability;

import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.CommonUtil;
import invoker54.invocore.common.util.MathUtil;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.ReviveMathUtil;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveConfigData;
import invoker54.reviveme.common.data.ReviveItemData;
import invoker54.reviveme.common.network.payload.SyncClientCapMsg;
import invoker54.reviveme.init.AttachmentTypesInit;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;
import java.util.stream.Collectors;

public class FallenData implements INBTSerializable<CompoundTag> {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallenData.class, ReviveMeConfig.debugMode);

    public static final String FALLEN_BOOL = "isFallenREVIVE";
    public static final String FELL_START_LONG = "fellStartREVIVE";
    public static final String FELL_END_DOUBLE = "fellEndREVIVE";
    public static final String REVIVE_START_LONG = "revStartREVIVE";
    public static final String REVIVE_END_INT = "revEndREVIVE";
    public static final String OTHERPLAYER_UUID = "otherPlayerREVIVE";
    public static final String PENALTY_MULTIPLIER_INT = "penaltyMultiplierIntREVIVE";
    public static final String CALLED_FOR_HELP_LONG = "calledForHelpLong";
    public static final String SAVED_EFFECTS_TAG = "savedEffectsTag";
    public static final String DOWNED_BY_PLAYER_BOOL = "DOWNED_BY_PLAYER_BOOL";
    public static final String IS_EFFECTS_REMOVED = "IS_EFFECTS_REMOVED";

    public static final String SELF_REVIVE_OPTIONS_STRING = "SELF_REVIVE_OPTIONS_STRING";
    public static final String SACRIFICEITEMS_COMPOUND = "SACRIFICEITEMS_COMPOUND";
    public static final String STATUS_EFFECTS_COMPOUND = "STATUS_EFFECTS_COMPOUND";
    public static final String SELF_REVIVE_COUNT_INT = "SELF_REVIVE_COUNT_INT";
    public static final String IS_CALL_TOGGLED_BOOL = "IS_CALL_TOGGLED_BOOL";
    public static final String REVIVE_STACK_ITEMSTACK = "REVIVE_STACK_ITEMSTACK";
    public static final String MAX_OVERHEAL_DOUBLE = "MAX_OVERHEAL_DOUBLE";
    public static final String CURRENT_OVERHEAL_DOUBLE = "CURRENT_OVERHEAL_DOUBLE";
//    public static final String REVIVE_ITEMS_USED_NBT = "IS_CALL_TOGGLED_BOOL";

    public static boolean FALLEN_HAS_CREATIVE = false;

    protected HolderLookup.Provider provider;
    protected Level level;
    protected Player player;
    protected long revStart = 0;
    protected int revEnd = 0;
    protected long fellStart = 0;
    protected double fellEnd = 0;
    protected DamageSource damageSource;
    protected boolean isFallen = false;
    protected long fallenTick = 0;
    protected UUID otherPlayer = null;
    protected long calledForHelpTime = 0;
    protected boolean isCallToggled = false;
    protected ItemStack reviveStack = null;

    protected int penaltyMultiplier = 0;

    protected CompoundTag savedEffectsTag = new CompoundTag();

    @Override
    public @UnknownNullability CompoundTag serializeNBT(HolderLookup.@NotNull Provider provider) {
        this.provider = provider;
        return this.writeNBT();
    }

    @Override
    public void deserializeNBT(HolderLookup.@NotNull Provider provider, @NotNull CompoundTag compoundTag) {
        this.provider = provider;
        this.readNBT(compoundTag);
    }

    public enum PENALTYPE  {
        NONE,
        HEALTH,
        EXPERIENCE,
        FOOD,
        ITEM
    }

    public enum SELFREVIVETYPE{
        CHANCE,
        RANDOM_ITEMS,
        KILL,
        STATUS_EFFECTS,
        EXPERIENCE,
        CREATIVE,
        NONE
    }
    protected List<SELFREVIVETYPE> selfReviveTypeList = new ArrayList<>(ReviveMeConfig.selfReviveOptions);
    protected List<ItemStack> sacrificialItems = new ArrayList<>();
    protected List<MobEffect> negativeStatusEffects = new ArrayList<>();
    protected int selfReviveCount = 0;
    protected boolean isDownedByPlayer = false;
    protected boolean isEffectsRemoved = false;
    protected double maxOverheal = 0;
    protected double currentOverheal = 0;

    public static FallenData get(LivingEntity player){
        if (!(player instanceof Player)) throw new ClassCastException(player.getClass() + " is not of type Player!");
        FallenData cap = player.getData(AttachmentTypesInit.FALLEN_DATA);
        if (cap.player == null) cap.player = ((Player)player);
        if (cap.level == null) cap.level = player.getCommandSenderWorld();
        if (cap.provider == null) cap.provider = cap.level.registryAccess();
        if (cap.damageSource == null) cap.damageSource = cap.level.damageSources().fellOutOfWorld();
        return cap;
    }

    public void setFallen(boolean fallen) {
        this.isFallen = fallen;

        if (!fallen){
            setProgress(0, 1);
            SetTimeLeft(0, 1, false);
            setOtherPlayerAndItem(null, null);
            this.calledForHelpTime = 0;
            this.isEffectsRemoved = false;
        }
        else {
            if (this.level == null) return;
            this.fallenTick = this.level.getGameTime();
        }
    }

    public void removeOriginalEffects(Player player){
        if (this.isEffectsRemoved) return;
        try {
            player.removeAllEffects();
            this.isEffectsRemoved = true;
        }
        catch (Exception e){
            LOGGER.warn("MobEffect removal failed, remove effects later...");
            e.printStackTrace();
        }
    }

    public boolean canSelfRevive(){
        if (ReviveMeConfig.maxSelfRevives != -1 && getSelfReviveCount() >= ReviveMeConfig.maxSelfRevives) return false;
        if (ReviveMeConfig.disableSelfReviveIfPlayerDowned && isDownedByPlayer) return false;
        return true;
    }

    public void callForHelp(boolean isSneaking){
        if (!this.isCallingForHelp()) this.calledForHelpTime = this.level.getGameTime();
        if (isSneaking) this.isCallToggled = !this.isCallToggled;
    }
    public boolean isCallingForHelp(){
        return callForHelpCooldown() != 1;
    }
    public boolean isCallToggled(){
        return this.isCallToggled;
    }

    public long callForHelpTicks(){
        return this.level.getGameTime() - this.calledForHelpTime;
    }

    public double callForHelpCooldown(){
        long timePassed = this.callForHelpTicks();
        boolean isReady = timePassed == (ReviveMeConfig.reviveHelpDuration*20);

        if (!this.isCallToggled && isReady && player.level().isClientSide){
            this.calledForHelpTime -= 1;
            float pitch = MathUtil.randomFloat(0.9f, 1.2F);
            float volume = MathUtil.randomFloat(0.7f, 0.8F);

            player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), volume, pitch);
        }
        return Math.min(timePassed/(ReviveMeConfig.reviveHelpDuration*20),1);
    }

    public void setMaxOverheal(){
        int penalty = this.getPenaltyMultiplier();
        double penaltyPercentage =  penalty * ReviveMeConfig.overhealPenaltyPercentage;
        this.maxOverheal = ReviveMeConfig.overhealAmount * (1 + penaltyPercentage);
        this.currentOverheal = 0;
    }

    public boolean addOverheal(float healAmount){
        if (maxOverheal == 0) return false;
        this.currentOverheal += healAmount;
        return this.currentOverheal >= maxOverheal;
    }

    public double getOverhealPercentage(){
        if (this.maxOverheal == 0) return 0;
        return Math.min(1,this.currentOverheal/this.maxOverheal);
    }

    public float getPenaltyAmount(Player player){
        Double actualAmount = ReviveMeConfig.penaltyAmount;
        if (actualAmount > 0 && actualAmount < 1){
            actualAmount *= countReviverPenaltyAmount(player);
        }

        if (ReviveMeConfig.penaltyType == PENALTYPE.EXPERIENCE){
            actualAmount = (double) ReviveMathUtil.getExperienceFromLevel((int) Math.round(actualAmount), (float) (actualAmount % 1));
        }

        if (player.isCreative()) actualAmount = 0D;

        return Math.round(actualAmount);
    }

    public void forceDeath() {
//        boolean canDieWhenTimerExpires = !ReviveMeConfig.dieWhenTimerEnds && this.timeRanOut();
//        boolean canGiveUp = ReviveMeConfig.canGiveUp;
//
//        if (!bypassChecks && ()) return;
//        //Times to die
//        //timer runs out and
//        //Force death
//        if (!ReviveMeConfig.dieWhenTimerEnds && )
//        if (!ReviveMeConfig.canGiveUp && !this.timeRanOut() && !killedByPlayer) return;

        this.player.playSound(SoundEvents.PLAYER_DEATH, 1, (this.player.getRandom().nextFloat() - this.player.getRandom().nextFloat()) * 0.2F + 1.0F);
        this.player.getCombatTracker().recordDamage(this.getDamageSource(), 1);
        if (this.damageSource.getEntity() instanceof Player) this.player.setLastHurtByPlayer((Player) this.damageSource.getEntity());
        if (this.damageSource.getEntity() instanceof Mob) this.player.setLastHurtByMob((Mob) this.damageSource.getEntity());
        this.player.die(this.damageSource);
        this.player.setHealth(0);
    }

    public double countReviverPenaltyAmount(Player reviver){
        switch (ReviveMeConfig.penaltyType) {
            case NONE: return 0;
            case HEALTH: return reviver.getHealth() + reviver.getAbsorptionAmount();
            case EXPERIENCE: return reviver.experienceLevel;
            case FOOD: return (reviver.getFoodData().getFoodLevel() + Math.max(reviver.getFoodData().getSaturationLevel(), 0));
            case ITEM: break;
        }
        return 0;
    }

    public static boolean hasSimilarData(ItemStack baseStack, ItemStack otherStack){
        boolean baseHasTag = baseStack.has(DataComponents.CUSTOM_DATA);
        boolean otherHasTag = otherStack.has(DataComponents.CUSTOM_DATA);
        if (baseHasTag != otherHasTag) return false;
        if (baseHasTag && !(baseStack.get(DataComponents.CUSTOM_DATA).matchedBy(otherStack.get(DataComponents.CUSTOM_DATA).copyTag()))){
            return false;
        }

        boolean baseHasName = baseStack.has(DataComponents.CUSTOM_NAME);
        boolean otherHasName = otherStack.has(DataComponents.CUSTOM_NAME);
        if (baseHasName != otherHasName) return false;
        if (baseHasName && !(baseStack.get(DataComponents.CUSTOM_NAME).contains(otherStack.get(DataComponents.CUSTOM_NAME).copy()))){
            return false;
        }

        boolean baseHasEnchants = baseStack.has(DataComponents.ENCHANTMENTS);
        boolean otherHasEnchants = otherStack.has(DataComponents.ENCHANTMENTS);
        if (baseHasEnchants != otherHasEnchants) return false;
        if (baseHasEnchants && !(baseStack.get(DataComponents.ENCHANTMENTS).equals(otherStack.get(DataComponents.ENCHANTMENTS)))){
            return false;
        }

        boolean baseHasStoredEnchants = baseStack.has(DataComponents.STORED_ENCHANTMENTS);
        boolean otherHasStoredEnchants = otherStack.has(DataComponents.STORED_ENCHANTMENTS);
        if (baseHasStoredEnchants != otherHasStoredEnchants) return false;
        if (baseHasStoredEnchants && !(baseStack.get(DataComponents.STORED_ENCHANTMENTS).equals(otherStack.get(DataComponents.STORED_ENCHANTMENTS)))){
            return false;
        }

        return true;
    }

    public boolean hasEnough(Player reviver){
        if (reviver.isCreative()) return true;
        if (ReviveMeConfig.penaltyType == PENALTYPE.NONE) return true;
        ReviveItemData itemData = ReviveItemData.getData(reviver.getMainHandItem(), ReviveItemData.USER.REVIVER);
        if (itemData != null) return itemData.getItemCount(reviver, reviver.getMainHandItem()) >= itemData.getCountRequired();
        if (ReviveMeConfig.penaltyType == PENALTYPE.ITEM) return false;

        float penaltyAmount = this.getPenaltyAmount(reviver);
        if (ReviveMeConfig.penaltyType == PENALTYPE.EXPERIENCE) penaltyAmount = ReviveMathUtil.getLevelFromExperience((int) penaltyAmount);

        return countReviverPenaltyAmount(reviver) - penaltyAmount >= 0;
    }

    public void setDamageSource(DamageSource damageSource){
        this.damageSource = damageSource;
        this.isDownedByPlayer = (this.damageSource.getEntity() instanceof Player);
    }

    public DamageSource getDamageSource(){
        return damageSource;
    }

    public float getTimeLeft(boolean divideByMax) {
//        double maxSeconds = getPenaltyTicks(fellEnd);
        double maxSeconds = fellEnd;
        if (ReviveMeConfig.timeLeft == 0) maxSeconds = 0;

        if (divideByMax)
            return (float) (1 - ((level.getGameTime() - fellStart)/ maxSeconds));

        return (float) (((fellStart + maxSeconds) - level.getGameTime())/20);
    }

    public float getKillTime(boolean divideByMax){
        if (ReviveMeConfig.pvpTimer == -1) return -1;
        double maxSeconds = getPenaltyTicks(ReviveMeConfig.pvpTimer * 20);
        maxSeconds = Math.min(maxSeconds, Math.max(0, getTimeLeft(false)));

        if (divideByMax)
            return (float) Math.max (0, (1 - ((level.getGameTime() - fellStart)/maxSeconds)));

        return (float) Math.max (0, ((fellStart + maxSeconds) - level.getGameTime())/20);
    }

    public boolean timeRanOut(){
        return ReviveMeConfig.timeLeft != 0 && getTimeLeft(false) <= 0;
    }

    public boolean canDieThisTick(){
        return this.fallenTick != this.level.getGameTime();
    }

    public void SetTimeLeft(long timeStart, double maxSeconds, boolean applyPenalty) {
        this.fellStart = timeStart;

        double maxTicks = maxSeconds * 20d;

        if (applyPenalty) maxTicks = getPenaltyTicks(maxTicks);
        this.fellEnd = (long) (maxTicks);

        //System.out.println("Time left is!: " + (a/20f));
    }

    public void resumeFallTimer(){
        fellStart = (level.getGameTime() - (revStart - fellStart));
    }

    public void resetTimer(){
        this.fellStart = this.level.getGameTime();
    }

    public void pauseTimerOnLogout(){
        this.revStart = this.player.level().getGameTime();
    }

    public boolean isFallen() {
        return isFallen;
    }

    public boolean isDownedByPlayer(){
        return isDownedByPlayer;
    }

    public UUID getOtherPlayer() {
        return otherPlayer;
    }

    public boolean isReviver(UUID targUUID){
        if (targUUID == null) return false;

        if (getOtherPlayer() == null) return false;

        return getOtherPlayer().equals(targUUID);
    }

    public void setOtherPlayerAndItem(UUID playerID, ItemStack chosenStack){
        otherPlayer = playerID;
        reviveStack = chosenStack;
    }

    public ItemStack getReviveStack(){
        return this.reviveStack;
    }

    public void setProgress(long timeStart, double seconds){
        this.revStart = timeStart;
        this.revEnd = (int) (seconds * 20);
    }

    public float getProgress(boolean divideByMax) {
        long passedTicks = Math.min (revEnd, (level.getGameTime() - revStart));
        return divideByMax ? (float) passedTicks/revEnd : passedTicks;
    }

    public SELFREVIVETYPE getSelfReviveOption(int mouseButton){
        if (this.selfReviveTypeList.isEmpty()) return SELFREVIVETYPE.NONE;
        return this.selfReviveTypeList.get(mouseButton);
    }

    public void cycleReviveOptions(SELFREVIVETYPE selfrevivetype){
        if (selfrevivetype == null) selfrevivetype = this.selfReviveTypeList.get(0);
        this.selfReviveTypeList.remove(selfrevivetype);
        this.selfReviveTypeList.add(selfrevivetype);
    }

    public void useReviveOption(SELFREVIVETYPE selectedOption, Player player) {
        Inventory playerInv = player.getInventory();

        double penaltyPercentage = this.getSelfPenaltyPercentage();
        incrementSelfReviveCount();
        cycleReviveOptions(selectedOption);
//        LOGGER.warn("Penalty percentage: " + (penaltyPercentage));

        switch (selectedOption) {
            case CHANCE: {
                ReviveConfigData.reviveText.setArgs();
                if (player.level().random.nextFloat() <= (ReviveMeConfig.reviveChance * (1 - penaltyPercentage))) {
                    ReviveMeConfig.configReviveData.revivePlayer(player, false, player, selectedOption);
                    return;
                }
                else if (!ReviveMeConfig.reviveChanceKillOnFail){
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, MathUtil.randomFloat(0.7F,1.0F), MathUtil.randomFloat(0.8F,1.0F));

//                    if (!this.canSelfRevive() && ((!player.getServer().isDedicatedServer() &&
//                            player.getServer().getPlayerCount() == 1))) break;

                    refreshSelfReviveTypes(player);

                    CompoundTag nbt = new CompoundTag();
                    nbt.put(player.getStringUUID(), this.writeNBT());
                    this.syncClient(true);
                    return;
                }
                else if (!ReviveMeConfig.canGiveUp && ReviveMeConfig.maxSelfRevives != -1){
                    player.level().playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK, SoundSource.PLAYERS, MathUtil.randomFloat(0.7F,1.0F), MathUtil.randomFloat(0.8F,1.0F));

                    this.selfReviveCount = ReviveMeConfig.maxSelfRevives;

                    CompoundTag nbt = new CompoundTag();
                    nbt.put(player.getStringUUID(), this.writeNBT());
                    this.syncClient(true);
                    return;
                }
                break;
            }
            case RANDOM_ITEMS: {
                if (this.getItemList().isEmpty()) break;
                ReviveMeConfig.configReviveData.revivePlayer(player, false, player, selectedOption);
                for (ItemStack sacrificeStack : this.getItemList()) {
                    int count = FallenData.countItem(playerInv, sacrificeStack);
                    int amountToLose = (int) Math.round(Math.max(1, count *
                            (ReviveMeConfig.sacrificialItemPercent *(1+penaltyPercentage))));

                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        if (!ReviveMeConfig.sacrificialItemTakesHotbar && (a < 9 || a == 40)) continue;
                        ItemStack containerStack = playerInv.getItem(a);
                        if (!ItemStack.isSameItem(sacrificeStack, containerStack)) continue;
                        if (!hasSimilarData(sacrificeStack, containerStack)) continue;
                        int takeAway = (Math.min(amountToLose, containerStack.getCount()));
                        amountToLose -= takeAway;
                        containerStack.setCount(containerStack.getCount() - takeAway);
                        if (amountToLose == 0) break;
                    }
                }
                this.sacrificialItems.clear();
                return;
            }
            case KILL: {
                ReviveMeConfig.configReviveData.revivePlayer(player, false, player,selectedOption);
                int seconds = Math.max(1, (int) (ReviveMeConfig.reviveKillTime * 20 * (1 - penaltyPercentage)));
                int killCount = ReviveMeConfig.reviveKillAmount;
                player.addEffect(new MobEffectInstance(MobEffectInit.KILL_REVIVE_EFFECT, seconds,  killCount-1));
                return;
            }
            case STATUS_EFFECTS: {
                ReviveConfigData configReviveDataCopy = ReviveMeConfig.configReviveData.copy();
                if (ReviveMeConfig.disableReviveEffects) configReviveDataCopy.setReviveEffects(null);
                configReviveDataCopy.revivePlayer(player, false, player, selectedOption);

                int amp = 0;
                int duration = (int) (20 * ReviveMeConfig.negativeEffectsTime * (1 + penaltyPercentage));
                if (this.negativeStatusEffects.size() == 1) {
                    amp = 1;
                }

                for (MobEffect effect : this.negativeStatusEffects) {
                    player.addEffect(new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration, amp));
                }
                return;
            }
            case EXPERIENCE:{
                if (player.experienceLevel < ReviveMeConfig.minReviveXPLevel) break;
                ReviveMeConfig.configReviveData.revivePlayer(player, false, player, selectedOption);
                ReviveMathUtil.giveExperience(player, -ReviveMathUtil.getExperienceFromLevel((int)(player.experienceLevel * ReviveMeConfig.reviveXPLossPercentage
                        * (1 + penaltyPercentage)), 0));
                return;
            }
        }

        if (ReviveMeConfig.canGiveUp) this.forceDeath();
    }

    public void incrementSelfReviveCount(){
        this.selfReviveCount += 1;
    }

    public void resetSelfReviveCount(){
        this.selfReviveCount = 0;
    }

    public void refreshSelfReviveTypes(Player player){
        if (!this.selfReviveTypeList.containsAll(ReviveMeConfig.selfReviveOptions) ||
                this.selfReviveTypeList.size() != ReviveMeConfig.selfReviveOptions.size())
            this.selfReviveTypeList = new ArrayList<>(ReviveMeConfig.selfReviveOptions);

        if (ReviveMeConfig.randomizeSelfReviveOptions) Collections.shuffle(this.selfReviveTypeList);

        if (this.selfReviveTypeList.contains(SELFREVIVETYPE.RANDOM_ITEMS)){
            setSacrificialItems(player.getInventory());
        }
        if (this.selfReviveTypeList.contains(SELFREVIVETYPE.STATUS_EFFECTS)){
//            negativeEffects.forEach(e -> LOGGER.warn(e.getRegistryName().toString()));
            this.negativeStatusEffects.clear();
            this.negativeStatusEffects.addAll(
                    CommonUtil.pickRandomObjectsFromList(Math.random() > 0.5F ? 1 : 2, ReviveMeConfig.harmfulEffects));
        }

        int count = 0;
        if (ReviveMeConfig.onlyUseAvailableOptions) {
            for (int a = 0; a < this.selfReviveTypeList.size(); a++) {
                if (count == 2) break;

                switch (this.selfReviveTypeList.get(a)) {
                    case RANDOM_ITEMS:
                        if (getItemList().isEmpty()) continue;
                        break;
                    case EXPERIENCE:
                        if (player.experienceLevel < ReviveMeConfig.minReviveXPLevel) continue;
                        break;
                    default:
                        break;
                }
                this.selfReviveTypeList.add(0, this.selfReviveTypeList.remove(a));
                count++;
            }
        }
    }

    public List<MobEffect> getNegativeStatusEffects(){
        return new ArrayList<>(this.negativeStatusEffects);
    }

    public double getSelfPenaltyPercentage(){
        return Math.min(1, this.getSelfReviveCount() * ReviveMeConfig.selfPenaltyPercentage);
    }

    //This is how many self revive options have been used since last refresh
    public int getSelfReviveCount(){
        return this.selfReviveCount;
    }

    public void setSacrificialItems(Inventory inventory){
        if (inventory == null) return;
//        Pair<Integer, List<ItemStack>> specificPair = getSpecificItem(inventory.player);

        //Generate a sacrificial item list
        ArrayList<ItemStack> playerItems = new ArrayList<>();
        for (int a = 0; a < inventory.items.size(); a++) {
            if (!ReviveMeConfig.sacrificialItemTakesHotbar && (a < 9 || a == 40)) continue;
            ItemStack newStack = inventory.getItem(a);
            ReviveItemData itemData = ReviveItemData.getData(newStack, ReviveItemData.USER.BOTH);
            if (itemData != null) continue;

            if (!newStack.isStackable()) continue;
//            if (specificPair.getValue().contains(newStack)) continue;
            if (playerItems.stream().anyMatch(listStack ->
                    ItemStack.isSameItem(listStack, newStack) && hasSimilarData(newStack, listStack))) continue;
            if (newStack.isEmpty()) continue;
            playerItems.add(this.level.random.nextInt(Math.max(1, playerItems.size())), newStack.copy());
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
        if (!canSelfRevive()) return false;
        return this.sacrificialItems.stream().anyMatch(
                sacrificialStack -> ItemStack.isSameItem(sacrificialStack, mainStack) &&
                        hasSimilarData(mainStack, sacrificialStack));
    }
    public static int countItem(Inventory inventory, ItemStack sacrificialStack) {
        int count = 0;

        for (int a = 0; a < inventory.getContainerSize(); a++) {
            if (!ReviveMeConfig.sacrificialItemTakesHotbar && (a < 9 || a == 40)) continue;
            ItemStack containerStack = inventory.getItem(a);
            if (!ItemStack.isSameItem(sacrificialStack,containerStack)) continue;
            if (!hasSimilarData(sacrificialStack, containerStack)) continue;
            count += containerStack.getCount();
        }
        return count;
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

        boolean isWhitelist = ReviveMeConfig.revertEffectBlacklist.contains("//");
        List<String> effectTypeList = ReviveMeConfig.revertEffectBlacklist.stream()
                .filter(s -> StringUtils.countMatches(s, ";") == 2)
                .map(s -> s.replace(";", "")).collect(Collectors.toList());

        for (MobEffectInstance effectInstance : new ArrayList<>(player.getActiveEffects())) {
            boolean hasMatch = effectTypeList.contains(effectInstance.getEffect().value().getCategory().toString());
            if (!hasMatch){
                hasMatch = ReviveMeConfig.revertEffectBlacklist.stream().anyMatch(listString ->
                        BuiltInRegistries.MOB_EFFECT.getKey(effectInstance.getEffect().value()).toString().contains(listString));
            }

            if (isWhitelist != hasMatch) continue;

            Tag savedEffectTag = effectInstance.save();
            effectsTag.put(effectsTag.size() + "", savedEffectTag);
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

    public void syncClient(boolean resetBinds){
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player,
                new SyncClientCapMsg(player.getStringUUID(), this.writeNBT(), resetBinds));
    }

    public CompoundTag writeNBT(){
        CompoundTag cNBT = new CompoundTag();
        cNBT.putLong(FELL_START_LONG, this.fellStart);
        cNBT.putDouble(FELL_END_DOUBLE, this.fellEnd/20);
        cNBT.putBoolean(FALLEN_BOOL, this.isFallen);
        cNBT.putLong(REVIVE_START_LONG, this.revStart);
        cNBT.putInt(REVIVE_END_INT, this.revEnd/20);

        //Save Self revive option list
        String selfReviveListString = "";
        for (SELFREVIVETYPE selfrevivetype : this.selfReviveTypeList){
            selfReviveListString = selfReviveListString.concat(selfrevivetype.name()+",");
        }
        cNBT.putString(SELF_REVIVE_OPTIONS_STRING, selfReviveListString);

        //Save Self revive status effects
        CompoundTag statusEffectNBT = new CompoundTag();
        int count = 0;
        for (MobEffect effect : this.negativeStatusEffects) {
            statusEffectNBT.put("" + count, MobEffect.CODEC.encodeStart(NbtOps.INSTANCE,
                    BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect)).getOrThrow());
            count++;
        }
        cNBT.put(STATUS_EFFECTS_COMPOUND, statusEffectNBT);

        //The saved sacrificial items
        CompoundTag itemCompound = new CompoundTag();
        for (ItemStack item : sacrificialItems) {
            itemCompound.put(itemCompound.size() + "", item.save(this.provider));
        }
        cNBT.put(SACRIFICEITEMS_COMPOUND, itemCompound);
        //How many times the player fell with penalty timer active
        cNBT.putInt(PENALTY_MULTIPLIER_INT, this.penaltyMultiplier);

        cNBT.putLong(CALLED_FOR_HELP_LONG, this.calledForHelpTime);

        cNBT.put(SAVED_EFFECTS_TAG, this.savedEffectsTag);
        if(this.otherPlayer != null)
            cNBT.putUUID(OTHERPLAYER_UUID, this.otherPlayer);

        cNBT.putInt(SELF_REVIVE_COUNT_INT, this.selfReviveCount);

        cNBT.putBoolean(DOWNED_BY_PLAYER_BOOL, this.isDownedByPlayer());

        cNBT.putBoolean(IS_EFFECTS_REMOVED, this.isEffectsRemoved);

        cNBT.putBoolean(IS_CALL_TOGGLED_BOOL, this.isCallToggled);

        if (this.reviveStack != null) cNBT.put(REVIVE_STACK_ITEMSTACK, this.reviveStack.save(this.provider));

        cNBT.putDouble(MAX_OVERHEAL_DOUBLE, this.maxOverheal);

        cNBT.putDouble(CURRENT_OVERHEAL_DOUBLE, this.currentOverheal);

        return cNBT;
    }
    public void readNBT(Tag nbt){
        CompoundTag cNBT = (CompoundTag) nbt;
        this.SetTimeLeft(cNBT.getLong(FELL_START_LONG), cNBT.getDouble(FELL_END_DOUBLE), false);
        this.setFallen(cNBT.getBoolean(FALLEN_BOOL));
        this.setProgress(cNBT.getLong(REVIVE_START_LONG), cNBT.getInt(REVIVE_END_INT));

        this.selfReviveTypeList.clear();
        for (String s : cNBT.getString(SELF_REVIVE_OPTIONS_STRING).split(",")) {
            try {
                this.selfReviveTypeList.add(SELFREVIVETYPE.valueOf(s));
            } catch (Exception ignored) {
            }
        }
        if (this.selfReviveTypeList.isEmpty()) this.selfReviveTypeList.addAll(ReviveMeConfig.selfReviveOptions);

        //Save Self revive status effects
        this.negativeStatusEffects.clear();
        CompoundTag statusEffectNBT = cNBT.getCompound(STATUS_EFFECTS_COMPOUND);
        for (String s : statusEffectNBT.getAllKeys()) {
            try {
            this.negativeStatusEffects.add(MobEffect.CODEC.parse(
                        NbtOps.INSTANCE, (Tag) statusEffectNBT.get(s)).getOrThrow().value());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        sacrificialItems.clear();
        CompoundTag itemCompound = cNBT.getCompound(SACRIFICEITEMS_COMPOUND);
        if (!itemCompound.isEmpty()) {
            for (String key : itemCompound.getAllKeys()) {
                Optional<ItemStack> optional = ItemStack.parse(this.provider, itemCompound.getCompound(key));
                if (optional.isEmpty()) continue;
                ItemStack sacrificeStack = optional.get();
                sacrificialItems.add(sacrificeStack);
            }
        }

        this.penaltyMultiplier = cNBT.getInt(PENALTY_MULTIPLIER_INT);

        this.calledForHelpTime = cNBT.getLong(CALLED_FOR_HELP_LONG);

        this.savedEffectsTag = cNBT.getCompound(SAVED_EFFECTS_TAG);

        if(cNBT.hasUUID(OTHERPLAYER_UUID)) {
            this.setOtherPlayerAndItem(cNBT.getUUID(OTHERPLAYER_UUID), null);
        }
        else {
            this.setOtherPlayerAndItem(null, null);
        }

        this.selfReviveCount = cNBT.getInt(SELF_REVIVE_COUNT_INT);

        this.isDownedByPlayer = cNBT.getBoolean(DOWNED_BY_PLAYER_BOOL);

        this.isEffectsRemoved = cNBT.getBoolean(IS_EFFECTS_REMOVED);

        this.isCallToggled = cNBT.getBoolean(IS_CALL_TOGGLED_BOOL);

        this.reviveStack = null;
        if (cNBT.contains(REVIVE_STACK_ITEMSTACK)){
            Optional<ItemStack> optional = ItemStack.parse(this.provider, cNBT.getCompound(REVIVE_STACK_ITEMSTACK));
            if (optional.isPresent()) this.reviveStack = optional.get();
        }

        this.maxOverheal = cNBT.getDouble(MAX_OVERHEAL_DOUBLE);

        this.currentOverheal = cNBT.getDouble(CURRENT_OVERHEAL_DOUBLE);
    }
}
