package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
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

        //If they are in creative or spectator mode, cancel the event
        if (event.player.isCreative() || event.player.isSpectator()) {
            if (event.side.isClient()) return;

            revivePlayer(event.player);
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

        //Finally make sure they have all the required effects.
        FallEvent.applyDownedEffects(event.player);

        if (!cap.shouldDie()) return;

        if (event.side == LogicalSide.CLIENT) return;

        cap.kill(event.player);
        //System.out.println("Who's about to die: " + event.player.getDisplayName());
    }

    //Make sure this only runs for the person being revived
    @SubscribeEvent
    public static void TickProgress(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (event.side != LogicalSide.SERVER) return;

        FallenCapability cap = FallenCapability.GetFallCap(event.player);

        //make sure other player isn't null
        if (cap.getOtherPlayer() == null) return;

        //If tick progress finishes, revive the fallen player and take whatever you need to take from the reviver
        if (cap.getProgress() < 1) return;

        //Make sure this person is fallen.
        if (!cap.isFallen()) return;

        Player fellPlayer = event.player;

        Player reviver = fellPlayer.getServer().getPlayerList().getPlayer(cap.getOtherPlayer());
        takeFromReviver(reviver, fellPlayer);
        revivePlayer(fellPlayer);
    }

    public static void takeFromReviver(Player reviver, Player fallen) {
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
                    FoodData food = reviver.getFoodData();
                    float amountNeeded = cap.getPenaltyAmount(reviver);
                    float saturation = food.getSaturationLevel();
                    if (saturation > 0) food.setSaturation(Math.max(0, food.getSaturationLevel() - amountNeeded));
                    amountNeeded = Math.max(0, amountNeeded - saturation);
                    food.setFoodLevel((int) (food.getFoodLevel() - amountNeeded));
                    break;
                case ITEM:
                    int itemAmount = (int) cap.getPenaltyAmount(reviver);
                    Item penaltyItem = cap.getPenaltyItem().getItem();
                    Inventory playerInv = reviver.getInventory();
                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        ItemStack currStack = playerInv.getItem(a);
                        if (currStack.is(penaltyItem)) {
                            int takeAway = (Math.min(itemAmount, currStack.getCount()));
                            itemAmount -= takeAway;
                            currStack.setCount(currStack.getCount() - takeAway);
                        }
                        if (itemAmount == 0) break;
                    }
                    break;
            }
        }


        cap = FallenCapability.GetFallCap(reviver);
        cap.setOtherPlayer(null);
        CompoundTag nbt = new CompoundTag();
        nbt.put(reviver.getStringUUID(), cap.writeNBT());

        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> reviver),
                new SyncClientCapMsg(nbt));
    }

    public static void revivePlayer(Player fallen){
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
        fallen.getFoodData().eat((int) Math.min(20, foodAmount), 0);
        //Then their saturation
        fallen.getFoodData().eat(1, Math.max(0, foodAmount - 20) / 2);
        //endregion

        //Remove all potion effects
        fallen.removeAllEffects();

        //Add the fallen potion effect if one of the two self revives were used
        fallen.addEffect(new MobEffectInstance(MobEffectInit.FALLEN_EFFECT, (int) (ReviveMeConfig.fallenPenaltyTimer * 20), cap.getPenaltyMultiplier()));

        //Add invulnerability if it isn't 0
        if (ReviveMeConfig.reviveInvulnTime != 0) {
            fallen.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
            fallen.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
        }

        cap.setFallen(false);
        fallen.setPose(Pose.STANDING);

        CompoundTag nbt = new CompoundTag();
        nbt.put(fallen.getStringUUID(), cap.writeNBT());

        if (!fallen.level.isClientSide) {
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> fallen),
                    new SyncClientCapMsg(nbt));
        }
    }
}
