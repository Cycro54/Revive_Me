package invoker54.reviveme.common.config;


import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.capability.FallenCapability.SELFREVIVETYPE;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ReviveMeConfig {
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final ModLogger LOGGER = ModLogger.getLogger(ReviveMeConfig.class, ReviveMeConfig.debugMode);

    public static Integer timeLeft;
    public static Integer reviveTime;
    public static Double revivedHealth;
    public static Double revivedFood;
    public static FallenCapability.PENALTYPE penaltyType;
    public static Double penaltyAmount;
    public static String penaltyItem;
    public static CompoundTag penaltyItemData;
    public static boolean cancelReviveOnDamage;
    public static Double reviveInvulnTime;
    //region self-revive shtuff
    public static List<FallenCapability.SELFREVIVETYPE> selfReviveOptions = new ArrayList<>();
    public static Integer maxSelfRevives;
    public static boolean disableSelfReviveIfPlayerDowned;
    public static boolean randomizeSelfReviveOptions;
    public static boolean onlyUseAvailableOptions;
    public static Double selfPenaltyPercentage;
    public static Double reviveChance;
    public static boolean reviveChanceKillOnFail;
    public static Double sacrificialItemPercent;
    public static boolean sacrificialItemTakesHotbar;
    public static String specificItem;
    public static Integer specificItemCount;
    public static CompoundTag specificItemData;
    public static Integer reviveKillAmount;
    public static Integer reviveKillTime;
    public static List<MobEffect> harmfulEffects = null;
    public static Integer negativeEffectsTime;
    public static boolean disableReviveEffects;
    public static double reviveXPLossPercentage;
    public static Integer minReviveXPLevel;
    //endregion
    public static Double fallenPenaltyTimer;
    public static boolean runDeathEventFirst;
    public static boolean canGiveUp;
    public static boolean dieOnDisconnect;
    public static Double reviveHelpDuration;
    public static Double reviveGlowMaxDistance;
    public static Double deathTimerMaxDistance;

    public enum FALLEN_POSE{
        CROUCH,
        PRONE,
        SLEEP
    }
    public static FALLEN_POSE fallenPose;
    public enum JUMP{
        YES,
        LIQUID_ONLY,
        NO
    }
    public static JUMP canJump;
    public static boolean canMove;
    public static INTERACT_WITH_INVENTORY interactWithInventory;
    public enum INTERACT_WITH_INVENTORY{
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
    public static final AtomicBoolean debugMode = new AtomicBoolean(true);
    //Client settings
    public static Boolean compactReviveUI = false;
    public static Double soundLevel = 0D;


    private static boolean isDirty = false;

    static {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }
    public static void bakeConfig(){
        timeLeft = COMMON.timeLeft.get();
        reviveTime = COMMON.reviveTime.get();
        revivedHealth = COMMON.revivedHealth.get();
        revivedFood = COMMON.revivedFood.get();
        penaltyType = COMMON.penaltyType.get();
        penaltyAmount = COMMON.penaltyAmount.get();
        penaltyItem = COMMON.penaltyItem.get();
        try {
            penaltyItemData = TagParser.parseTag(COMMON.penaltyItemData.get());
        } catch (CommandSyntaxException e) {
            LOGGER.error("There's a typo somewhere in 'Revive_Item_Data' config for Self Revival! (Revive Me! Mod)");
            LOGGER.error(e.getMessage());
            penaltyItemData = new CompoundTag();
        }
        cancelReviveOnDamage = COMMON.cancelReviveOnDamage.get();
        reviveInvulnTime = COMMON.reviveInvulnTime.get();

        selfReviveOptions = COMMON.selfReviveOptions.get().stream().map(SELFREVIVETYPE::valueOf).collect(Collectors.toList());
        if (selfReviveOptions.isEmpty()) selfReviveOptions.add(SELFREVIVETYPE.CHANCE);
        if (selfReviveOptions.size() == 1) selfReviveOptions.add(selfReviveOptions.get(0));

        maxSelfRevives = COMMON.maxSelfRevives.get();
        disableSelfReviveIfPlayerDowned = COMMON.disableSelfReviveIfPlayerDowned.get();
        randomizeSelfReviveOptions = COMMON.randomizeSelfReviveOptions.get();
        onlyUseAvailableOptions = COMMON.onlyUseAvailableOptions.get();
        selfPenaltyPercentage = COMMON.selfPenaltyPercentage.get();
        reviveChance = COMMON.reviveChance.get();
        reviveChanceKillOnFail = COMMON.reviveChanceKillOnFail.get();
        sacrificialItemPercent = COMMON.sacrificialItemPercent.get();
        sacrificialItemTakesHotbar = COMMON.sacrificialItemTakesHotbar.get();
        specificItem = COMMON.specificItem.get();
        specificItemCount = COMMON.specificItemCount.get();
        try {
            specificItemData = TagParser.parseTag(COMMON.specificItemData.get());
        } catch (CommandSyntaxException e) {
            LOGGER.error("There's a typo somewhere in 'Item_Data' config for Self Revival! (Revive Me! Mod)");
            LOGGER.error(e.getMessage());
            specificItemData = new CompoundTag();
        }
        reviveKillAmount = COMMON.reviveKillAmount.get();
        reviveKillTime = COMMON.reviveKillTime.get();
        List<String> harmfulEffectsBlackList = (List<String>) COMMON.harmfulEffectsBlackList.get();
        Stream<MobEffect> negativeEffects = ForgeRegistries.MOB_EFFECTS.getValues().stream().filter(e -> e.getCategory() == MobEffectCategory.HARMFUL);
        if (harmfulEffectsBlackList.contains("//")){
            harmfulEffects = negativeEffects.filter(a -> harmfulEffectsBlackList.stream()
                    .anyMatch(b -> !b.isEmpty() && a.getRegistryName().toString().contains(b))).collect(Collectors.toList());
        }
        else {
            harmfulEffects = negativeEffects.filter(a -> harmfulEffectsBlackList.stream()
                    .anyMatch(b -> b.isEmpty() || a.getRegistryName().toString().contains(b) == false)).collect(Collectors.toList());
        }

        negativeEffectsTime = COMMON.negativeEffectsTime.get();
        disableReviveEffects = COMMON.disableReviveEffects.get();
        reviveXPLossPercentage = COMMON.reviveXPLossPercentage.get();
        minReviveXPLevel = COMMON.minReviveXPLevel.get();
        fallenPenaltyTimer = COMMON.fallenPenaltyTimer.get();
        runDeathEventFirst = COMMON.runDeathEventFirst.get();
        canGiveUp = COMMON.canGiveUp.get();
        dieOnDisconnect = COMMON.dieOnDisconnect.get();
        reviveHelpDuration = COMMON.reviveHelpDuration.get();
        reviveGlowMaxDistance = COMMON.reviveGlowMaxDistance.get();
        deathTimerMaxDistance = COMMON.deathTimerMaxDistance.get();
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

    public static CompoundTag serialize(){
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
        mainTag.put("penaltyItemData", penaltyItemData);
        //Self Revive options
        String allSelfReviveOptions = "";
        for (SELFREVIVETYPE reviveType : selfReviveOptions) {
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
        mainTag.put("specificItemData", specificItemData);
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
        //is Give Up Disabled
        mainTag.putBoolean("canGiveUp", canGiveUp);
        //How long the help effects last for
        mainTag.putDouble("reviveHelpDuration", reviveHelpDuration);
        //How far a glowing fallen player can be seen
        mainTag.putDouble("reviveGlowMaxDistance", reviveGlowMaxDistance);
        //How far a fallen player's timer can be seen
        mainTag.putDouble("deathTimerMaxDistance", deathTimerMaxDistance);
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

    public static void deserialize(CompoundTag mainTag){
        //Time Left
        timeLeft = mainTag.getInt("timeLeft");
        //Penalty Type
        penaltyType = FallenCapability.PENALTYPE.valueOf(mainTag.getString("penaltyType"));
        //Penalty Amount
        penaltyAmount = mainTag.getDouble("penaltyAmount");
        //Penalty Item
        penaltyItem = mainTag.getString("penaltyItem");
        //Penalty Data
        penaltyItemData = mainTag.getCompound("penaltyItemData");
        //Self Revive options
        String[] allSelfReviveOptions = mainTag.getString("selfReviveOptions").split(",");
        selfReviveOptions.clear();
        for (String reviveType : allSelfReviveOptions) selfReviveOptions.add(SELFREVIVETYPE.valueOf(reviveType));
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
        specificItemData = mainTag.getCompound("specificItemData");
        //Revive Kill Amount
        reviveKillAmount = mainTag.getInt("reviveKillAmount");
        //Revive Kill Time
        reviveKillTime = mainTag.getInt("reviveKillTime");
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
        //How long the help effects last for
        reviveHelpDuration = mainTag.getDouble("reviveHelpDuration");
        //How far a glowing fallen player can be seen
        reviveGlowMaxDistance = mainTag.getDouble("reviveGlowMaxDistance");
        //How far a fallen player's timer can be seen
        deathTimerMaxDistance = mainTag.getDouble("deathTimerMaxDistance");
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
    public static void onConfigChanged(final ModConfigEvent eventConfig){
        //System.out.println("What's the config type? " + eventConfig.getConfig().getType());

        if(eventConfig.getConfig().getSpec() == ReviveMeConfig.COMMON_SPEC){
            bakeConfig();
            markDirty(true);
        }
    }

    public static void markDirty(boolean dirty){
        isDirty = dirty;
    }

    public static boolean isDirty(){
        return isDirty;
    }

    public static class CommonConfig {

        //This is how to make a config value
        //public static final ForgeConfigSpec.ConfigValue<Integer> exampleInt;
        public final ForgeConfigSpec.ConfigValue<Integer> timeLeft;
        public final ForgeConfigSpec.ConfigValue<Integer> reviveTime;
        public final ForgeConfigSpec.ConfigValue<Double> revivedHealth;
        public final ForgeConfigSpec.ConfigValue<Double> revivedFood;
        public final ForgeConfigSpec.EnumValue<FallenCapability.PENALTYPE> penaltyType;
        public final ForgeConfigSpec.ConfigValue<Double> penaltyAmount;
        public final ForgeConfigSpec.ConfigValue<String> penaltyItem;
        public final ForgeConfigSpec.ConfigValue<String> penaltyItemData;
        public final ForgeConfigSpec.ConfigValue<Boolean> cancelReviveOnDamage;
        public final ForgeConfigSpec.ConfigValue<Double> reviveInvulnTime;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> selfReviveOptions;
        public final ForgeConfigSpec.ConfigValue<Integer> maxSelfRevives;
        public final ForgeConfigSpec.ConfigValue<Boolean> disableSelfReviveIfPlayerDowned;
        public final ForgeConfigSpec.ConfigValue<Boolean> randomizeSelfReviveOptions;
        public final ForgeConfigSpec.ConfigValue<Boolean> onlyUseAvailableOptions;
        public final ForgeConfigSpec.ConfigValue<Double> selfPenaltyPercentage;
        public final ForgeConfigSpec.ConfigValue<Double> reviveChance;
        public final ForgeConfigSpec.ConfigValue<Boolean> reviveChanceKillOnFail;
        public final ForgeConfigSpec.ConfigValue<Double> sacrificialItemPercent;
        public final ForgeConfigSpec.ConfigValue<Boolean> sacrificialItemTakesHotbar;
        public final ForgeConfigSpec.ConfigValue<String> specificItem;
        public final ForgeConfigSpec.ConfigValue<Integer> specificItemCount;
        public final ForgeConfigSpec.ConfigValue<String> specificItemData;
        public final ForgeConfigSpec.ConfigValue<Integer> reviveKillAmount;
        public final ForgeConfigSpec.ConfigValue<Integer> reviveKillTime;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> harmfulEffectsBlackList;
        public final ForgeConfigSpec.ConfigValue<Integer> negativeEffectsTime;
        public final ForgeConfigSpec.ConfigValue<Boolean> disableReviveEffects;
        public final ForgeConfigSpec.ConfigValue<Double> reviveXPLossPercentage;
        public final ForgeConfigSpec.ConfigValue<Integer> minReviveXPLevel;
        public final ForgeConfigSpec.ConfigValue<Double> fallenPenaltyTimer;
        public final ForgeConfigSpec.ConfigValue<Boolean> runDeathEventFirst;
        public final ForgeConfigSpec.ConfigValue<Boolean> canGiveUp;
        public final ForgeConfigSpec.ConfigValue<Boolean> dieOnDisconnect;
        public final ForgeConfigSpec.ConfigValue<Double> reviveHelpDuration;
        public final ForgeConfigSpec.ConfigValue<Double> reviveGlowMaxDistance;
        public final ForgeConfigSpec.ConfigValue<Double> deathTimerMaxDistance;
        public final ForgeConfigSpec.ConfigValue<FALLEN_POSE> fallenPose;
        public final ForgeConfigSpec.ConfigValue<JUMP> canJump;
        public final ForgeConfigSpec.ConfigValue<Boolean> canMove;
        public final ForgeConfigSpec.ConfigValue<INTERACT_WITH_INVENTORY> interactWithInventory;
        public final ForgeConfigSpec.ConfigValue<Double> timeReductionPenalty;
        public final ForgeConfigSpec.ConfigValue<Integer> pvpTimer;
        public final ForgeConfigSpec.ConfigValue<Boolean> revertEffectsOnRevive;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> downedEffects;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blockedCommands;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> allowedKeybinds;
        public final ForgeConfigSpec.ConfigValue<Boolean> compactReviveUI;
        public final ForgeConfigSpec.ConfigValue<Double> soundLevel;
        public final ForgeConfigSpec.ConfigValue<Boolean> silenceRegularMessages;
        public final ForgeConfigSpec.ConfigValue<Boolean> silenceCommandMessages;
        public final ForgeConfigSpec.ConfigValue<Boolean> universalChatMessages;
        public final ForgeConfigSpec.ConfigValue<Boolean> debugMode;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Self-Revive Settings");

            builder.push("General Settings");
            List<String> defaultSelfReviveList = new ArrayList<>(Arrays.asList(SELFREVIVETYPE.SPECIFIC_ITEM.name(), SELFREVIVETYPE.RANDOM_ITEMS.name(), SELFREVIVETYPE.CHANCE.name(), SELFREVIVETYPE.KILL.name(), SELFREVIVETYPE.STATUS_EFFECTS.name(), SELFREVIVETYPE.EXPERIENCE.name()));
            selfReviveOptions = builder.comment("List of all your self-revive options (Duplicate options are allowed). Self-revive options refresh from this list when the revive penalty ends. OPTIONS: CHANCE, RANDOM_ITEMS, SPECIFIC_ITEM, KILL, STATUS_EFFECTS, EXPERIENCE")
                    .defineList("SELF_REVIVE_OPTIONS", new ArrayList<>(defaultSelfReviveList), defaultSelfReviveList::contains);
            maxSelfRevives = builder.comment("Max self revives (setting to 0 will disable self-revive) (setting to -1 will disable the self-revive max) (In multiplayer self-revive will only be disabled when you reach the max) Refreshes when penalty timer ends.").defineInRange("Max_Self_revives", 3, -1, Integer.MAX_VALUE);
            disableSelfReviveIfPlayerDowned = builder.comment("If self-revive should be disabled if a player places you in the fallen state").define("Disable_Self_Revive_On_PVP", false);
            randomizeSelfReviveOptions = builder.comment("If the chosen self-revive options are picked randomly.").define("Randomize_Self_Revive_Options", true);
            onlyUseAvailableOptions = builder.comment("If the mod should only pick self-revive options that you would meet the requirements for (when possible)").define("Only_Use_Available_Options", false);
            selfPenaltyPercentage = builder.comment("Increases the cost of self-revival options each time you self-revive. Stacks additively. Resets to 0 on fallen penalty timer expiration.").defineInRange("Self_Penalty_Percentage", 0.25, 0, 1);
            builder.pop();

            builder.push("Revive Type Settings");

            builder.push("Chance");
            reviveChance = builder.comment("How high your chance is to revive. (Affected by penalty)").defineInRange("Revive_Chance", 0.75F, 0F, 1F);
            reviveChanceKillOnFail = builder.comment("If you should die instantly if you fail the chance check.").define("Kill_On_Fail", false);
            builder.pop();

            builder.push("Random Items");
            sacrificialItemPercent = builder.comment("Percentage to lose for sacrificial items. (Affected by penalty)").defineInRange("Item_Loss_Percentage", 0.25F, 0F, 1F);
            sacrificialItemTakesHotbar = builder.comment("Should check hotbar when sacrificing items").define("Include_Hotbar_Items", false);
            builder.pop();

            builder.push("Specific Item");
            specificItem = builder.comment("Item that you wish to sacrifice. Usage: MODID:ITEM").define("Item_ID", "minecraft:golden_apple");
            specificItemCount = builder.comment("How much of the specific item is needed.").defineInRange("Item_Count", 3, 0, Integer.MAX_VALUE);
            specificItemData = builder.comment("NBT Data for the specific item (Don't forget the curly brackets! {})").define("Item_Data", "{}", s -> {
                        if (s == null) return false;
                        if (s.equals("{}")) return true;
                        try {
                            return !(TagParser.parseTag((String) s)).isEmpty();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
            );
            builder.pop();

            builder.push("Kill");
            reviveKillAmount = builder.comment("Amount of Living Entities you must kill before the death timer runs out.").defineInRange("Kill_Count", 2, 0, Integer.MAX_VALUE);
            reviveKillTime = builder.comment("Time in SECONDS you have to kill the needed amount of Living Entities. (Affected by penalty)").defineInRange("Kill_Time", 20, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Status Effects");
            harmfulEffectsBlackList = builder.comment("List of harmful effects that shouldn't be chosen. Add \"//\" to make it a whitelist instead. You can also use parts of an effect Usage: (ModId:PotionEffect) 'minecraft:slowness' or 'slowness' or 'minecraft'")
                    .defineList("Harmful_Effects_Blacklist", Arrays.asList("minecraft:bad_omen", "minecraft:unluck", "minecraft:instant_damage", "reviveme:kill_revive_effect"),
                            a -> !a.toString().isEmpty() && ((a.equals("//")) || ForgeRegistries.MOB_EFFECTS.getValues().stream().filter((effect) -> effect.getCategory() == MobEffectCategory.HARMFUL)
                                    .map(e -> e.getRegistryName().toString()).collect(Collectors.toList()).contains(a)));

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
            runDeathEventFirst = builder.comment("If the regular Death Event should run first before this mod does (if Death event runs first and player death is cancelled, Revive-Me code will not execute. Same thing vice-versa.)")
                    .define("Run_Death_Event_First", true);
            canGiveUp = builder.comment("If you can give up and die").define("Can_Give_Up", true);
            dieOnDisconnect = builder.comment("If you should die instantly if you disconnect while in the fallen state").define("Die_On_Disconnect", false);
            interactWithInventory = builder.comment("If the player can use their inventory while fallen").defineEnum("Interact_With_Inventory", INTERACT_WITH_INVENTORY.LOOK_ONLY);
            downedEffects = builder.comment("Potion effects the player has while fallen (ModId:PotionEffect:Amplification)(minecraft:slowness:0)").define("Downed_Effects", new ArrayList<String>(ImmutableList.of("minecraft:slowness:3")));
            blockedCommands = builder.comment("Commands the player isn't allowed to use while fallen. Type \"/\" to block all commands. Type \"//\" to make this a whitelist.").define("Blocked_Commands", new ArrayList<>());
            allowedKeybinds = builder.comment("Keybinds that you can use while in the fallen state. (You can put a piece or the full name of a keybind. (Check the translation json for keybinding names (en_us.json for example))" +
                    "\nExample of binding: 'key.fullscreen' or 'fullscreen' will let you use the 'Toggle Fullscreen' keybinding while in the fallen state)").define("Allowed_Keybinds", new ArrayList<>());
            reviveHelpDuration = builder.comment("How long the Help call effects will last in SECONDS").defineInRange("Revive_Help_Duration", 10F, 1, Double.MAX_VALUE);
            reviveGlowMaxDistance = builder.comment("How far you can see glowing players").defineInRange("Revive_Glow_Max_Distance", 80, 0, Double.MAX_VALUE);
            deathTimerMaxDistance = builder.comment("How far you can see the death timer for a player in the fallen state.").defineInRange("Death_Timer_Max_Distance", 40, 0, Double.MAX_VALUE);
            builder.pop();

            builder.push("Timer Settings");
            timeLeft = builder.comment("How long you have before death. Setting to 0 will disable the timer").defineInRange("Time_Left", 60, 0, Integer.MAX_VALUE);
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
            penaltyType = builder.comment("What the reviver will lose").defineEnum("Penalty_Type", FallenCapability.PENALTYPE.FOOD);
            penaltyAmount = builder.comment("Amount that will be taken from reviver, Numbers below 1 and greater than 0 will turn it into a percentage").define("Penalty_Amount", 10D);
            penaltyItem = builder.comment("Item used to revive fallen players (Only if you selected ITEM as penalty type). Usage: MODID:ITEM").define("Revive_Item", "minecraft:golden_apple");
            penaltyItemData = builder.comment("Item data used to revive fallen players (Only if you selected ITEM as penalty type) (Don't forget the curly brackets! {}).").define("Revive_Item_Data", "{}", s -> {
                        if (s == null) return false;
                        if (s.equals("{}")) return true;
                        try {
                            return !(TagParser.parseTag((String) s)).isEmpty();
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
            );
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
