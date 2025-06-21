package invoker54.reviveme.common.config;


import com.google.common.collect.ImmutableList;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ReviveMeConfig {
    public static final CommonConfig COMMON;
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModLogger LOGGER = ModLogger.getLogger(ReviveMeConfig.class, ReviveMeConfig.debugMode);

    public static Integer timeLeft;
    public static Integer reviveTime;
    public static Double revivedHealth;
    public static Double revivedFood;
    public static FallenData.PENALTYPE penaltyType;
    public static Double penaltyAmount;
    public static String penaltyItem;
    public static String penaltyItemData;
    public static boolean cancelReviveOnDamage;
    public static Double reviveInvulnTime;
    //region self-revive shtuff
    public static List<FallenData.SELFREVIVETYPE> selfReviveOptions = new ArrayList<>();
    public static Integer maxSelfRevives;
    public static boolean disableSelfReviveIfPlayerDowned;
    public static boolean randomizeSelfReviveOptions;
    public static boolean onlyUseAvailableOptions;
    public static Double selfPenaltyPercentage;
    public static Double reviveChance;
    public static Double sacrificialItemPercent;
    public static String specificItem;
    public static Integer specificItemCount;
    public static String specificItemData;
    public static Integer reviveKillAmount;
    public static Integer reviveKillTime;
    public static List<String> harmfulEffectsBlackList = null;
    public static Integer negativeEffectsTime;
    public static boolean disableReviveEffects;
    public static double reviveXPLossPercentage;
    public static Integer minReviveXPLevel;
    //endregion
    public static Double fallenPenaltyTimer;
    public static boolean runDeathEventFirst;
    public static boolean canGiveUp;
    public static boolean dieOnDisconnect;
    public static Double reviveHelpCooldown;
    public static Double reviveHelpDuration;

    public enum FALLEN_POSE {
        CROUCH,
        PRONE,
        SLEEP
    }

    public static FALLEN_POSE fallenPose;

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
    public static List<String> downedEffects;
    public static List<String> blockedCommands;
    public static List<String> allowedKeybinds;
    public static boolean silenceRegularMessages;
    public static boolean silenceCommandMessages;
    public static boolean universalChatMessages;
    public static final AtomicBoolean debugMode = new AtomicBoolean(false);
    //Client settings
    public static Boolean compactReviveUI = false;
    public static Double soundLevel = 0D;


    private static boolean isDirty = false;

    static {
        final Pair<CommonConfig, ModConfigSpec> specPair = new ModConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static void bakeConfig() {
        timeLeft = COMMON.timeLeft.get();
        reviveTime = COMMON.reviveTime.get();
        revivedHealth = COMMON.revivedHealth.get();
        revivedFood = COMMON.revivedFood.get();
        penaltyType = COMMON.penaltyType.get();
        penaltyAmount = COMMON.penaltyAmount.get();
        penaltyItem = COMMON.penaltyItem.get();
        penaltyItemData = COMMON.penaltyItemData.get();
        cancelReviveOnDamage = COMMON.cancelReviveOnDamage.get();
        reviveInvulnTime = COMMON.reviveInvulnTime.get();

        selfReviveOptions = COMMON.selfReviveOptions.get().stream().map(FallenData.SELFREVIVETYPE::valueOf).collect(Collectors.toList());
        if (selfReviveOptions.isEmpty()) selfReviveOptions.add(FallenData.SELFREVIVETYPE.CHANCE);
        if (selfReviveOptions.size() == 1) selfReviveOptions.add(selfReviveOptions.get(0));

        maxSelfRevives = COMMON.maxSelfRevives.get();
        disableSelfReviveIfPlayerDowned = COMMON.disableSelfReviveIfPlayerDowned.get();
        randomizeSelfReviveOptions = COMMON.randomizeSelfReviveOptions.get();
        onlyUseAvailableOptions = COMMON.onlyUseAvailableOptions.get();
        selfPenaltyPercentage = COMMON.selfPenaltyPercentage.get();
        reviveChance = COMMON.reviveChance.get();
        sacrificialItemPercent = COMMON.sacrificialItemPercent.get();
        specificItem = COMMON.specificItem.get();
        specificItemCount = COMMON.specificItemCount.get();
        specificItemData = COMMON.specificItemData.get();
        reviveKillAmount = COMMON.reviveKillAmount.get();
        reviveKillTime = COMMON.reviveKillTime.get();
        harmfulEffectsBlackList = (List<String>) COMMON.harmfulEffectsBlackList.get();
        negativeEffectsTime = COMMON.negativeEffectsTime.get();
        disableReviveEffects = COMMON.disableReviveEffects.get();
        reviveXPLossPercentage = COMMON.reviveXPLossPercentage.get();
        minReviveXPLevel = COMMON.minReviveXPLevel.get();
        fallenPenaltyTimer = COMMON.fallenPenaltyTimer.get();
        runDeathEventFirst = COMMON.runDeathEventFirst.get();
        canGiveUp = COMMON.canGiveUp.get();
        dieOnDisconnect = COMMON.dieOnDisconnect.get();
        reviveHelpCooldown = COMMON.reviveHelpCooldown.get();
        reviveHelpDuration = COMMON.reviveHelpDuration.get();
        fallenPose = COMMON.fallenPose.get();
        canJump = COMMON.canJump.get();
        canMove = COMMON.canMove.get();
        interactWithInventory = COMMON.interactWithInventory.get();
        timeReductionPenalty = COMMON.timeReductionPenalty.get();
        pvpTimer = COMMON.pvpTimer.get();
        revertEffectsOnRevive = COMMON.revertEffectsOnRevive.get();
        downedEffects = (List<String>) COMMON.downedEffects.get();
        blockedCommands = (List<String>) COMMON.blockedCommands.get();
        allowedKeybinds = (List<String>) COMMON.allowedKeybinds.get();
        compactReviveUI = COMMON.compactReviveUI.get();
        soundLevel = COMMON.soundLevel.get();
        silenceRegularMessages = COMMON.silenceRegularMessages.get();
        silenceCommandMessages = COMMON.silenceCommandMessages.get();
        universalChatMessages = COMMON.universalChatMessages.get();
        debugMode.set(COMMON.debugMode.get());
    }

    public static CompoundTag serialize() {
        CompoundTag mainTag = new CompoundTag();
        //Time Left
        mainTag.putInt("timeLeft", timeLeft);
        //Penalty Type
        mainTag.putString("penaltyType", penaltyType.name());
        //Penalty Amount
        mainTag.putDouble("penaltyAmount", penaltyAmount);
        //Penalty Item
        mainTag.putString("penaltyItem", penaltyItem);
        //Penalty Data
        mainTag.putString("penaltyItemData", penaltyItemData);
        //Self Revive options
        String allSelfReviveOptions = "";
        for (FallenData.SELFREVIVETYPE reviveType : selfReviveOptions) {
//            LOGGER.error("Whats the class thing?? " + reviveType.getClass());
            allSelfReviveOptions = allSelfReviveOptions.concat(reviveType.name() + ",");
        }
        mainTag.putString("selfReviveOptions", allSelfReviveOptions);
        //Max Self Revives
        mainTag.putInt("maxSelfRevives", maxSelfRevives);
        //Disable Self Revive If Player Downed
        mainTag.putBoolean("disableSelfReviveIfPlayerDowned", disableSelfReviveIfPlayerDowned);
        //Randomize Self Revive Options
        mainTag.putBoolean("randomizeSelfReviveOptions", randomizeSelfReviveOptions);
        //Self Revive Penalty Percentage
        mainTag.putDouble("selfPenaltyPercentage", selfPenaltyPercentage);
        //Revive Chance
        mainTag.putDouble("reviveChance", reviveChance);
        //Sacrificial Item Percentage
        mainTag.putDouble("sacrificialItemPercent", sacrificialItemPercent);
        //Specific Item
        mainTag.putString("specificItem", specificItem);
        //Specific Item Count
        mainTag.putInt("specificItemCount", specificItemCount);
        //Specific Item Data
        mainTag.putString("specificItemData", specificItemData);
        //Revive Kill Amount
        mainTag.putInt("reviveKillAmount", reviveKillAmount);
        //Revive Kill Time
        mainTag.putInt("reviveKillTime", reviveKillTime);
        //Negative Effects Blacklist
        String allHarmfulEffectsBlackList = "";
        for (String s : harmfulEffectsBlackList)
            allHarmfulEffectsBlackList = allHarmfulEffectsBlackList.concat(s + ",");
        mainTag.putString("harmfulEffectsBlackList", allHarmfulEffectsBlackList);
        //Negative Effects duration
        mainTag.putInt("negativeEffectsTime", negativeEffectsTime);
        //Revive XP Loss Percentage
        mainTag.putDouble("reviveXPLossPercentage", reviveXPLossPercentage);
        //Minimum Revive XP Level
        mainTag.putInt("minReviveXPLevel", minReviveXPLevel);
        //Can Run Death Event
        mainTag.putBoolean("runDeathEventFirst", runDeathEventFirst);
        //is Give Up Disabled
        mainTag.putBoolean("canGiveUp", canGiveUp);
        //How long before they can ask for help
        mainTag.putDouble("reviveHelpCooldown", reviveHelpCooldown);
        //How long the help effects last for
        mainTag.putDouble("reviveHelpDuration", reviveHelpDuration);
        //Fallen pose
        mainTag.putString("fallenPose", fallenPose.toString());
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
        return mainTag;
    }

    public static void deserialize(CompoundTag mainTag) {
        //Time Left
        timeLeft = mainTag.getInt("timeLeft");
        //Penalty Type
        penaltyType = FallenData.PENALTYPE.valueOf(mainTag.getString("penaltyType"));
        //Penalty Amount
        penaltyAmount = mainTag.getDouble("penaltyAmount");
        //Penalty Item
        penaltyItem = mainTag.getString("penaltyItem");
        //Penalty Data
        penaltyItemData = mainTag.getString("penaltyItemData");
        //Self Revive options
        String[] allSelfReviveOptions = mainTag.getString("selfReviveOptions").split(",");
        selfReviveOptions.clear();
        for (String reviveType : allSelfReviveOptions) selfReviveOptions.add(FallenData.SELFREVIVETYPE.valueOf(reviveType));
        //Max Self Revives
        maxSelfRevives = mainTag.getInt("maxSelfRevives");
        //Disable Self Revive If Player Downed
        disableSelfReviveIfPlayerDowned = mainTag.getBoolean("disableSelfReviveIfPlayerDowned");
        //Randomize Self Revive Options
        randomizeSelfReviveOptions = mainTag.getBoolean("randomizeSelfReviveOptions");
        //Self Revive Penalty Percentage
        selfPenaltyPercentage = mainTag.getDouble("selfPenaltyPercentage");
        //Revive Chance
        reviveChance = mainTag.getDouble("reviveChance");
        //Sacrificial Item Percentage
        sacrificialItemPercent = mainTag.getDouble("sacrificialItemPercent");
        //Specific Item
        specificItem = mainTag.getString("specificItem");
        //Specific Item Count
        specificItemCount = mainTag.getInt("specificItemCount");
        //Specific Item Data
        specificItemData = mainTag.getString("specificItemData");
        //Revive Kill Amount
        reviveKillAmount = mainTag.getInt("reviveKillAmount");
        //Revive Kill Time
        reviveKillTime = mainTag.getInt("reviveKillTime");
        //Negative Effects Blacklist
        harmfulEffectsBlackList.clear();
        harmfulEffectsBlackList.addAll(Arrays.asList(mainTag.getString("harmfulEffectsBlackList").split(",")));
        //Negative Effects duration
        negativeEffectsTime = mainTag.getInt("negativeEffectsTime");
        //Revive XP Loss Percentage
        reviveXPLossPercentage = mainTag.getDouble("reviveXPLossPercentage");
        //Minimum Revive XP Level
        minReviveXPLevel = mainTag.getInt("minReviveXPLevel");
        //Run Living Death Event first
        runDeathEventFirst = mainTag.getBoolean("runDeathEventFirst");
        //Is Give Up Disabled
        canGiveUp = mainTag.getBoolean("canGiveUp");
        //How long before they can ask for help
        reviveHelpCooldown = mainTag.getDouble("reviveHelpCooldown");
        //How long the help effects last for
        reviveHelpDuration = mainTag.getDouble("reviveHelpDuration");
        //Fallen Pose
        fallenPose = FALLEN_POSE.valueOf(mainTag.getString("fallenPose"));
        //Can Jump
        canJump = JUMP.valueOf(mainTag.getString("canJump"));
        //can Move
        canMove = mainTag.getBoolean("canMove");
        //open Inventory While Downed
        interactWithInventory = INTERACT_WITH_INVENTORY.valueOf(mainTag.getString("interactWithInventory"));
        //time Reduction Penalty
        timeReductionPenalty = mainTag.getDouble("timeReductionPenalty");
        //pvp Timer
        pvpTimer = mainTag.getInt("pvpTimer");
        //Blocked Commands
        blockedCommands = Arrays.asList(mainTag.getString("blockedCommands").split(","));
        //Allowed Keybinds
        allowedKeybinds = Arrays.asList(mainTag.getString("allowedKeybinds").split(","));
    }

    @SubscribeEvent
    public static void onConfigChanged(final ModConfigEvent eventConfig) {
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
        public final ModConfigSpec.ConfigValue<Integer> timeLeft;
        public final ModConfigSpec.ConfigValue<Integer> reviveTime;
        public final ModConfigSpec.ConfigValue<Double> revivedHealth;
        public final ModConfigSpec.ConfigValue<Double> revivedFood;
        public final ModConfigSpec.EnumValue<FallenData.PENALTYPE> penaltyType;
        public final ModConfigSpec.ConfigValue<Double> penaltyAmount;
        public final ModConfigSpec.ConfigValue<String> penaltyItem;
        public final ModConfigSpec.ConfigValue<String> penaltyItemData;
        public final ModConfigSpec.ConfigValue<Boolean> cancelReviveOnDamage;
        public final ModConfigSpec.ConfigValue<Double> reviveInvulnTime;
        public final ModConfigSpec.ConfigValue<List<? extends String>> selfReviveOptions;
        public final ModConfigSpec.ConfigValue<Integer> maxSelfRevives;
        public final ModConfigSpec.ConfigValue<Boolean> disableSelfReviveIfPlayerDowned;
        public final ModConfigSpec.ConfigValue<Boolean> randomizeSelfReviveOptions;
        public final ModConfigSpec.ConfigValue<Boolean> onlyUseAvailableOptions;
        public final ModConfigSpec.ConfigValue<Double> selfPenaltyPercentage;
        public final ModConfigSpec.ConfigValue<Double> reviveChance;
        public final ModConfigSpec.ConfigValue<Double> sacrificialItemPercent;
        public final ModConfigSpec.ConfigValue<String> specificItem;
        public final ModConfigSpec.ConfigValue<Integer> specificItemCount;
        public final ModConfigSpec.ConfigValue<String> specificItemData;
        public final ModConfigSpec.ConfigValue<Integer> reviveKillAmount;
        public final ModConfigSpec.ConfigValue<Integer> reviveKillTime;
        public final ModConfigSpec.ConfigValue<List<? extends String>> harmfulEffectsBlackList;
        public final ModConfigSpec.ConfigValue<Integer> negativeEffectsTime;
        public final ModConfigSpec.ConfigValue<Boolean> disableReviveEffects;
        public final ModConfigSpec.ConfigValue<Double> reviveXPLossPercentage;
        public final ModConfigSpec.ConfigValue<Integer> minReviveXPLevel;
        public final ModConfigSpec.ConfigValue<Double> fallenPenaltyTimer;
        public final ModConfigSpec.ConfigValue<Boolean> runDeathEventFirst;
        public final ModConfigSpec.ConfigValue<Boolean> canGiveUp;
        public final ModConfigSpec.ConfigValue<Boolean> dieOnDisconnect;
        public final ModConfigSpec.ConfigValue<Double> reviveHelpCooldown;
        public final ModConfigSpec.ConfigValue<Double> reviveHelpDuration;
        public final ModConfigSpec.ConfigValue<FALLEN_POSE> fallenPose;
        public final ModConfigSpec.ConfigValue<JUMP> canJump;
        public final ModConfigSpec.ConfigValue<Boolean> canMove;
        public final ModConfigSpec.ConfigValue<INTERACT_WITH_INVENTORY> interactWithInventory;
        public final ModConfigSpec.ConfigValue<Double> timeReductionPenalty;
        public final ModConfigSpec.ConfigValue<Integer> pvpTimer;
        public final ModConfigSpec.ConfigValue<Boolean> revertEffectsOnRevive;
        public final ModConfigSpec.ConfigValue<List<? extends String>> downedEffects;
        public final ModConfigSpec.ConfigValue<List<? extends String>> blockedCommands;
        public final ModConfigSpec.ConfigValue<List<? extends String>> allowedKeybinds;
        public final ModConfigSpec.ConfigValue<Boolean> compactReviveUI;
        public final ModConfigSpec.ConfigValue<Double> soundLevel;
        public final ModConfigSpec.ConfigValue<Boolean> silenceRegularMessages;
        public final ModConfigSpec.ConfigValue<Boolean> silenceCommandMessages;
        public final ModConfigSpec.ConfigValue<Boolean> universalChatMessages;
        public final ModConfigSpec.ConfigValue<Boolean> debugMode;

        public CommonConfig(ModConfigSpec.Builder builder) {
            builder.push("Self-Revive Settings");

            builder.push("General Settings");
            List<String> defaultSelfReviveList = new ArrayList<>(Arrays.asList(FallenData.SELFREVIVETYPE.SPECIFIC_ITEM.name(), FallenData.SELFREVIVETYPE.RANDOM_ITEMS.name(), FallenData.SELFREVIVETYPE.CHANCE.name(), FallenData.SELFREVIVETYPE.KILL.name(), FallenData.SELFREVIVETYPE.STATUS_EFFECTS.name(), FallenData.SELFREVIVETYPE.EXPERIENCE.name()));
            selfReviveOptions = builder.comment("List of all your self-revive options (Duplicate options are allowed). Self-revive options refresh from this list when the revive penalty ends.\n OPTIONS: CHANCE, RANDOM_ITEMS, SPECIFIC_ITEM, KILL, STATUS_EFFECTS, EXPERIENCE")
                    .defineList("SELF_REVIVE_OPTIONS", new ArrayList<>(defaultSelfReviveList), String::new, defaultSelfReviveList::contains);
            maxSelfRevives = builder.comment("Max self revives (setting to 0 will disable self-revive) (setting to -1 will disable the self-revive max) (In multiplayer self-revive will only be disabled when you reach the max) Refreshes when penalty timer ends.").defineInRange("Max_Self_revives", 3, -1, Integer.MAX_VALUE);
            disableSelfReviveIfPlayerDowned = builder.comment("If self-revive should be disabled if a player places you in the fallen state").define("Disable_Self_Revive_On_PVP", false);
            randomizeSelfReviveOptions = builder.comment("If the chosen self-revive options are picked randomly.").define("Randomize_Self_Revive_Options", true);
            onlyUseAvailableOptions = builder.comment("If the mod should only pick self-revive options that you would meet the requirements for (when possible)").define("Only_Use_Available_Options", false);
            selfPenaltyPercentage = builder.comment("Increases the cost of self-revival options each time you self-revive. Stacks additively. Resets to 0 on fallen penalty timer expiration.").defineInRange("Self_Penalty_Percentage", 0.25, 0, 1);
            builder.pop();

            builder.push("Revive Type Settings");

            builder.push("Chance");
            reviveChance = builder.comment("How high your chance is to revive. (Affected by penalty)").defineInRange("Revive_Chance", 0.75F, 0F, 1F);
            builder.pop();

            builder.push("Random Items");
            sacrificialItemPercent = builder.comment("Percentage to lose for sacrificial items. (Affected by penalty)").defineInRange("Item_Loss_Percentage", 0.25F, 0F, 1F);
            builder.pop();

            builder.push("Specific Item");
            specificItem = builder.comment("Item that you wish to sacrifice. Usage: MODID:ITEM").define("Item_ID", "minecraft:golden_apple");
            specificItemCount = builder.comment("How much of the specific item is needed.").defineInRange("Item_Count", 3, 0, Integer.MAX_VALUE);
            specificItemData = builder.comment("Component Data for the specific item").define("Item_Data", "{}", it -> (it instanceof String) && ((String) it).contains("{") && ((String) it).contains("}"));
            builder.pop();

            builder.push("Kill");
            reviveKillAmount = builder.comment("Amount of Living Entities you must kill before the death timer runs out.").defineInRange("Kill_Count", 2, 0, Integer.MAX_VALUE);
            reviveKillTime = builder.comment("Time in SECONDS you have to kill the needed amount of Living Entities. (Affected by penalty)").defineInRange("Kill_Time", 20, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Status Effects");
            harmfulEffectsBlackList = builder.comment("List of harmful effects that shouldn't be chosen. Usage: (ModId:PotionEffect)(minecraft:slowness)")
                    .defineList("Harmful_Effects_Blacklist", Arrays.asList("minecraft:bad_omen", "minecraft:unluck", "minecraft:instant_damage", "reviveme:kill_revive_effect"),
                            () -> BuiltInRegistries.MOB_EFFECT.entrySet().stream().filter((effect) ->
                                            effect.getValue().getCategory() == MobEffectCategory.HARMFUL)
                                    .map(e -> e.getKey().location().toString()).toList().getFirst(),
                            a -> BuiltInRegistries.MOB_EFFECT.entrySet().stream().filter((effect) ->
                                            effect.getValue().getCategory() == MobEffectCategory.HARMFUL)
                                    .map(e -> e.getKey().location().toString()).toList().contains((String) a));

            negativeEffectsTime = builder.comment("How long the harmful effects will last for. (Affected by penalty)").defineInRange("Effect_Duration", 12, 0, Integer.MAX_VALUE);
            disableReviveEffects = builder.comment("If Revive effects should be disabled when reviving with this self-revive type.").define("Disable_Revive_Effects", true);
            builder.pop();

            builder.push("XP");
            reviveXPLossPercentage = builder.comment("Percentage of XP you should lose to revive. (Affected by penalty)").defineInRange("XP_Loss_Percentage", 0.4F, 0, 1);
            minReviveXPLevel = builder.comment("The minimum amount of XP levels needed.").defineInRange("Minimum_XP_Level", 15, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.pop();
            builder.pop();

            builder.push("Fallen State Settings");
            builder.push("General Settings");
            runDeathEventFirst = builder.comment("If the Living Death Event should run first before this mod does (if Death event runs first and player death is cancelled, revive_me code will not execute. Same thing vice-versa.)")
                    .define("Run_Death_Event_First", true);
            canGiveUp = builder.comment("If you can give up and die").define("Can_Give_Up", true);
            dieOnDisconnect = builder.comment("If you should instantly die if you disconnect while in the fallen state").define("Die_On_Disconnect", false);
            interactWithInventory = builder.comment("If the player can use their inventory while fallen").defineEnum("Interact_With_Inventory", INTERACT_WITH_INVENTORY.LOOK_ONLY);
            downedEffects = builder.comment("Potion effects the player has while fallen (ModId:PotionEffect:Amplification)(minecraft:slowness:0)").define("Downed_Effects", new ArrayList<String>(ImmutableList.of("minecraft:slowness:3")));
            blockedCommands = builder.comment("Commands the player isn't allowed to use while fallen. Type \"/\" to block all commands.").define("Blocked_Commands", new ArrayList<>());
            allowedKeybinds = builder.comment("Keybinds that you can use while in the fallen state. (You can put a piece or the full name of a keybind. (Check the translation json for keybinding names (en_us.json for example))" +
                    "\nExample of binding: 'key.fullscreen' or 'fullscreen' will let you use the 'Toggle Fullscreen' keybinding while in the fallen state)").define("Allowed_Keybinds", new ArrayList<>());
            reviveHelpCooldown = builder.comment("How long before you can call for help again in SECONDS").defineInRange("Revive_Help_Call_Cooldown", 0.75f, 0, Double.MAX_VALUE);
            reviveHelpDuration = builder.comment("How long the Help call effects will last in SECONDS").defineInRange("Revive_Help_Duration", 8F, 0, Double.MAX_VALUE);
            builder.pop();

            builder.push("Timer Settings");
            timeLeft = builder.comment("How long you have before death. Default is 30 seconds. Setting to 0 will disable the timer").defineInRange("Time_Left", 60, 0, Integer.MAX_VALUE);
            timeReductionPenalty = builder.comment("How much time (in seconds) your death timer loses each time you fall. (Less than 1 is a percentage of max death time, -1 will take away the max)").defineInRange("Time_Reduction_Penalty", 5, -1F, Double.MAX_VALUE);
            fallenPenaltyTimer = builder.comment("how long the revive penalty effects will last in SECONDS").defineInRange("Revive_Penalty_Timer", 45, 0F, Double.MAX_VALUE);
            pvpTimer = builder.comment("How much time (in seconds) must pass before you may be killed by other players. Affected by time reduction penalty. Setting to -1 will disable this").defineInRange("PVP_Timer", 10, -1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Movement Settings");
            fallenPose = builder.comment("What pose you have whilst fallen").defineEnum("Fallen_Pose", FALLEN_POSE.CROUCH);
            canJump = builder.comment("If the player may jump while fallen").defineEnum("Can_Jump", JUMP.YES);
            canMove = builder.comment("If the player may move while fallen").define("Can_Move", true);
            builder.pop();
            builder.pop();

            builder.push("Revive Settings");
            builder.push("Revivee Settings");
            revivedHealth = builder.comment("How much health you will be revived with, -1 is max health, Less than 1 is percentage").defineInRange("Revive_Health", 10F, -1F, Integer.MAX_VALUE);
            revivedFood = builder.comment("How much food you will be revived with, -1 is max food, Less than 1 is percentage").defineInRange("Revive_Food", 6F, -1F, Integer.MAX_VALUE);
            reviveInvulnTime = builder.comment("How many seconds of invulnerability you have on revive").defineInRange("Revive_Invuln_Time", 5F, 0F, Float.MAX_VALUE);
            revertEffectsOnRevive = builder.comment("Give back all of the potion effects the player had before entering the fallen state").define("Revert_Effects_On_Revive", false);
            builder.pop();

            builder.push("Reviver Settings");
            reviveTime = builder.comment("How long to revive someone").defineInRange("Revive_Time", 3, 0, Integer.MAX_VALUE);
            penaltyType = builder.comment("What the reviver will lose").defineEnum("Penalty_Type", FallenData.PENALTYPE.FOOD);
            penaltyAmount = builder.comment("Amount that will be taken from reviver, Numbers below 1 and greater than 0 will turn it into a percentage").define("Penalty_Amount", 10D);
            penaltyItem = builder.comment("Item used to revive fallen players (Only if you selected ITEM as penalty type). Usage: MODID:ITEM").define("Revive_Item", "minecraft:golden_apple");
            penaltyItemData = builder.comment("Item data used to revive fallen players (Only if you selected ITEM as penalty type).").define("Revive_Item_Data", "{}");
            cancelReviveOnDamage = builder.comment("If revive should be cancelled when taking damage").define("Cancel_Revive_On_Damage", false);
            builder.pop();
            builder.pop();

            builder.push("Chat Settings");
            silenceRegularMessages = builder.comment("Silence the revive and fallen chat messages").define("Silence_Regular_Messages", false);
            silenceCommandMessages = builder.comment("Silence command chat messages").define("Silence_Command_Messages", false);
            universalChatMessages = builder.comment("If chat messages from this mod should be sent to everyone. If false, will only send to those nearby").define("Universal_Chat_Messages", true);
            builder.pop();

            builder.push("Client Settings");
            compactReviveUI = builder.comment("Makes self-revive UI smaller, more compact").define("Compact_UI", false);
            soundLevel = builder.comment("Controls how loud Revive Me! sounds are").defineInRange("Sound_Level", 0.7F, 0F, 1F);
            builder.pop();

            debugMode = builder.comment("If debug mode should be activated (just for testing and troubleshooting)").define("Debug_Mode", false);
        }

    }

}
