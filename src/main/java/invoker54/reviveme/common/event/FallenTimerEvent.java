package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.FoodStats;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallenTimerEvent {

    @SubscribeEvent
    public static void TickDownTimer(TickEvent.PlayerTickEvent event) {
        //System.out.println("Game time is: " + event.player.level.getGameTime());

        if (event.phase == TickEvent.Phase.END) return;

        if (event.player.isDeadOrDying()) return;

        FallenCapability cap = FallenCapability.GetFallCap(event.player);

//        //System.out.println("event player UUID equals inst player id?: " +
//                (event.player.getUUID() == Minecraft.getInstance().player.getUUID()));

        if (!cap.isFallen() || cap.getOtherPlayer() != null) return;

        //Make sure they aren't sprinting.
        if(event.player.isSprinting()) event.player.setSprinting(false);

        if (!cap.shouldDie()) return;

        if (event.side == LogicalSide.CLIENT) return;

        event.player.setInvulnerable(false);
        event.player.hurt(cap.getDamageSource(), Float.MAX_VALUE);
        //System.out.println("Who's about to die: " + event.player.getDisplayName());
    }

    //Make sure this only runs for the person being revived
    @SubscribeEvent
    public static void TickProgress(TickEvent.PlayerTickEvent event){
        if (event.phase == TickEvent.Phase.END) return;

        FallenCapability cap = FallenCapability.GetFallCap(event.player);

        //make sure other player isn't null
        if(cap.getOtherPlayer() == null) return;

        //If tick progress finishes, revive the fallen player and take whatever you need to take from the reviver
        if(cap.getProgress() < 1) return;

        //Make sure this person is fallen.
        if(!cap.isFallen()) return;

        PlayerEntity fellPlayer = event.player;

        if(event.side == LogicalSide.SERVER){
            PlayerEntity revPlayer = fellPlayer.getServer().getPlayerList().getPlayer(cap.getOtherPlayer());

            //Take penalty amount from reviver
            switch (cap.getPenaltyType()) {
                case NONE:
                    break;
                case HEALTH:
                    revPlayer.setHealth(Math.max(1, revPlayer.getHealth() - cap.getPenaltyAmount(revPlayer)));
                    break;
                case EXPERIENCE:
                    revPlayer.totalExperience -= cap.getPenaltyAmount(revPlayer);
                    break;
                case FOOD:
                    FoodStats food = revPlayer.getFoodData();
                    float amountNeeded = cap.getPenaltyAmount(revPlayer);
                    float saturation = food.getSaturationLevel();
                    food.setSaturation(food.getSaturationLevel() - amountNeeded);
                    amountNeeded = Math.max(0,amountNeeded - saturation);
                    food.setFoodLevel((int) (food.getFoodLevel() - amountNeeded));
                    break;
            }

            //Set the revived players health
            float healAmount;
            if (ReviveMeConfig.revivedHealth == 0){
                healAmount = fellPlayer.getMaxHealth();
            }
            //Percentage
            else if (ReviveMeConfig.revivedHealth > 0 && ReviveMeConfig.revivedHealth < 1){
                healAmount = (float) (fellPlayer.getMaxHealth() * ReviveMeConfig.revivedHealth);
            }
            //Flat value
            else {
                healAmount = ReviveMeConfig.revivedHealth.floatValue();
            }
            fellPlayer.setHealth(healAmount);

            //Set the revived players Food
            float foodAmount;
            if (ReviveMeConfig.revivedFood == 0){
                foodAmount = 40;
            }
            //Percentage
            else if (ReviveMeConfig.revivedFood > 0 && ReviveMeConfig.revivedFood < 1){
                foodAmount = (float) (40 * ReviveMeConfig.revivedFood);
            }
            //Flat value
            else {
                foodAmount = ReviveMeConfig.revivedFood.floatValue();
            }
            //Now set their food level
            fellPlayer.getFoodData().eat((int) Math.min(20, foodAmount), 0);
            //Then their saturation
            fellPlayer.getFoodData().eat(1, Math.max(0, foodAmount - 20)/2);

            //Make them vulnerable to damage
            fellPlayer.setInvulnerable(false);
            //Remove all potion effects
            fellPlayer.removeAllEffects();
        }

        cap.setFallen(false);
        fellPlayer.setPose(Pose.STANDING);
    }
}
