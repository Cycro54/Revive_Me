package invoker54.reviveme.common.config;


import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

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

        public CommonConfig(ForgeConfigSpec.Builder builder) {
            //This is what goes on top inside of the config
            builder.push("Revive Me! Config");
            //This is how you place a variable in the config file
            //exampleInt = BUILDER.comment("This is an integer. Default value is 3.").define("Example Integer", 54);
            timeLeft = builder.comment("How long you have before death. Default is 30 seconds. Setting to 0 will disable the timer").defineInRange("Time Left", 30,0, Integer.MAX_VALUE);

            reviveTime = builder.comment("How long to revive someone").define("Revive Time", 3);

            revivedHealth = builder.comment("How much health you will be revived with, 0 is max health, Less than 1 is percentage").defineInRange("Revive Health", 10F, 0F, Integer.MAX_VALUE);

            revivedFood = builder.comment("How much food you will be revived with, 0 is max food, Less than 1 is percentage").defineInRange("Revive Food", 6F, 0F, Integer.MAX_VALUE);

            penaltyType = builder.comment("What the reviver will lose").defineEnum("Penalty Type", FallenCapability.PENALTYPE.FOOD);

            penaltyAmount = builder.comment("Amount that will be taken from reviver, Numbers below 1 and greater than 0 will turn it into a percentage").define("Penalty Amount", 10D);

            penaltyItem = builder.comment("Item used to revive fallen players (Only if you selected ITEM as penalty type). Usage: MODID:ITEM").define("Revive Item", "minecraft:golden_apple");

            reviveInvulnTime = builder.comment("How many seconds of invulnerability you have on revive").defineInRange("Revive_Invuln_Time", 5F, 0F, Float.MAX_VALUE);

            reviveChance = builder.comment("(SinglePlayer only) How high your chance is to revive.").defineInRange("Revive Chance", 0.5F, 0F, 1F);

            sacrificialItemPercent = builder.comment("(SinglePlayer only) Percentage to lose for sacrificial items.").defineInRange("Sacrificial item percentage", 0.5F, 0F, 1F);

            builder.pop();
        }

    }

}
