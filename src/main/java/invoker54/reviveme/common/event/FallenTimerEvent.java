package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallenTimerEvent {

    @SubscribeEvent
    public static void TickDownTimer(TickEvent.PlayerTickEvent event) {
        //System.out.println("Game time is: " + event.player.level.getGameTime());

        if (event.phase == TickEvent.Phase.END) return;

        if (event.player.isDeadOrDying()) return;

        FallenCapability cap = FallenCapability.GetFallCap(event.player);

        if (!cap.isFallen() || cap.getOtherPlayer() != null) return;

        //If they are in creative mode, cancel the event
        if (event.player.isCreative()){
            cap.setFallen(false);
            event.player.setPose(Pose.STANDING);
            //Remove all potion effects
            event.player.removeAllEffects();
            event.player.setHealth(event.player.getMaxHealth());
            return;
        }

        //Make sure they aren't sprinting.
        if(event.player.isSprinting()) event.player.setSprinting(false);

        //Make sure they aren't healing
        if(event.player.getHealth() != 1){
            event.player.setHealth(1);
        }

        //Make sure they have no food either
        event.player.getFoodData().setFoodLevel(0);
//
//        //Make sure they aren't invulnerable either.
//        if (event.player.isInvulnerable()){
//            event.player.setInvulnerable(false);
//        }

        if (!cap.shouldDie()) return;

        if (event.side == LogicalSide.CLIENT) return;

//        event.player.setInvulnerable(false);
        event.player.hurt(cap.getDamageSource().bypassInvul().bypassArmor(), Float.MAX_VALUE);
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

        Player fellPlayer = event.player;

        if(event.side == LogicalSide.SERVER){
            Player revPlayer = fellPlayer.getServer().getPlayerList().getPlayer(cap.getOtherPlayer());
            if (revPlayer == null) return;
            //Take penalty amount from reviver
            if (!revPlayer.isCreative()) {
                switch (cap.getPenaltyType()) {
                    case NONE:
                        break;
                    case HEALTH:
                        revPlayer.setHealth(Math.max(1, revPlayer.getHealth() - cap.getPenaltyAmount(revPlayer)));
                        break;
                    case EXPERIENCE:
                        revPlayer.giveExperienceLevels(-Math.round(cap.getPenaltyAmount(revPlayer)));
                        break;
                    case FOOD:
                        FoodData food = revPlayer.getFoodData();
                        float amountNeeded = cap.getPenaltyAmount(revPlayer);
                        float saturation = food.getSaturationLevel();
                        if (saturation > 0) food.setSaturation(Math.max(0, food.getSaturationLevel() - amountNeeded));
                        amountNeeded = Math.max(0, amountNeeded - saturation);
                        food.setFoodLevel((int) (food.getFoodLevel() - amountNeeded));
                        break;
                }
            }

            //region Set the revived players health
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
            //endregion

            //region Set the revived players Food
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
            //endregion

            //Remove all potion effects
            fellPlayer.removeAllEffects();

            //Add invulnerability if it isn't 0
            if (ReviveMeConfig.reviveInvulnTime != 0) {
                fellPlayer.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
                fellPlayer.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
            }
        }

        cap.setFallen(false);
        fellPlayer.setPose(Pose.STANDING);

        CompoundTag nbt = new CompoundTag();
        nbt.put(fellPlayer.getStringUUID(), cap.writeNBT());

        if (event.side == LogicalSide.SERVER){
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY.with(() -> fellPlayer),
                    new SyncClientCapMsg(nbt));
        }
    }
}