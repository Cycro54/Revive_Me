package invoker54.reviveme.common.capability;

import com.mojang.serialization.JavaOps;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.CommonUtil;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import invoker54.reviveme.init.AttachmentTypesInit;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.*;

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

    public static final String SELF_REVIVE_OPTIONS_STRING = "SELF_REVIVE_OPTIONS_STRING";
    public static final String SACRIFICEITEMS_COMPOUND = "SACRIFICEITEMS_COMPOUND";
    public static final String STATUS_EFFECTS_COMPOUND = "STATUS_EFFECTS_COMPOUND";
    public static final String SELF_REVIVE_COUNT_INT = "SELF_REVIVE_COUNT_INT";
    //endregion

    protected HolderLookup.Provider provider;
    protected Level level;
    protected long revStart = 0;
    protected int revEnd = 0;
    protected long fellStart = 0;
    protected double fellEnd = 0;
    protected DamageSource damageSource;
    protected boolean isFallen = false;
    protected UUID otherPlayer = null;
    protected long calledForHelpTime = 0;

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

    public enum PENALTYPE {
        NONE,
        HEALTH,
        EXPERIENCE,
        FOOD,
        ITEM
    }

    public enum SELFREVIVETYPE {
        CHANCE,
        RANDOM_ITEMS,
        SPECIFIC_ITEM,
        KILL,
        STATUS_EFFECTS,
        EXPERIENCE
    }

    protected List<SELFREVIVETYPE> selfReviveTypeList = new ArrayList<>(ReviveMeConfig.selfReviveOptions);
    protected List<ItemStack> sacrificialItems = new ArrayList<>();
    protected List<MobEffect> negativeStatusEffects = new ArrayList<>();
    protected int selfReviveCount = 0;
    protected boolean isDownedByPlayer = false;

    public static FallenData get(LivingEntity player) {
        FallenData cap = player.getData(AttachmentTypesInit.FALLEN_DATA);
        if (cap.level == null) cap.level = player.getCommandSenderWorld();
        if (cap.provider == null) cap.provider = cap.level.registryAccess();
        if (cap.damageSource == null) cap.damageSource = cap.level.damageSources().fellOutOfWorld();
        return cap;
    }

    public boolean canSelfRevive() {
        if (ReviveMeConfig.maxSelfRevives != -1 && getSelfReviveCount() >= ReviveMeConfig.maxSelfRevives) return false;
        return !ReviveMeConfig.disableSelfReviveIfPlayerDowned || !isDownedByPlayer;
    }

    public void callForHelp() {
        this.calledForHelpTime = this.level.getGameTime();
    }

    public boolean isCallingForHelp() {
        return this.level.getGameTime() < (this.calledForHelpTime + (ReviveMeConfig.reviveHelpDuration * 20));
    }

    public double callForHelpCooldown() {
        long timePassed = this.level.getGameTime() - this.calledForHelpTime;
        return Math.min(timePassed / (ReviveMeConfig.reviveHelpCooldown * 20), 1);
    }

    public float getPenaltyAmount(Player player) {
        Double actualAmount = ReviveMeConfig.penaltyAmount;
        if (actualAmount > 0 && actualAmount < 1) {
            actualAmount *= countReviverPenaltyAmount(player);
        }
        if (player.isCreative()) actualAmount = 0D;

        return Math.round(actualAmount);
    }

    public void kill(Player player) {
        player.playSound(SoundEvents.PLAYER_DEATH, 1, (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * 0.2F + 1.0F);
        player.setHealth(0);
        player.getCombatTracker().recordDamage(this.getDamageSource(), 1);
        player.die(this.damageSource);
    }

    public double countReviverPenaltyAmount(Player reviver) {
        switch (ReviveMeConfig.penaltyType) {
            case NONE:
                return 0;
            case HEALTH:
                return reviver.getHealth() + reviver.getAbsorptionAmount();
            case EXPERIENCE:
                return reviver.experienceLevel;
            case FOOD:
                return (reviver.getFoodData().getFoodLevel() + Math.max(reviver.getFoodData().getSaturationLevel(), 0));
            case ITEM: {
                ItemStack penaltyStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ReviveMeConfig.penaltyItem)));
                String n = (String) DataComponentPatch.CODEC.encode(penaltyStack.getComponentsPatch(), JavaOps.INSTANCE, "").getOrThrow();
                LOGGER.warn("What's the item component as a string? " + n);
//                CompoundTag tag = new CompoundTag();
//                DataComponentPatch.CODEC.encodeStart(this.provider.createSerializationContext(NbtOps.INSTANCE), penaltyStack.getComponentsPatch());
//                LOGGER.warn("What's the tag? " + tag);
                int count = 0;
                for (int a = 0; a < reviver.getInventory().getContainerSize(); a++) {
                    ItemStack containerStack = reviver.getInventory().getItem(a);
                    if (!ItemStack.isSameItem(containerStack, penaltyStack)) continue;
                    if (!ItemStack.isSameItemSameComponents(containerStack, penaltyStack)) continue;
                    count += containerStack.getCount();
                }
                return count;
            }
        }
        return 0;
    }

    public boolean hasEnough(Player reviver) {
        if (reviver.isCreative()) return true;
        if (ReviveMeConfig.penaltyType == PENALTYPE.NONE) return true;
        return countReviverPenaltyAmount(reviver) - this.getPenaltyAmount(reviver) >= 0;
    }

    public void setDamageSource(DamageSource damageSource) {
        this.damageSource = damageSource;
        this.isDownedByPlayer = (this.damageSource.getEntity() instanceof Player);
    }

    public DamageSource getDamageSource() {
        return damageSource;
    }

    public float GetTimeLeft(boolean divideByMax) {
        double maxSeconds = getPenaltyTicks(fellEnd);
        if (ReviveMeConfig.timeLeft == 0) maxSeconds = 0;
        getKillTime();

        if (divideByMax)
            return (float) (1 - ((level.getGameTime() - fellStart) / maxSeconds));

        return (float) (((fellStart + maxSeconds) - level.getGameTime()) / 20);
    }

    public float getKillTime() {
        if (ReviveMeConfig.pvpTimer == -1) return -1;
        float maxSeconds = getPenaltyTicks(ReviveMeConfig.pvpTimer * 20);

        return Math.max(0, ((fellStart + maxSeconds) - level.getGameTime()) / 20f);
    }

    public boolean shouldDie() {
        return ReviveMeConfig.timeLeft != 0 && GetTimeLeft(false) <= 0;
    }

    public void SetTimeLeft(long timeStart, double maxSeconds) {
        this.fellStart = timeStart;
        this.fellEnd = (long) (maxSeconds * 20);
        //System.out.println("Time left is!: " + (a/20f));
    }

    public void resumeFallTimer() {
        fellStart = (level.getGameTime() - (revStart - fellStart));
    }

    public boolean isFallen() {
        return isFallen;
    }

    public void setFallen(boolean fallen) {
        this.isFallen = fallen;

        if (!fallen) {
            setProgress(0, 1);
            SetTimeLeft(0, 1);
            setOtherPlayer(null);
        }
    }

    public boolean isDownedByPlayer() {
        return isDownedByPlayer;
    }

    public UUID getOtherPlayer() {
        return otherPlayer;
    }

    public boolean isReviver(UUID targUUID) {
        if (targUUID == null) return false;

        if (getOtherPlayer() == null) return false;

        return getOtherPlayer().equals(targUUID);
    }

    public void setOtherPlayer(UUID playerID) {
        otherPlayer = playerID;
    }

    public void setProgress(long timeStart, int seconds) {
        this.revStart = timeStart;
        this.revEnd = seconds * 20;
    }

    public float getProgress() {
        return Math.min(1, (level.getGameTime() - revStart) / (float) revEnd);
    }

    public SELFREVIVETYPE getSelfReviveOption(int mouseButton) {
        return this.selfReviveTypeList.get(mouseButton);
    }

    public void useReviveOption(SELFREVIVETYPE selectedOption, Player player) {
        Inventory playerInv = player.getInventory();

        double penaltyPercentage = this.getSelfPenaltyPercentage();
        this.selfReviveCount++;
        this.selfReviveTypeList.remove(selectedOption);
        this.selfReviveTypeList.add(selectedOption);
        LOGGER.warn("Penalty percentage: " + (penaltyPercentage));

        switch (selectedOption) {
            case CHANCE: {
                if (player.level().random.nextFloat() < (ReviveMeConfig.reviveChance * (1 - penaltyPercentage))) {
                    FallenTimerEvent.revivePlayer(player, false);
                    return;
                }
                break;
            }
            case RANDOM_ITEMS: {
                if (this.getItemList().isEmpty()) break;
                FallenTimerEvent.revivePlayer(player, false);
                for (ItemStack sacrificeStack : this.getItemList()) {
                    int count = FallenData.countItem(playerInv, sacrificeStack);
                    int amountToLose = (int) Math.round(Math.max(1, count *
                            (ReviveMeConfig.sacrificialItemPercent * (1 + penaltyPercentage))));

                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        ItemStack containerStack = playerInv.getItem(a);
                        if (!ItemStack.isSameItem(sacrificeStack, containerStack)) continue;
                        if (!ItemStack.isSameItemSameComponents(sacrificeStack, containerStack)) continue;
                        int takeAway = (Math.min(amountToLose, containerStack.getCount()));
                        amountToLose -= takeAway;
                        containerStack.setCount(containerStack.getCount() - takeAway);
                        if (amountToLose == 0) break;
                    }
                }
                this.sacrificialItems.clear();
                return;
            }
            case SPECIFIC_ITEM: {
                Pair<Integer, List<ItemStack>> specificPair = this.getSpecificItem(player);
                if (specificPair.getKey() >= ReviveMeConfig.specificItemCount) {
                    FallenTimerEvent.revivePlayer(player, false);
                    int amountLeft = specificPair.getKey();
                    ItemStack defaultStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ReviveMeConfig.specificItem)));
                    try {
                        defaultStack.applyComponents(DataComponentPatch.CODEC.decode(JavaOps.INSTANCE, ReviveMeConfig.specificItemData).getOrThrow().getFirst());
                    } catch (Exception e) {
                        LOGGER.warn(e.getMessage());
                    }
                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        ItemStack containerStack = playerInv.getItem(a);
                        if (!ItemStack.isSameItem(defaultStack, containerStack)) continue;
                        if (!ItemStack.isSameItemSameComponents(defaultStack, containerStack)) continue;
                        int takeAway = (Math.min(amountLeft, containerStack.getCount()));
                        amountLeft -= takeAway;
                        containerStack.setCount(containerStack.getCount() - takeAway);
                        if (amountLeft == 0) break;
                    }
                    return;
                }
                break;
            }
            case KILL: {
                FallenTimerEvent.revivePlayer(player, false);
                int seconds = (int) (ReviveMeConfig.reviveKillTime * 20 * (1 - penaltyPercentage));
                int killCount = ReviveMeConfig.reviveKillAmount;
                player.addEffect(new MobEffectInstance(MobEffectInit.KILL_REVIVE_EFFECT, seconds, killCount - 1));
                return;
            }
            case STATUS_EFFECTS: {
                double invulnTime = ReviveMeConfig.reviveInvulnTime;
                if (ReviveMeConfig.disableReviveEffects) ReviveMeConfig.reviveInvulnTime = 0D;
                FallenTimerEvent.revivePlayer(player, false);
                ReviveMeConfig.reviveInvulnTime = invulnTime;

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
            case EXPERIENCE: {
                if (player.experienceLevel < ReviveMeConfig.minReviveXPLevel) break;
                FallenTimerEvent.revivePlayer(player, false);
                player.giveExperienceLevels((int) -(player.experienceLevel * ReviveMeConfig.reviveXPLossPercentage
                        * (1 + penaltyPercentage)));
                return;
            }
        }

        this.kill(player);
    }

    public void resetSelfReviveCount() {
        this.selfReviveCount = 0;
    }

    public void refreshSelfReviveTypes(Player player) {
        if (!this.selfReviveTypeList.containsAll(ReviveMeConfig.selfReviveOptions) ||
                this.selfReviveTypeList.size() != ReviveMeConfig.selfReviveOptions.size())
            this.selfReviveTypeList = new ArrayList<>(ReviveMeConfig.selfReviveOptions);

        if (ReviveMeConfig.randomizeSelfReviveOptions) Collections.shuffle(this.selfReviveTypeList);

        if (this.selfReviveTypeList.contains(SELFREVIVETYPE.RANDOM_ITEMS)) {
            setSacrificialItems(player.getInventory());
        }
        if (this.selfReviveTypeList.contains(SELFREVIVETYPE.STATUS_EFFECTS)) {
            List<MobEffect> negativeEffects = BuiltInRegistries.MOB_EFFECT.entrySet().stream().filter((effect) ->
                            (effect.getValue().getCategory() == MobEffectCategory.HARMFUL) &&
                                    (!ReviveMeConfig.harmfulEffectsBlackList.contains(effect.getKey().location().toString())))
                    .map(Map.Entry::getValue).toList();

//            negativeEffects.forEach(e -> LOGGER.warn(e.getRegistryName().toString()));
            this.negativeStatusEffects.clear();
            this.negativeStatusEffects.addAll(CommonUtil.pickRandomObjectsFromList(Math.random() > 0.5F ? 1 : 2, negativeEffects));
        }

        int count = 0;
        if (ReviveMeConfig.onlyUseAvailableOptions) {
            for (int a = 0; a < this.selfReviveTypeList.size(); a++) {
                if (count == 2) break;

                switch (this.selfReviveTypeList.get(a)) {
                    case RANDOM_ITEMS:
                        if (getItemList().isEmpty()) continue;
                        break;
                    case SPECIFIC_ITEM:
                        if (getSpecificItem(player).getKey() == 0) continue;
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

    public Pair<Integer, List<ItemStack>> getSpecificItem(Player player) {
        ItemStack defaultStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ReviveMeConfig.specificItem)));
        try {
            defaultStack.applyComponents(DataComponentPatch.CODEC.decode(JavaOps.INSTANCE, ReviveMeConfig.specificItemData).getOrThrow().getFirst());
        } catch (Exception e) {
            LOGGER.warn(e.getMessage());
        }
        Inventory playerInv = player.getInventory();

        List<ItemStack> stackList = new ArrayList<>();
        int countNeeded = ReviveMeConfig.specificItemCount;
        int count = 0;

        for (int a = 0; a < playerInv.getContainerSize(); a++) {
            ItemStack containerStack = playerInv.getItem(a);
            if (!ItemStack.isSameItem(containerStack, defaultStack)) continue;
            if (!ItemStack.isSameItemSameComponents(containerStack, defaultStack)) continue;
            stackList.add(containerStack);
            count += containerStack.getCount();
            if (count >= countNeeded) break;
        }
        if (count == 0) {
            stackList.add(defaultStack);
        }
        count = Math.min(count, countNeeded);

        return Pair.of(count, stackList);
    }

    public List<MobEffect> getNegativeStatusEffects() {
        return new ArrayList<>(this.negativeStatusEffects);
    }

    public double getSelfPenaltyPercentage() {
        return this.getSelfReviveCount() * ReviveMeConfig.selfPenaltyPercentage;
    }

    //This is how many self revive options have been used since last refresh
    public int getSelfReviveCount() {
        return this.selfReviveCount;
    }

    public void setSacrificialItems(Inventory inventory) {
        if (inventory == null) return;
        Pair<Integer, List<ItemStack>> specificPair = getSpecificItem(inventory.player);


        //Generate a sacrificial item list
        ArrayList<ItemStack> playerItems = new ArrayList<>();
        for (ItemStack newStack : inventory.items) {
            if (!newStack.isStackable()) continue;
            if (specificPair.getValue().contains(newStack)) continue;
            if (playerItems.stream().anyMatch(listStack ->
                    ItemStack.isSameItem(newStack, listStack) && ItemStack.isSameItemSameComponents(newStack, listStack)))
                continue;
            if (newStack.isEmpty()) continue;
            playerItems.add(this.level.random.nextInt(Math.max(1, playerItems.size())), newStack);
        }
        //Remove all except 4
        while (playerItems.size() > 4) {
            playerItems.remove(this.level.random.nextInt(playerItems.size()));
        }

        this.sacrificialItems = playerItems;
    }

    public ArrayList<ItemStack> getItemList() {
        return new ArrayList<>(this.sacrificialItems);
    }

    public boolean isSacrificialItem(ItemStack mainStack) {
        if (!canSelfRevive()) return false;
        return this.sacrificialItems.stream().anyMatch(
                sacrificialStack -> ItemStack.isSameItem(mainStack, sacrificialStack) &&
                        ItemStack.isSameItemSameComponents(mainStack, sacrificialStack));
    }

    public static int countItem(Inventory inventory, ItemStack sacrificialStack) {
        int count = 0;

        for (int a = 0; a < inventory.getContainerSize(); a++) {
            ItemStack containerStack = inventory.getItem(a);
            if (!ItemStack.isSameItem(sacrificialStack, containerStack)) continue;
            if (!ItemStack.isSameItemSameComponents(sacrificialStack, containerStack)) continue;
            count += containerStack.getCount();
        }
        return count;
    }

    public int getPenaltyMultiplier() {
        return this.penaltyMultiplier;
    }

    public long getPenaltyTicks(double ticks) {
        double multiplier = getPenaltyMultiplier() * ReviveMeConfig.timeReductionPenalty;
        if (ReviveMeConfig.timeReductionPenalty == -1) multiplier = getPenaltyMultiplier() * ticks;
        else if (ReviveMeConfig.timeReductionPenalty < 1) multiplier *= ticks;
        else if (ReviveMeConfig.timeReductionPenalty >= 1) multiplier *= 20F;

        return (long) Math.max(0, ticks - multiplier);
    }

    public void setPenaltyMultiplier(int newMultiplier) {
        this.penaltyMultiplier = newMultiplier;
    }

    public void saveEffects(Player player) {
        CompoundTag effectsTag = new CompoundTag();
        for (MobEffectInstance effectInstance : player.getActiveEffects()) {
            if (effectInstance.getDuration() <= 20) continue;
            Tag savedEffectTag = effectInstance.save();
            effectsTag.put(effectsTag.size() + "", savedEffectTag);
        }
        this.savedEffectsTag = effectsTag;
    }

    public void loadEffects(Player player) {
        for (String key : this.savedEffectsTag.getAllKeys()) {
            MobEffectInstance instance = MobEffectInstance.load(this.savedEffectsTag.getCompound(key));
            if (instance == null) continue;
            player.addEffect(instance);
        }
        this.savedEffectsTag = new CompoundTag();
    }

    public CompoundTag writeNBT() {
        CompoundTag cNBT = new CompoundTag();
        cNBT.putLong(FELL_START_LONG, this.fellStart);
        cNBT.putDouble(FELL_END_DOUBLE, this.fellEnd / 20);
        cNBT.putBoolean(FALLEN_BOOL, this.isFallen);
        cNBT.putLong(REVIVE_START_LONG, this.revStart);
        cNBT.putInt(REVIVE_END_INT, this.revEnd / 20);

        //Save Self revive option list
        String selfReviveListString = "";
        for (SELFREVIVETYPE selfrevivetype : this.selfReviveTypeList) {
            selfReviveListString = selfReviveListString.concat(selfrevivetype.name() + ",");
        }
        cNBT.putString(SELF_REVIVE_OPTIONS_STRING, selfReviveListString);

        //Save Self revive status effects
        CompoundTag statusEffectNBT = new CompoundTag();
        int count = 0;
        ItemStack s = new ItemStack(Items.GOLD_BLOCK);
        ItemStack.CODEC.encodeStart(NbtOps.INSTANCE, s).getOrThrow();
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
        if (this.otherPlayer != null)
            cNBT.putUUID(OTHERPLAYER_UUID, this.otherPlayer);

        cNBT.putInt(SELF_REVIVE_COUNT_INT, this.selfReviveCount);

        cNBT.putBoolean(DOWNED_BY_PLAYER_BOOL, this.isDownedByPlayer());

        return cNBT;
    }

    public void readNBT(Tag nbt) {
        CompoundTag cNBT = (CompoundTag) nbt;
        this.SetTimeLeft(cNBT.getLong(FELL_START_LONG), cNBT.getDouble(FELL_END_DOUBLE));
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

        if (cNBT.hasUUID(OTHERPLAYER_UUID)) {
            this.setOtherPlayer(cNBT.getUUID(OTHERPLAYER_UUID));
        } else {
            this.setOtherPlayer(null);
        }

        this.selfReviveCount = cNBT.getInt(SELF_REVIVE_COUNT_INT);

        this.isDownedByPlayer = cNBT.getBoolean(DOWNED_BY_PLAYER_BOOL);
    }
}
