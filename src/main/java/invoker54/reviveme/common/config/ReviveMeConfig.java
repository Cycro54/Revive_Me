package invoker54.reviveme.common.config;


import com.google.common.collect.ImmutableList;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.data.ReviveConfigData;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public final class ReviveMeConfig {
    public static final CommonConfig COMMON;
    public static final ModConfigSpec COMMON_SPEC;
    public static final AtomicBoolean debugMode = new AtomicBoolean(true);
    public static final ModLogger LOGGER = ModLogger.getLogger(ReviveMeConfig.class, ReviveMeConfig.debugMode);

    public static boolean reviveMeEnabled;
    public static ReviveItemData.USER itemUser;
    public static Integer timeLeft;
    public static Boolean dieWhenTimerEnds;
    public static boolean pauseFallenTimerOnDisconnect;
    public static Integer reviveTime;
    public static boolean resetReviveOnHit;
    public static boolean reviverMustLook;
    public static Double revivedHealth;
    public static Double revivedFood;
    public static FallenData.PENALTYPE penaltyType;
    public static Double penaltyAmount;
    public static List<MobEffectInstance> reviveEffects = new ArrayList<>();
    //region self-revive shtuff
    public static List<FallenData.SELFREVIVETYPE> selfReviveOptions = new ArrayList<>();
    public static Integer maxTotalRevives;
    public static Integer maxPlayerRevives;
    public static Integer maxSelfRevives;
    public static boolean disableSelfReviveIfPlayerDowned;
    public static boolean randomizeSelfReviveOptions;
    public static boolean onlyUseAvailableOptions;
    public static Double selfPenaltyPercentage;
    public static Boolean refreshItems;
    public static Double reviveChance;
    public static boolean reviveChanceKillOnFail;
    public static Double sacrificialItemPercent;
    public static boolean sacrificialItemTakesHotbar;
    public static Integer reviveKillAmount;
    public static Integer reviveKillTime;
    public static List<? extends String> reviveKillBlackList = new ArrayList<>();
    public static boolean hideReviveKillEffect;
    public static List<MobEffect> harmfulEffects = new ArrayList<>();
    public static Integer negativeEffectsTime;
    public static boolean disableReviveEffects;
    public static boolean hideNegativeEffects;
    public static double reviveXPLossPercentage;
    public static Integer minReviveXPLevel;
    //endregion
    public static Double fallenPenaltyTimer;
    public static boolean canRemovePenaltyTimer;
    public static boolean hidePenaltyTimerEffect;
    public static Integer reviveRadius;
    public static boolean runDeathEventFirst;
    public static double fallenHealth;
    public static List<String> damageSourceWhitelist;
    public static boolean canPickUpItems;
    public static boolean canGiveUp;
    public static Double overhealAmount;
    public static Double overhealPenaltyPercentage;
    public static boolean dieOnDisconnect;
    public static Double reviveHelpDuration;
    public static Double reviveGlowMaxDistance;
    public static Double deathTimerMaxDistance;

    public enum FALLEN_POSE {
        CROUCH,
        PRONE,
        SLEEP
    }

    public static FALLEN_POSE fallenPose;

    public enum FALLEN_PERSPECTIVE{
        DEFAULT,
        THIRD_PERSON,
        R_THIRD_PERSON,
        FIRST_PERSON
    }
    public static FALLEN_PERSPECTIVE fallenPerspective;

    public enum JUMP {
        YES,
        LIQUID_ONLY,
        NO
    }

    public static JUMP canJump;
    public static boolean canMove;
    public static INTERACT_WITH_INVENTORY interactWithInventory;

    public enum INTERACT_WITH_INVENTORY {
        NO,
        LOOK_ONLY,
        YES
    }

    public static double timeReductionPenalty;
    public static Integer pvpTimer;
    public static boolean revertEffectsOnRevive;
    public static List<? extends String> revertEffectBlacklist;
    public static List<String> downedEffects;
    public static List<String> blockedCommands;
    public static List<String> allowedKeybinds;
    public static boolean silenceRegularMessages;
    public static boolean silenceCommandMessages;
    public static boolean universalChatMessages;
    //Client settings
    public static Boolean compactReviveUI = false;
    public static Boolean useBoldText = true;
    public static Boolean showItemPage = true;
    public static Boolean showToolTip = true;
    public static Double soundLevel = 0D;

    public static ReviveConfigData configReviveData;

    private static boolean isDirty = false;

    static {
        final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static void bakeConfig() {
        itemUser = COMMON.itemUser.get();
        timeLeft = COMMON.timeLeft.get();
        dieWhenTimerEnds = COMMON.dieWhenTimerEnds.get();
        pauseFallenTimerOnDisconnect = COMMON.pauseFallenTimerOnDisconnect.get();
        reviveTime = COMMON.reviveTime.get();
        resetReviveOnHit = COMMON.resetReviveOnHit.get();
        reviverMustLook = COMMON.reviverMustLook.get();
        revivedHealth = COMMON.revivedHealth.get();
        revivedFood = COMMON.revivedFood.get();
        penaltyType = COMMON.penaltyType.get();
        penaltyAmount = COMMON.penaltyAmount.get();
        reviveEffects.clear();
        for (String s : COMMON.reviveEffects.get()){
            reviveEffects.add(ReviveConfigData.EffectFromString(s));
        }

        selfReviveOptions = COMMON.selfReviveOptions.get().stream().map(FallenData.SELFREVIVETYPE::valueOf).collect(Collectors.toList());
//        if (selfReviveOptions.isEmpty()) selfReviveOptions.add(SELFREVIVETYPE.CHANCE);
        if (selfReviveOptions.size() == 1) selfReviveOptions.add(selfReviveOptions.get(0));

        bakeMaxReviveStuff();

        disableSelfReviveIfPlayerDowned = COMMON.disableSelfReviveIfPlayerDowned.get();
        randomizeSelfReviveOptions = COMMON.randomizeSelfReviveOptions.get();
        onlyUseAvailableOptions = COMMON.onlyUseAvailableOptions.get();
        selfPenaltyPercentage = COMMON.selfPenaltyPercentage.get();
        refreshItems = COMMON.refreshItems.get();
        reviveChance = COMMON.reviveChance.get();
        reviveChanceKillOnFail = COMMON.reviveChanceKillOnFail.get();
        sacrificialItemPercent = COMMON.sacrificialItemPercent.get();
        sacrificialItemTakesHotbar = COMMON.sacrificialItemTakesHotbar.get();
        reviveKillAmount = COMMON.reviveKillAmount.get();
        reviveKillTime = COMMON.reviveKillTime.get();

        reviveKillBlackList = COMMON.reviveKillBlackList.get();

        List<String> harmfulEffectsBlackList = (List<String>) COMMON.harmfulEffectsBlackList.get();
        Stream<Map.Entry<ResourceKey<MobEffect>, MobEffect>> negativeEffects = BuiltInRegistries.MOB_EFFECT.entrySet().stream().filter(
                e -> e.getValue().getCategory() == MobEffectCategory.HARMFUL);
        if (harmfulEffectsBlackList.contains("//")){
            harmfulEffects = negativeEffects.filter(entry ->
                    harmfulEffectsBlackList.stream()
                            .anyMatch(listString -> {
                                boolean isEmpty = listString.isEmpty();
                                if (isEmpty) return false;
                                String entryString = entry.getKey().identifier().toString();
                                return entryString.contains(listString);
                            })).map(Map.Entry::getValue).collect(Collectors.toList());
        }
        else {
            harmfulEffects = negativeEffects.filter(entry ->
                    harmfulEffectsBlackList.stream()
                            .noneMatch(listString -> {
                                boolean isEmpty = listString.isEmpty();
                                if (isEmpty) return false;
                                String entryString = entry.getKey().identifier().toString();
                                return entryString.contains(listString);
                            })).map(Map.Entry::getValue).collect(Collectors.toList());
        }

        hideReviveKillEffect = COMMON.hideReviveKillEffect.get();

        negativeEffectsTime = COMMON.negativeEffectsTime.get();
        disableReviveEffects = COMMON.disableReviveEffects.get();
        hideNegativeEffects = COMMON.hideNegativeEffects.get();
        reviveXPLossPercentage = COMMON.reviveXPLossPercentage.get();
        minReviveXPLevel = COMMON.minReviveXPLevel.get();
        fallenPenaltyTimer = COMMON.fallenPenaltyTimer.get();
        canRemovePenaltyTimer = COMMON.canRemovePenaltyTimer.get();
        hidePenaltyTimerEffect = COMMON.hidePenaltyTimerEffect.get();
        reviveRadius = COMMON.reviveRadius.get();
        runDeathEventFirst = COMMON.runDeathEventFirst.get();
        fallenHealth = COMMON.fallenHealth.get();
        damageSourceWhitelist = (List<String>) COMMON.damageSourceWhitelist.get();
        canPickUpItems = COMMON.canPickUpItems.get();
        canGiveUp = COMMON.canGiveUp.get();
        overhealAmount = COMMON.overhealAmount.get();
        overhealPenaltyPercentage = COMMON.overhealPenaltyPercentage.get();
        dieOnDisconnect = COMMON.dieOnDisconnect.get();
        reviveHelpDuration = COMMON.reviveHelpDuration.get();
        reviveGlowMaxDistance = COMMON.reviveGlowMaxDistance.get();
        deathTimerMaxDistance = COMMON.deathTimerMaxDistance.get();
        fallenPose = COMMON.fallenPose.get();
        fallenPerspective = COMMON.fallenPerspective.get();
        canJump = COMMON.canJump.get();
        canMove = COMMON.canMove.get();
        interactWithInventory = COMMON.interactWithInventory.get();
        timeReductionPenalty = COMMON.timeReductionPenalty.get();
        pvpTimer = COMMON.pvpTimer.get();
        revertEffectsOnRevive = COMMON.revertEffectsOnRevive.get();
        revertEffectBlacklist = COMMON.revertEffectBlacklist.get();
        downedEffects = (List<String>) COMMON.downedEffects.get();
        blockedCommands = (List<String>) COMMON.blockedCommands.get();
        allowedKeybinds = (List<String>) COMMON.allowedKeybinds.get();
        compactReviveUI = COMMON.compactReviveUI.get();
        useBoldText = COMMON.useBoldText.get();
        showItemPage = COMMON.showItemPage.get();
        showToolTip = COMMON.showToolTip.get();
        soundLevel = COMMON.soundLevel.get();
        silenceRegularMessages = COMMON.silenceRegularMessages.get();
        silenceCommandMessages = COMMON.silenceCommandMessages.get();
        universalChatMessages = COMMON.universalChatMessages.get();
        debugMode.set(COMMON.debugMode.get());
        reviveMeEnabled = COMMON.reviveMeEnabled.get();

        //Revive data stuff
        configReviveData = new ReviveConfigData(revivedHealth, revivedFood, fallenPenaltyTimer, reviveEffects);
        ReviveItemData.deserializeNBT(ReviveItemData.getNBTFromFile());
    }

    public static void bakeMaxReviveStuff() {
        maxTotalRevives = COMMON.maxTotalRevives.get();
        maxPlayerRevives = COMMON.maxPlayerRevives.get();
        maxSelfRevives = COMMON.maxSelfRevives.get();
        boolean oneIsUnlimited = maxSelfRevives == -1 || maxPlayerRevives == -1;
        if (maxTotalRevives != -1 && !oneIsUnlimited){
            maxTotalRevives = Math.min(maxTotalRevives, (maxSelfRevives + maxPlayerRevives));
        }
        else if (maxTotalRevives == -1 && !oneIsUnlimited)
            maxTotalRevives = (maxPlayerRevives + maxSelfRevives);
        if (maxTotalRevives != -1) {
            maxPlayerRevives = maxPlayerRevives == -1 ? maxTotalRevives : Math.min(maxPlayerRevives, maxTotalRevives);
            maxSelfRevives = maxSelfRevives == -1 ? maxTotalRevives : Math.min(maxSelfRevives, maxTotalRevives);
//            LOGGER.error("Player Revive: " + maxPlayerRevives);
//            LOGGER.error("Self Revive: " + maxSelfRevives);
        }
//        LOGGER.error("Luckily this is running btw...");
    }

    public static CompoundTag serialize() {
        CompoundTag mainTag = new CompoundTag();
        //Item User
        mainTag.putString("itemUser", itemUser.toString());
        //Time Left
        mainTag.putInt("timeLeft", timeLeft);
        //Reviver Must Look
        mainTag.putBoolean("reviverMustLook", reviverMustLook);
        //Penalty Type
        mainTag.putString("penaltyType", penaltyType.name());
        //Penalty Amount
        mainTag.putDouble("penaltyAmount", penaltyAmount);
        //Self Revive options
        String allSelfReviveOptions = "";
        for (FallenData.SELFREVIVETYPE reviveType : selfReviveOptions) {
//            LOGGER.error("Whats the class thing?? " + reviveType.getClass());
            allSelfReviveOptions = allSelfReviveOptions.concat(reviveType.name() + ",");
        }
        mainTag.putString("selfReviveOptions", allSelfReviveOptions);
        //Max Total Revives
        mainTag.putInt("maxTotalRevives", maxTotalRevives);
        //Max Player Revives
        mainTag.putInt("maxPlayerRevives", maxPlayerRevives);
        //Max Self Revives
        mainTag.putInt("maxSelfRevives", maxSelfRevives);
        //Disable Self Revive If Player Downed
        mainTag.putBoolean("disableSelfReviveIfPlayerDowned", disableSelfReviveIfPlayerDowned);
        //Randomize Self Revive Options
        mainTag.putBoolean("randomizeSelfReviveOptions", randomizeSelfReviveOptions);
        //Self Revive Penalty Percentage
        mainTag.putDouble("selfPenaltyPercentage", selfPenaltyPercentage);
        //Refresh Items
        mainTag.putBoolean("refreshItems", refreshItems);
        //Revive Chance
        mainTag.putDouble("reviveChance", reviveChance);
        //Sacrificial Item Percentage
        mainTag.putDouble("sacrificialItemPercent", sacrificialItemPercent);
        //Revive Kill Amount
        mainTag.putInt("reviveKillAmount", reviveKillAmount);
        //Revive Kill Time
        mainTag.putInt("reviveKillTime", reviveKillTime);
        //Negative Effects duration
        mainTag.putInt("negativeEffectsTime", negativeEffectsTime);
        //Revive XP Loss Percentage
        mainTag.putDouble("reviveXPLossPercentage", reviveXPLossPercentage);
        //Minimum Revive XP Level
        mainTag.putInt("minReviveXPLevel", minReviveXPLevel);
        //Can Run Death Event
        mainTag.putBoolean("runDeathEventFirst", runDeathEventFirst);
        //Can Give Up
        mainTag.putBoolean("canGiveUp", canGiveUp);
//        //Overheal Amount
//        mainTag.putDouble("overhealAmount", overhealAmount);
//        //Overheal Penalty Percentage
//        mainTag.putDouble("overhealPenaltyPercentage", overhealPenaltyPercentage);
        //How long the help effects last for
        mainTag.putDouble("reviveHelpDuration", reviveHelpDuration);
        //How far a glowing fallen player can be seen
        mainTag.putDouble("reviveGlowMaxDistance", reviveGlowMaxDistance);
        //How far a fallen player's timer can be seen
        mainTag.putDouble("deathTimerMaxDistance", deathTimerMaxDistance);
        //Fallen pose
        mainTag.putString("fallenPose", fallenPose.toString());
        //Fallen perspective
        mainTag.putString("fallenPerspective", fallenPerspective.toString());
        //can jump
        mainTag.putString("canJump", canJump.toString());
        //can move
        mainTag.putBoolean("canMove", canMove);
        //open Inventory While Downed
        mainTag.putString("interactWithInventory", interactWithInventory.toString());
        //time Reduction Penalty
        mainTag.putDouble("timeReductionPenalty", timeReductionPenalty);
        //pvp Timer
        mainTag.putInt("pvpTimer", pvpTimer);
        //Blocked Commands
        String allBlockedCommands = "";
        for (String string : blockedCommands) {
            allBlockedCommands = allBlockedCommands.concat(string + ",");
        }
        mainTag.putString("blockedCommands", allBlockedCommands);
        //Allowed Keybinds
        String allAllowedKeybinds = "";
        for (String s : allowedKeybinds) {
            allAllowedKeybinds = allAllowedKeybinds.concat(s + ",");
        }
        mainTag.putString("allowedKeybinds", allAllowedKeybinds);

        mainTag.put(ReviveItemData.MAIN_REVIVE_ITEM_NBT, ReviveItemData.getNBTFromFile());
        return mainTag;
    }

    public static void deserialize(CompoundTag mainTag) {
        //Item User
        itemUser = ReviveItemData.USER.valueOf(mainTag.getString("itemUser").get());
        //Time Left
        timeLeft = mainTag.getInt("timeLeft").get();
        //Reviver Must Look
        reviverMustLook = mainTag.getBoolean("reviverMustLook").get();
        //Penalty Type
        penaltyType = FallenData.PENALTYPE.valueOf(mainTag.getString("penaltyType").get());
        //Penalty Amount
        penaltyAmount = mainTag.getDouble("penaltyAmount").get();
        //Self Revive options
        if (!mainTag.getString("selfReviveOptions").isEmpty()) {
            String[] allSelfReviveOptions = mainTag.getString("selfReviveOptions").get().split(",");
            selfReviveOptions.clear();
            for (String reviveType : allSelfReviveOptions) selfReviveOptions.add(FallenData.SELFREVIVETYPE.valueOf(reviveType));
        }
        //Max Total Revives
        maxTotalRevives = mainTag.getInt("maxTotalRevives").get();
        //Max Player Revives
        maxPlayerRevives = mainTag.getInt("maxPlayerRevives").get();
        //Max Self Revives
        maxSelfRevives = mainTag.getInt("maxSelfRevives").get();
        //Disable Self Revive If Player Downed
        disableSelfReviveIfPlayerDowned = mainTag.getBoolean("disableSelfReviveIfPlayerDowned").get();
        //Randomize Self Revive Options
        randomizeSelfReviveOptions = mainTag.getBoolean("randomizeSelfReviveOptions").get();
        //Self Revive Penalty Percentage
        selfPenaltyPercentage = mainTag.getDouble("selfPenaltyPercentage").get();
        //Refresh Items
        refreshItems = mainTag.getBoolean("refreshItems").get();
        //Revive Chance
        reviveChance = mainTag.getDouble("reviveChance").get();
        //Sacrificial Item Percentage
        sacrificialItemPercent = mainTag.getDouble("sacrificialItemPercent").get();
        //Revive Kill Amount
        reviveKillAmount = mainTag.getInt("reviveKillAmount").get();
        //Revive Kill Time
        reviveKillTime = mainTag.getInt("reviveKillTime").get();
        //Negative Effects duration
        negativeEffectsTime = mainTag.getInt("negativeEffectsTime").get();
        //Revive XP Loss Percentage
        reviveXPLossPercentage = mainTag.getDouble("reviveXPLossPercentage").get();
        //Minimum Revive XP Level
        minReviveXPLevel = mainTag.getInt("minReviveXPLevel").get();
        //Run Living Death Event first
        runDeathEventFirst = mainTag.getBoolean("runDeathEventFirst").get();
        //Can Give Up
        canGiveUp = mainTag.getBoolean("canGiveUp").get();
//        //Overheal Amount
//        overhealAmount = mainTag.getDouble("overhealAmount");
//        //Overheal Penalty Percentage
//        overhealPenaltyPercentage = mainTag.getDouble("overhealPenaltyPercentage");
        //How long the help effects last for
        reviveHelpDuration = mainTag.getDouble("reviveHelpDuration").get();
        //How far a glowing fallen player can be seen
        reviveGlowMaxDistance = mainTag.getDouble("reviveGlowMaxDistance").get();
        //How far a fallen player's timer can be seen
        deathTimerMaxDistance = mainTag.getDouble("deathTimerMaxDistance").get();
        //Fallen Pose
        fallenPose = FALLEN_POSE.valueOf(mainTag.getString("fallenPose").get());
        //Fallen Perspective
        fallenPerspective = FALLEN_PERSPECTIVE.valueOf(mainTag.getString("fallenPerspective").get());
        //Can Jump
        canJump = JUMP.valueOf(mainTag.getString("canJump").get());
        //can Move
        canMove = mainTag.getBoolean("canMove").get();
        //open Inventory While Downed
        interactWithInventory = INTERACT_WITH_INVENTORY.valueOf(mainTag.getString("interactWithInventory").get());
        //time Reduction Penalty
        timeReductionPenalty = mainTag.getDouble("timeReductionPenalty").get();
        //pvp Timer
        pvpTimer = mainTag.getInt("pvpTimer").get();
        //Blocked Commands
        blockedCommands = Arrays.asList(mainTag.getString("blockedCommands").get().split(","));
        //Allowed Keybinds
        allowedKeybinds = Arrays.asList(mainTag.getString("allowedKeybinds").get().split(","));

        ReviveItemData.deserializeNBT((CompoundTag) mainTag.get(ReviveItemData.MAIN_REVIVE_ITEM_NBT));
    }

    @SubscribeEvent
    public static void onConfigChanged(final ModConfigEvent eventConfig){
        //System.out.println("What's the config type? " + eventConfig.getConfig().getType());

        if (eventConfig.getConfig().getSpec() == ReviveMeConfig.COMMON_SPEC) {
            bakeConfig();
            markDirty(true);
        }
    }

    public static void markDirty(boolean dirty) {
        isDirty = dirty;
    }

    public static boolean isDirty() {
        return isDirty;
    }

    public static class CommonConfig {

        //This is how to make a config value
        //public static final ModConfigSpec.ConfigValue<Integer> exampleInt;
        public final ModConfigSpec.EnumValue<ReviveItemData.USER> itemUser;
        public final ModConfigSpec.ConfigValue<Integer> timeLeft;
        public final ModConfigSpec.ConfigValue<Boolean> dieWhenTimerEnds;
        public final ModConfigSpec.ConfigValue<Boolean> pauseFallenTimerOnDisconnect;
        public final ModConfigSpec.ConfigValue<Integer> reviveTime;
        public final ModConfigSpec.ConfigValue<Boolean> resetReviveOnHit;
        public final ModConfigSpec.ConfigValue<Boolean> reviverMustLook;
        public final ModConfigSpec.ConfigValue<Double> revivedHealth;
        public final ModConfigSpec.ConfigValue<Double> revivedFood;
        public final ModConfigSpec.EnumValue<FallenData.PENALTYPE> penaltyType;
        public final ModConfigSpec.ConfigValue<Double> penaltyAmount;
        public final ModConfigSpec.ConfigValue<List<? extends String>> reviveEffects;
        public final ModConfigSpec.ConfigValue<List<? extends String>> selfReviveOptions;
        public final ModConfigSpec.ConfigValue<Integer> maxTotalRevives;
        public final ModConfigSpec.ConfigValue<Integer> maxPlayerRevives;
        public final ModConfigSpec.ConfigValue<Integer> maxSelfRevives;
        public final ModConfigSpec.ConfigValue<Boolean> disableSelfReviveIfPlayerDowned;
        public final ModConfigSpec.ConfigValue<Boolean> randomizeSelfReviveOptions;
        public final ModConfigSpec.ConfigValue<Boolean> onlyUseAvailableOptions;
        public final ModConfigSpec.ConfigValue<Double> selfPenaltyPercentage;
        public final ModConfigSpec.ConfigValue<Boolean> refreshItems;
        public final ModConfigSpec.ConfigValue<Double> reviveChance;
        public final ModConfigSpec.ConfigValue<Boolean> reviveChanceKillOnFail;
        public final ModConfigSpec.ConfigValue<Double> sacrificialItemPercent;
        public final ModConfigSpec.ConfigValue<Boolean> sacrificialItemTakesHotbar;
        public final ModConfigSpec.ConfigValue<Integer> reviveKillAmount;
        public final ModConfigSpec.ConfigValue<Integer> reviveKillTime;
        public final ModConfigSpec.ConfigValue<List<? extends String>> reviveKillBlackList;
        public final ModConfigSpec.ConfigValue<Boolean> hideReviveKillEffect;
        public final ModConfigSpec.ConfigValue<List<? extends String>> harmfulEffectsBlackList;
        public final ModConfigSpec.ConfigValue<Integer> negativeEffectsTime;
        public final ModConfigSpec.ConfigValue<Boolean> disableReviveEffects;
        public final ModConfigSpec.ConfigValue<Boolean> hideNegativeEffects;
        public final ModConfigSpec.ConfigValue<Double> reviveXPLossPercentage;
        public final ModConfigSpec.ConfigValue<Integer> minReviveXPLevel;
        public final ModConfigSpec.ConfigValue<Double> fallenPenaltyTimer;
        public final ModConfigSpec.ConfigValue<Boolean> canRemovePenaltyTimer;
        public final ModConfigSpec.ConfigValue<Boolean> hidePenaltyTimerEffect;
        public final ModConfigSpec.ConfigValue<Integer> reviveRadius;
        public final ModConfigSpec.ConfigValue<Boolean> runDeathEventFirst;
        public final ModConfigSpec.ConfigValue<Double> fallenHealth;
        public final ModConfigSpec.ConfigValue<List<? extends String>> damageSourceWhitelist;
        public final ModConfigSpec.ConfigValue<Boolean> canPickUpItems;
        public final ModConfigSpec.ConfigValue<Boolean> canGiveUp;
        public final ModConfigSpec.ConfigValue<Double> overhealAmount;
        public final ModConfigSpec.ConfigValue<Double> overhealPenaltyPercentage;
        public final ModConfigSpec.ConfigValue<Boolean> dieOnDisconnect;
        public final ModConfigSpec.ConfigValue<Double> reviveHelpDuration;
        public final ModConfigSpec.ConfigValue<Double> reviveGlowMaxDistance;
        public final ModConfigSpec.ConfigValue<Double> deathTimerMaxDistance;
        public final ModConfigSpec.ConfigValue<FALLEN_POSE> fallenPose;
        public final ModConfigSpec.ConfigValue<FALLEN_PERSPECTIVE> fallenPerspective;
        public final ModConfigSpec.ConfigValue<JUMP> canJump;
        public final ModConfigSpec.ConfigValue<Boolean> canMove;
        public final ModConfigSpec.ConfigValue<INTERACT_WITH_INVENTORY> interactWithInventory;
        public final ModConfigSpec.ConfigValue<Double> timeReductionPenalty;
        public final ModConfigSpec.ConfigValue<Integer> pvpTimer;
        public final ModConfigSpec.ConfigValue<Boolean> revertEffectsOnRevive;
        public final ModConfigSpec.ConfigValue<List<? extends String>> revertEffectBlacklist;
        public final ModConfigSpec.ConfigValue<List<? extends String>> downedEffects;
        public final ModConfigSpec.ConfigValue<List<? extends String>> blockedCommands;
        public final ModConfigSpec.ConfigValue<List<? extends String>> allowedKeybinds;
        public final ModConfigSpec.ConfigValue<Boolean> compactReviveUI;
        public final ModConfigSpec.ConfigValue<Boolean> useBoldText;
        public final ModConfigSpec.ConfigValue<Boolean> showItemPage;
        public final ModConfigSpec.ConfigValue<Boolean> showToolTip;
        public final ModConfigSpec.ConfigValue<Double> soundLevel;
        public final ModConfigSpec.ConfigValue<Boolean> silenceRegularMessages;
        public final ModConfigSpec.ConfigValue<Boolean> silenceCommandMessages;
        public final ModConfigSpec.ConfigValue<Boolean> universalChatMessages;
        public final ModConfigSpec.ConfigValue<Boolean> debugMode;
        public final ModConfigSpec.ConfigValue<Boolean> reviveMeEnabled;

        public CommonConfig(ModConfigSpec.Builder builder) {
            itemUser = builder.comment("Who can use revive items").defineEnum("Item_User", ReviveItemData.USER.BOTH);

            builder.push("Self-Revive Settings");

            builder.push("General Settings");
            List<String> defaultSelfReviveList = new ArrayList<>(Arrays.asList(FallenData.SELFREVIVETYPE.RANDOM_ITEMS.name(), FallenData.SELFREVIVETYPE.CHANCE.name(), FallenData.SELFREVIVETYPE.KILL.name(), FallenData.SELFREVIVETYPE.STATUS_EFFECTS.name(), FallenData.SELFREVIVETYPE.EXPERIENCE.name()));
            selfReviveOptions = builder.comment("List of all your self-revive options EXCEPT revive items (Duplicate options are allowed, can be empty). Self-revive options refresh from this list when the revive penalty ends. OPTIONS: 'CHANCE', 'RANDOM_ITEMS', 'KILL', 'STATUS_EFFECTS', 'EXPERIENCE'")
                    .defineList("SELF_REVIVE_OPTIONS", new ArrayList<>(defaultSelfReviveList), defaultSelfReviveList::contains);
            disableSelfReviveIfPlayerDowned = builder.comment("If self-revive should be disabled if a player places you in the fallen state").define("Disable_Self_Revive_On_PVP", false);
            randomizeSelfReviveOptions = builder.comment("If the chosen self-revive options are picked randomly.").define("Randomize_Self_Revive_Options", true);
            onlyUseAvailableOptions = builder.comment("If the mod should only pick self-revive options that you would meet the requirements for (when possible)").define("Only_Use_Available_Options", false);
            selfPenaltyPercentage = builder.comment("Increases the cost of self-revival options each time you self-revive. Stacks additively. Resets to 0 on fallen penalty timer expiration.").defineInRange("Self_Penalty_Percentage", 0.25, 0, 1);
            refreshItems = builder.comment("If the revive item list should refresh continuously (if false it will refresh once when entering fallen state)").define("Refresh_Item_List", false);
            builder.pop();

            builder.push("Revive Type Settings");

            builder.push("Chance");
            reviveChance = builder.comment("How high your chance is to revive. (Affected by penalty)").defineInRange("Revive_Chance", 0.75F, 0F, 1F);
            reviveChanceKillOnFail = builder.comment("If you should die instantly if you fail the chance check. (Will only disable self revive if Can_Give_Up is false)").define("Kill_On_Fail", false);
            builder.pop();

            builder.push("Random Items");
            sacrificialItemPercent = builder.comment("Percentage to lose for sacrificial items. (Affected by penalty)").defineInRange("Item_Loss_Percentage", 0.25F, 0F, 1F);
            sacrificialItemTakesHotbar = builder.comment("Should check hotbar when sacrificing items").define("Include_Hotbar_Items", false);
            builder.pop();

            builder.push("Kill");
            reviveKillAmount = builder.comment("Amount of Living Entities you must kill before the death timer runs out.").defineInRange("Kill_Count", 2, 0, Integer.MAX_VALUE);
            reviveKillTime = builder.comment("Time in SECONDS you have to kill the needed amount of Living Entities. (Affected by penalty)").defineInRange("Kill_Time", 30, 0, Integer.MAX_VALUE);
            reviveKillBlackList = builder.comment("List of entities that won't count towards removing the death timer. Add \"//\" to make it a whitelist instead." +
                            " Add ';EntityClassification;' to block/allow mob categories. Usage: (ModId:EntityType) 'minecraft:zombie' or 'zombie' or 'minecraft'. Usage2: (EntityClassification) ';MONSTER;' or ';MISC;'")
                    .defineList("Revive_Kill_Blacklist", new ArrayList<>(), a -> !a.toString().isEmpty());
            hideReviveKillEffect = builder.comment("If the Kill Revive particles should be hidden").define("Hide_Kill_Revive_Effect", false);
            builder.pop();

            builder.push("Status Effects");
            harmfulEffectsBlackList = builder.comment("List of harmful effects that shouldn't be chosen. Add \"//\" to make it a whitelist instead. You can also use parts of an effect Usage: (ModId:PotionEffect) 'minecraft:slowness' or 'slowness' or 'minecraft'")
                    .defineList("Harmful_Effects_Blacklist", Arrays.asList("minecraft:bad_omen", "minecraft:unluck", "minecraft:instant_damage", "revive_me:kill_revive_effect"),
                            a -> !a.toString().isEmpty());

            negativeEffectsTime = builder.comment("How long the harmful effects will last for. (Affected by penalty)").defineInRange("Effect_Duration", 15, 0, Integer.MAX_VALUE);
            disableReviveEffects = builder.comment("If Revive effects should be disabled when reviving with this self-revive type.").define("Disable_Revive_Effects", true);
            hideNegativeEffects = builder.comment("If negative effect particles should be hidden").define("Hide_Negative_Effects", false);
            builder.pop();

            builder.push("EXPERIENCE");
            reviveXPLossPercentage = builder.comment("Percentage of XP you should lose to revive. (Affected by penalty)").defineInRange("XP_Loss_Percentage", 0.4F, 0, 1);
            minReviveXPLevel = builder.comment("The minimum amount of XP levels needed.").defineInRange("Minimum_XP_Level", 15, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.pop();
            builder.pop();

            builder.push("Fallen State Settings");
            builder.push("General Settings");
            builder.push("Health & Heal Settings");
            fallenHealth = builder.comment("Max Health you can have while in the fallen state, -1 is max health, Less than 1 is percentage").defineInRange("Fallen_Health", 2F, -1F, Integer.MAX_VALUE);
            overhealAmount = builder.comment("How much you have to be healed in order to be revived, 0 disables this feature, -1 is max health, Less than 1 is percentage").defineInRange("Overheal_Amount", 0, -1F, Integer.MAX_VALUE);
            overhealPenaltyPercentage = builder.comment("Increases overheal cost each time you are revived, stacks additively.").defineInRange("Overheal_Penalty_Percentage", 0.25F, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Interaction Settings");
            builder.push("Call For Help Settings");
            reviveHelpDuration = builder.comment("How long the Help call will last in SECONDS").defineInRange("Revive_Help_Duration", 10F, 1, Double.MAX_VALUE);
            reviveGlowMaxDistance = builder.comment("How far you can see glowing players").defineInRange("Revive_Glow_Max_Distance", 80, 0, Double.MAX_VALUE);
            deathTimerMaxDistance = builder.comment("How far you can see the death timer for a player in the fallen state.").defineInRange("Death_Timer_Max_Distance", 40, 0, Double.MAX_VALUE);
            builder.pop();

            canPickUpItems = builder.comment("If a fallen player can pick up items").define("Can_Pick_Up_Items", true);
            canGiveUp = builder.comment("If a fallen player can give up and die").define("Can_Give_Up", true);
            interactWithInventory = builder.comment("If a fallen player can use their inventory").defineEnum("Interact_With_Inventory", INTERACT_WITH_INVENTORY.LOOK_ONLY);
            blockedCommands = builder.comment("Commands a fallen player isn't allowed to use. Type \"/\" to block all commands. Type \"//\" to make this a whitelist.").define("Blocked_Commands", new ArrayList<>());
            allowedKeybinds = builder.comment("Keybinds that you can use while in the fallen state. (You can put a piece or the full name of a keybind. (Check the translation json for keybinding names (en_us.json for example))" +
                    "\nExample of binding: 'key.fullscreen' or 'fullscreen' will let you use the 'Toggle Fullscreen' keybinding while in the fallen state)").define("Allowed_Keybinds", new ArrayList<>());
            builder.pop();

            damageSourceWhitelist = builder.comment("List of damage sources that bypass fallen state invulnerability (or the entire mod). Type \"/\" to allow all damage sources. Type \"//\" to make it a blacklist instead (entries will trigger the fallen state). You can also use parts of a damage source, add ';' to make it match exactly (Case sensitive both ways), add '*' to make it bypass the entire mod (does the opposite if set to blacklist). Usage: (MessageID) 'inWall' or 'Wall' or ';inWall', or '*inWall' or ';*inWall'").define("Damage_Source_Whitelist", new ArrayList<>(ImmutableList.of(";outOfWorld")));

            maxTotalRevives = builder.comment("Max TOTAL revives (Setting to 0 will disable Revive Me!) (setting to -1 will disable the total-revive max) Refreshes when penalty timer ends.").defineInRange("Max_Total_revives", 6, -1, Integer.MAX_VALUE);
            maxPlayerRevives = builder.comment("Max PLAYER revives (setting to 0 will disable player-revive) (setting to -1 will disable the player-revive max) Refreshes when penalty timer ends.").defineInRange("Max_Player_revives", -1, -1, Integer.MAX_VALUE);
            maxSelfRevives = builder.comment("Max SELF revives (setting to 0 will disable self-revive) (setting to -1 will disable the self-revive max) Refreshes when penalty timer ends.").defineInRange("Max_Self_revives", 3, -1, Integer.MAX_VALUE);

            reviveRadius = builder.comment("Max distance another player can be to enter the fallen state (0 disables this)").defineInRange("Revive_Radius", 0,0,Integer.MAX_VALUE);
            runDeathEventFirst = builder.comment("If Forge's Death Event should run first before this mod does (if Death event runs first and player death is cancelled, Revive-Me code will not execute. Same thing vice-versa.)")
                    .define("Run_Death_Event_First", false);
            downedEffects = builder.comment("Potion effects the player has while fallen (ModId:PotionEffect:Amplification:HideEffect <-optional)(minecraft:slowness:0 or minecraft:blindness:0:true)").define("Downed_Effects", new ArrayList<String>(ImmutableList.of("minecraft:slowness:3:true")));
            dieOnDisconnect = builder.comment("If you should die instantly if you disconnect while in the fallen state").define("Die_On_Disconnect", false);
            builder.pop();

            builder.push("Timer Settings");
            timeLeft = builder.comment("How long you have before death. Setting to -1 will disable the timer").defineInRange("Time_Left", 60, -1, Integer.MAX_VALUE);
            dieWhenTimerEnds = builder.comment("If you should die when the death timer ends. If set to false, you will instead be targetable by mobs.").define("Die_When_Timer_Ends", false);
            pauseFallenTimerOnDisconnect = builder.comment("If the fallen timer should pause on disconnect (Multiplayer only)").define("Pause_Fallen_Timer_On_Disconnect", false);
            timeReductionPenalty = builder.comment("How much time (in seconds) your death timer loses each time you fall. (Less than 1 is a percentage of max death time, -1 will take away the max)").defineInRange("Time_Reduction_Penalty", 5, -1F, Double.MAX_VALUE);
            fallenPenaltyTimer = builder.comment("how long the revive penalty effects will last in SECONDS").defineInRange("Revive_Penalty_Timer", 45, 0F, Double.MAX_VALUE);
            canRemovePenaltyTimer = builder.comment("If you can remove the penalty timer before it ends").define("Can_Remove_Penalty_Timer", false);
            hidePenaltyTimerEffect = builder.comment("If penalty timer particles should be hidden").define("Hide_Penalty_Timer_Particles", true);
            pvpTimer = builder.comment("How much time (in seconds) must pass before you may be killed by other players. Affected by time reduction penalty. Setting to -1 will disable this").defineInRange("PVP_Timer", 10, -1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Movement & Camera Settings");
            fallenPose = builder.comment("What pose you have whilst fallen").defineEnum("Fallen_Pose", FALLEN_POSE.CROUCH);
            fallenPerspective = builder.comment("What perspective you have whilst fallen").defineEnum("Fallen_Perspective", FALLEN_PERSPECTIVE.DEFAULT);
            canJump = builder.comment("If the player may jump while fallen").defineEnum("Can_Jump", JUMP.YES);
            canMove = builder.comment("If the player may move while fallen").define("Can_Move", true);
            builder.pop();
            builder.pop();

            builder.push("Revive Settings");
            builder.push("Revivee Settings");
            reviverMustLook = builder.comment("If the reviver must look at the revivee to revive them").define("Reviver_Must_look", true);
            revivedHealth = builder.comment("How much health you will be revived with, -1 is max health, Less than 1 is percentage").defineInRange("Revive_Health", 10F, -1F, Integer.MAX_VALUE);
            revivedFood = builder.comment("How much food you will be revived with, -1 is max food, Less than 1 is percentage").defineInRange("Revive_Food", 6F, -1F, Integer.MAX_VALUE);
            reviveEffects = builder.comment("What effects you revive with (ModId:PotionEffect:Amplification:Ticks:HideEffect <-optional)(minecraft:fire_resistance:3:100 or minecraft:resistance:3:100:true)").defineList("Revive_Effects",
                    Arrays.asList("minecraft:fire_resistance:5:100:true", "minecraft:resistance:5:100:true"), s -> !s.toString().isEmpty() && (s.toString().split(":").length == 4 || s.toString().split(":").length == 5));
            revertEffectsOnRevive = builder.comment("Give back all of the potion effects the player had before entering the fallen state").define("Revert_Effects_On_Revive", false);
            revertEffectBlacklist = builder.comment("What effects you won't gain back on revive. Add \"//\" to make it a whitelist instead. Add ';MobEffectCategory;' to block/allow effect categories. " +
                            "Usage: (ModId:PotionEffect) 'minecraft:slowness' or 'slowness' or 'minecraft'. Usage2: (MobEffectCategory) ';HARMFUL;' or ';NEUTRAL;' or ';BENEFICIAL;'")
                    .defineList("Revert_Effect_Blacklist", new ArrayList<>(Arrays.asList(ReviveMe.MOD_ID)), a -> !a.toString().isEmpty());
            builder.pop();

            builder.push("Reviver Settings");
            reviveTime = builder.comment("How long to revive someone").defineInRange("Revive_Time", 3, 0, Integer.MAX_VALUE);
            penaltyType = builder.comment("What the reviver will lose (Reviver can use ITEM without it being selected. Selecting ITEM while Revive Items are disabled will disable this revive option.)").defineEnum("Penalty_Type", FallenData.PENALTYPE.FOOD);
            penaltyAmount = builder.comment("Amount that will be taken from reviver, Numbers below 1 and greater than 0 will turn it into a percentage").define("Penalty_Amount", 10D);
            resetReviveOnHit = builder.comment("If revive progress resets when damage is taken").define("Reset_Revive_On_Hit", true);
            builder.pop();
            builder.pop();

            builder.push("Chat Settings");
            silenceRegularMessages = builder.comment("Silence the revive and fallen chat messages").define("Silence_Regular_Messages", false);
            silenceCommandMessages = builder.comment("Silence command chat messages").define("Silence_Command_Messages", false);
            universalChatMessages = builder.comment("If chat messages from this mod should be sent to everyone. If false, will only send to those nearby").define("Universal_Chat_Messages", true);
            builder.pop();

            builder.push("Client Settings");
            compactReviveUI = builder.comment("Makes self-revive UI smaller, more compact").define("Compact_UI", false);
            useBoldText = builder.comment("If the UI text should use bold formatting").define("Bold_Text", true);
            showItemPage = builder.comment("If item info should be shown on Revive Item Screen").define("Show_Item_Page", true);
            showToolTip = builder.comment("If Revive items should have extended tooltips").define("Show_Tool_Tip", true);
            soundLevel = builder.comment("Controls how loud Revive Me! sounds are").defineInRange("Sound_Level", 0.7F, 0F, 1F);
            builder.pop();

            debugMode = builder.comment("If debug mode should be activated (just for testing and troubleshooting)").define("Debug_Mode", true);
            reviveMeEnabled = builder.comment("If Revive Me is enabled").define("Revive_Me_Enabled", true);
        }
    }
}