package invoker54.reviveme.common.config;


import com.google.common.collect.ImmutableList;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ReviveMeConfig {
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public static Integer timeLeft;
    public static Integer reviveTime;
    public static Double revivedHealth;
    public static Double revivedFood;
    public static FallenCapability.PENALTYPE penaltyType;
    public static Double penaltyAmount;
    public static String penaltyItem;
    public static Double reviveInvulnTime;
    public static Double reviveChance;
    public static Double sacrificialItemPercent;
    public static Double fallenPenaltyTimer;
    public static boolean selfReviveMultiplayer;
    public static boolean canGiveUp;
    public static Double reviveHelpCooldown;
    public static Double reviveHelpDuration;
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
    public static double fallenXpPenalty;
    public static double timeReductionPenalty;
    public static Integer pvpTimer;
    public static boolean revertEffectsOnRevive;
    public static List<String> downedEffects;
    public static List<String> blockedCommands;
    public static boolean silenceRegularMessages;
    public static boolean silenceCommandMessages;
    public static boolean universalChatMessages;
    //Client settings
    public static Boolean compactReviveUI;


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
        reviveInvulnTime = COMMON.reviveInvulnTime.get();
        reviveChance = COMMON.reviveChance.get();
        sacrificialItemPercent = COMMON.sacrificialItemPercent.get();
        fallenPenaltyTimer = COMMON.fallenPenaltyTimer.get();
        selfReviveMultiplayer = COMMON.selfReviveMultiplayer.get();
        canGiveUp = COMMON.canGiveUp.get();
        reviveHelpCooldown = COMMON.reviveHelpCooldown.get();
        reviveHelpDuration = COMMON.reviveHelpDuration.get();
        fallenPose = COMMON.fallenPose.get();
        canJump = COMMON.canJump.get();
        canMove = COMMON.canMove.get();
        interactWithInventory = COMMON.interactWithInventory.get();
        fallenXpPenalty = COMMON.fallenXpPenalty.get();
        timeReductionPenalty = COMMON.timeReductionPenalty.get();
        pvpTimer = COMMON.pvpTimer.get();
        revertEffectsOnRevive = COMMON.revertEffectsOnRevive.get();
        downedEffects = (List<String>) COMMON.downedEffects.get();
        blockedCommands = (List<String>) COMMON.blockedCommands.get();
        compactReviveUI = COMMON.compactReviveUI.get();
        silenceRegularMessages = COMMON.silenceRegularMessages.get();
        silenceCommandMessages = COMMON.silenceCommandMessages.get();
        universalChatMessages = COMMON.universalChatMessages.get();
    }

    public static CompoundNBT serialize(){
        CompoundNBT mainTag = new CompoundNBT();
        //Time Left
        mainTag.putInt("timeLeft",timeLeft);
        //Revive Chance
        mainTag.putDouble("reviveChance", reviveChance);
        //Sacrificial Item Percentage
        mainTag.putDouble("sacrificialItemPercent", sacrificialItemPercent);
        //Self Revive Multiplayer
        mainTag.putBoolean("selfReviveMultiplayer", selfReviveMultiplayer);
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
        for (String string: blockedCommands){
            allBlockedCommands = allBlockedCommands.concat(string+",");
        }
        mainTag.putString("blockedCommands", allBlockedCommands);
        return mainTag;
    }

    public static void deserialize(CompoundNBT mainTag){
        //Time Left
        timeLeft = mainTag.getInt("timeLeft");
        //Revive Chance
        reviveChance = mainTag.getDouble("reviveChance");
        //Sacrificial Item Percentage
        sacrificialItemPercent = mainTag.getDouble("sacrificialItemPercent");
        //Self Revive Multiplayer
        selfReviveMultiplayer = mainTag.getBoolean("selfReviveMultiplayer");
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
    }

    @SubscribeEvent
    public static void onConfigChanged(final ModConfig.ModConfigEvent eventConfig){
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
        public final ForgeConfigSpec.ConfigValue<Double> reviveInvulnTime;
        public final ForgeConfigSpec.ConfigValue<Double> reviveChance;
        public final ForgeConfigSpec.ConfigValue<Double> sacrificialItemPercent;
        public final ForgeConfigSpec.ConfigValue<Double> fallenPenaltyTimer;
        public final ForgeConfigSpec.ConfigValue<Boolean> selfReviveMultiplayer;
        public final ForgeConfigSpec.ConfigValue<Boolean> canGiveUp;
        public final ForgeConfigSpec.ConfigValue<Double> reviveHelpCooldown;
        public final ForgeConfigSpec.ConfigValue<Double> reviveHelpDuration;
        public final ForgeConfigSpec.ConfigValue<FALLEN_POSE> fallenPose;
        public final ForgeConfigSpec.ConfigValue<JUMP> canJump;
        public final ForgeConfigSpec.ConfigValue<Boolean> canMove;
        public final ForgeConfigSpec.ConfigValue<INTERACT_WITH_INVENTORY> interactWithInventory;
        public final ForgeConfigSpec.ConfigValue<Double> fallenXpPenalty;
        public final ForgeConfigSpec.ConfigValue<Double> timeReductionPenalty;
        public final ForgeConfigSpec.ConfigValue<Integer> pvpTimer;
        public final ForgeConfigSpec.ConfigValue<Boolean> revertEffectsOnRevive;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> downedEffects;
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> blockedCommands;
        public final ForgeConfigSpec.ConfigValue<Boolean> compactReviveUI;
        public final ForgeConfigSpec.ConfigValue<Boolean> silenceRegularMessages;
        public final ForgeConfigSpec.ConfigValue<Boolean> silenceCommandMessages;
        public final ForgeConfigSpec.ConfigValue<Boolean> universalChatMessages;

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            builder.push("Fallen Player Settings");
            builder.push("Self-Revive Settings");
            selfReviveMultiplayer = builder.comment("If you can use self-revive methods in multiplayer").define("Self_Revive_Multiplayer", true);
            reviveChance = builder.comment("How high your chance is to revive.").defineInRange("Revive Chance", 0.5F, 0F, 1F);
            sacrificialItemPercent = builder.comment("Percentage to lose for sacrificial items.").defineInRange("Sacrificial item percentage", 0.5F, 0F, 1F);
            builder.pop();

            builder.push("Timer Settings");
            timeLeft = builder.comment("How long you have before death. Default is 30 seconds. Setting to 0 will disable the timer").defineInRange("Time Left", 30,0, Integer.MAX_VALUE);
            timeReductionPenalty = builder.comment("How much time (in seconds) your death timer loses each time you fall. (Less than 1 is a percentage of max death time, -1 will take away the max)").defineInRange("Time_Reduction_Penalty", 0.2, -1F, Double.MAX_VALUE);
            fallenPenaltyTimer = builder.comment("how long the revive penalty effects will last in SECONDS").defineInRange("Revive_Penalty_Timer", 45, 0F, Double.MAX_VALUE);
            pvpTimer = builder.comment("How much time (in seconds) must pass before you may be killed by other players. Affected by time reduction penalty. Setting to -1 will disable this").defineInRange("PVP_Timer", 10, -1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Movement Settings");
            fallenPose = builder.comment("What pose you have whilst fallen").defineEnum("Fallen_Pose", FALLEN_POSE.CROUCH);
            canJump = builder.comment("If the player may jump while fallen").defineEnum("Can_Jump", JUMP.YES);
            canMove = builder.comment("If the player may move while fallen").define("Can_Move", true);
            builder.pop();

            builder.push("Other Settings");
            canGiveUp = builder.comment("If you can give up and die").define("Can_Give_Up", true);
            interactWithInventory = builder.comment("If the player can use their inventory while fallen").defineEnum("Interact_With_Inventory", INTERACT_WITH_INVENTORY.LOOK_ONLY);
            fallenXpPenalty = builder.comment("How many xp levels a player loses when downed (Less than 1 is a percentage)").defineInRange("Fallen_Xp_Penalty", 0, 0, Double.MAX_VALUE);
            revertEffectsOnRevive = builder.comment("Give back all of the potion effects the player had before entering the fallen state").define("Revert_Effects_On_Revive", false);
            downedEffects = builder.comment("Potion effects the player has while fallen (ModId:PotionEffect:Tier)(minecraft:slowness:0)").define("Downed_Effects", new ArrayList<String>(ImmutableList.of("minecraft:slowness:3")));
            blockedCommands = builder.comment("Commands the player isn't allowed to use while fallen. Type \"/\" to block all commands.").define("Blocked_Commands", new ArrayList<>());
            reviveHelpCooldown = builder.comment("How long before you can call for help again in SECONDS").defineInRange("Revive_Help_Call_Cooldown", 0.75f, 0, Double.MAX_VALUE);
            reviveHelpDuration = builder.comment("How long the Help call effects will last in SECONDS").defineInRange("Revive_Help_Duration", 8F, 0, Double.MAX_VALUE);
            builder.pop();
            builder.pop();

            builder.push("Revive Settings");
            builder.push("Revivee Settings");
            revivedHealth = builder.comment("How much health you will be revived with, -1 is max health, Less than 1 is percentage").defineInRange("Revive Health", 10F, -1F, Integer.MAX_VALUE);
            revivedFood = builder.comment("How much food you will be revived with, -1 is max food, Less than 1 is percentage").defineInRange("Revive Food", 6F, -1F, Integer.MAX_VALUE);
            reviveInvulnTime = builder.comment("How many seconds of invulnerability you have on revive").defineInRange("Revive_Invuln_Time", 5F, 0F, Float.MAX_VALUE);
            builder.pop();

            builder.push("Reviver Settings");
            reviveTime = builder.comment("How long to revive someone").defineInRange("Revive Time", 3,0, Integer.MAX_VALUE);
            penaltyType = builder.comment("What the reviver will lose").defineEnum("Penalty Type", FallenCapability.PENALTYPE.FOOD);
            penaltyAmount = builder.comment("Amount that will be taken from reviver, Numbers below 1 and greater than 0 will turn it into a percentage").define("Penalty Amount", 10D);
            penaltyItem = builder.comment("Item used to revive fallen players (Only if you selected ITEM as penalty type). Usage: MODID:ITEM").define("Revive Item", "minecraft:golden_apple");
            builder.pop();
            builder.pop();
            
            builder.push("Chat Settings");
            silenceRegularMessages = builder.comment("Silence the revive and fallen chat messages").define("Silence_Regular_Messages", false);
            silenceCommandMessages = builder.comment("Silence command chat messages").define("Silence_Command_Messages", false);
            universalChatMessages = builder.comment("If chat messages from this mod should be sent to everyone. If false, will only send to those nearby").define("Universal_Chat_Messages", true);
            builder.pop();

            builder.push("Client Settings");
            compactReviveUI = builder.comment("Makes self-revive UI smaller, more compact").define("Compact_UI", false);
            builder.pop();
        }

    }

}
