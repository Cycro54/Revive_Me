package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import invoker54.reviveme.mixin.FoodMixin;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.FoodStats;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;

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
            event.player.setInvulnerable(false);

            if (event.side.isServer()) {
                //Remove all potion effects
                event.player.removeAllEffects();
                event.player.setHealth(event.player.getMaxHealth());

                CompoundNBT nbt = new CompoundNBT();
                nbt.put(event.player.getStringUUID(), cap.writeNBT());

                if (event.side == LogicalSide.SERVER) {
                    NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> event.player),
                            new SyncClientCapMsg(nbt));
                }
            }
            return;
        }

        //Make sure they are still invulnerable
        if (!event.player.isInvulnerable()){
            event.player.setInvulnerable(true);
        }

        //Make sure they aren't sprinting.
        if(event.player.isSprinting()) event.player.setSprinting(false);

        //Make sure they aren't healing
        if(event.player.getHealth() != 1){
            event.player.setHealth(1);
        }

        //Make sure they have no food either
        event.player.getFoodData().setFoodLevel(0);

        if (!cap.shouldDie()) return;

        if (event.side == LogicalSide.CLIENT) return;

        event.player.setInvulnerable(false);
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

        PlayerEntity fellPlayer = event.player;

        if (event.side == LogicalSide.SERVER) {
            PlayerEntity reviver = fellPlayer.getServer().getPlayerList().getPlayer(cap.getOtherPlayer());
            takeFromReviver(reviver, fellPlayer);
        }

        revivePlayer(fellPlayer);
    }

    public static void takeFromReviver(PlayerEntity reviver, PlayerEntity fallen) {
        if (reviver == null) return;

        FallenCapability cap = FallenCapability.GetFallCap(fallen);

        //Take penalty amount from reviver
        if (!reviver.isCreative()) {
            switch (cap.getPenaltyType()) {
                case NONE:
                    break;
                case HEALTH:
                    reviver.setHealth(Math.max(1, reviver.getHealth() - cap.getPenaltyAmount(reviver)));
                    break;
                case EXPERIENCE:
                    reviver.giveExperienceLevels(-Math.round(cap.getPenaltyAmount(reviver)));
                    break;
                case FOOD:
                    FoodStats food = reviver.getFoodData();
                    float amountNeeded = cap.getPenaltyAmount(reviver);
                    float saturation = food.getSaturationLevel();
                    if (saturation > 0) food.setSaturation(Math.max(0, food.getSaturationLevel() - amountNeeded));
                    amountNeeded = Math.max(0, amountNeeded - saturation);
                    food.setFoodLevel((int) (food.getFoodLevel() - amountNeeded));
                    break;
            }
        }
    }

    public static void revivePlayer(PlayerEntity fallen){
        FallenCapability cap = FallenCapability.GetFallCap(fallen);

        //region Set the revived players health
        float healAmount;
        if (ReviveMeConfig.revivedHealth == 0) {
            healAmount = fallen.getMaxHealth();
        }
        //Percentage
        else if (ReviveMeConfig.revivedHealth > 0 && ReviveMeConfig.revivedHealth < 1) {
            healAmount = (float) (fallen.getMaxHealth() * ReviveMeConfig.revivedHealth);
        }
        //Flat value
        else {
            healAmount = ReviveMeConfig.revivedHealth.floatValue();
        }
        fallen.setHealth(healAmount);
        //endregion

        //region Set the revived players Food
        float foodAmount;
        if (ReviveMeConfig.revivedFood == 0) {
            foodAmount = 40;
        }
        //Percentage
        else if (ReviveMeConfig.revivedFood > 0 && ReviveMeConfig.revivedFood < 1) {
            foodAmount = (float) (40 * ReviveMeConfig.revivedFood);
        }
        //Flat value
        else {
            foodAmount = ReviveMeConfig.revivedFood.floatValue();
        }
        //Now set their food level
        fallen.getFoodData().setFoodLevel((int) Math.min(20, foodAmount));
        //Then their saturation
        ((FoodMixin) fallen.getFoodData()).setSaturationLevel(Math.max(0, foodAmount - 20));
//        fallen.getFoodData().setSaturation(Math.max(0, foodAmount - 20));
        //endregion

        //Remove all potion effects
        fallen.removeAllEffects();

        //Make it so they aren't invulnerable anymore
        fallen.setInvulnerable(false);

        //Add invulnerability if it isn't 0
        if (ReviveMeConfig.reviveInvulnTime != 0) {
            fallen.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
            fallen.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
        }

        cap.setFallen(false);
        fallen.setPose(Pose.STANDING);

        CompoundNBT nbt = new CompoundNBT();
        nbt.put(fallen.getStringUUID(), cap.writeNBT());

        if (!fallen.level.isClientSide) {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> fallen),
                    new SyncClientCapMsg(nbt));
        }
    }
}
