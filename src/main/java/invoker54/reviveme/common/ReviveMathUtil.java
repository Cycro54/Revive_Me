package invoker54.reviveme.common;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.MathUtil;
import invoker54.reviveme.client.event.FallScreenEvent;
import net.minecraft.entity.player.PlayerEntity;

public class ReviveMathUtil {

    public static double clampLoop(double value, double min, double max) {
        if (min == max) return max;
        if (min > max) {
            double holder = min;
            min = max;
            max = holder;
        }

        double difference = min - (max + 1);

        if (value > max) return clampLoop(value + difference, min, max);

        if (value < min) return clampLoop(value - difference, min, max);

        return value;
        //-3
        //-2,-1,0,1,2,3
    }

    public static double clamp(double value, double min, double max){
        return Math.max(min, Math.min(value, max));
    }

    public static InvoZone zoneLerp(double percentage, InvoZone beginZone, InvoZone endZone){
        InvoZone lerpZone = beginZone.copy();

        //First middle
        double middleX = MathUtil.lerp(percentage, beginZone.middleX(), endZone.middleX());
        double middleY = MathUtil.lerp(percentage, beginZone.middleY(), endZone.middleY());
        double width = MathUtil.lerp(percentage, beginZone.width(), endZone.width());
        double height = MathUtil.lerp(percentage, beginZone.height(), endZone.height());

        lerpZone.setWidth((float) width).setHeight((float) height).centerX((float) middleX).centerY((float) middleY);
        return lerpZone;
    }

    public static float ticksPassed(double startTime){
        return (float) ((ClientUtil.getWorld().getGameTime() + FallScreenEvent.getPartialTicks()) - startTime);
    }

    public static double percentageLerp(double value, double begin, double end){
        double maxDistance = end - begin;
        double valueDistance = value - begin;

        if (maxDistance == 0) maxDistance = 1;

        return valueDistance/maxDistance;
    }

    public static int getXpNeededForNextLevel(int currentLevel) {
        if (currentLevel >= 30) {
            return 112 + (currentLevel - 30) * 9;
        } else {
            return currentLevel >= 15 ? 37 + (currentLevel - 15) * 5 : 7 + currentLevel * 2;
        }
    }

    public static int getExperienceFromLevel(PlayerEntity player){
        return getExperienceFromLevel(player.experienceLevel, player.experienceProgress);
    }

    public static int getExperienceFromLevel(int xpLevel, float progress){
        int experience = 0;

        for (int a = 0; a < xpLevel; a++){
            experience += getXpNeededForNextLevel(a);
        }
        return experience + Math.round(getXpNeededForNextLevel(xpLevel) * progress);
    }

    public static float getLevelFromExperience(int totalExperience){
        int currentLevel = 0;
        float progress = 0;
        while (totalExperience > 0) {
            int maxNeeded = getXpNeededForNextLevel(currentLevel);
            if (totalExperience < maxNeeded) {
                progress = (float) totalExperience /maxNeeded;
                break;
            }
            totalExperience -= maxNeeded;
            currentLevel++;
        }

        return currentLevel + progress;
    }

    public static void fixExperience(PlayerEntity player){
        player.totalExperience = getExperienceFromLevel(player.experienceLevel, player.experienceProgress);
    }

    public static void giveExperience(PlayerEntity player, int experience){
        player.giveExperiencePoints(experience);
    }
}