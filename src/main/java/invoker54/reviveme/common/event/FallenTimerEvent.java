package invoker54.reviveme.common.event;

import invoker54.invocore.common.MathUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.SyncClientCapMsg;
import invoker54.reviveme.init.MobEffectInit;
import invoker54.reviveme.init.NetworkInit;
import invoker54.reviveme.init.SoundInit;
import invoker54.reviveme.mixin.FoodMixin;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;


@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallenTimerEvent {

    @SubscribeEvent
    public static void changeGamemode(PlayerEvent.PlayerChangeGameModeEvent event){
        Player player = event.getEntity();
        if (!(FallenData.get(player).isFallen())) return;
        if (event.getNewGameMode() != GameType.CREATIVE && event.getNewGameMode() != GameType.SPECTATOR) return;
        revivePlayer(player,false);
    }

    @SubscribeEvent
    public static void TickDownTimer(PlayerTickEvent.Pre event) {
        //System.out.println("Game time is: " + event.getEntity().level.getGameTime());
        if (event.getEntity().level().isClientSide) return;
        
        if (event.getEntity().isDeadOrDying()) return;

        FallenData cap = FallenData.get(event.getEntity());

        if (!cap.isFallen() || cap.getOtherPlayer() != null) return;

        //Make sure they aren't sprinting.
        if(event.getEntity().isSprinting()) event.getEntity().setSprinting(false);

        //Make sure they aren't healing
        if(event.getEntity().getHealth() != 1){
            event.getEntity().setHealth(1);
        }

        //Make sure they have no food either
        event.getEntity().getFoodData().setFoodLevel(0);

        //Finally make sure they have all the required effects.
        FallEvent.applyDownedEffects(event.getEntity());

        if (!cap.shouldDie()) return;

        cap.kill(event.getEntity());
        //System.out.println("Who's about to die: " + event.getEntity().getDisplayName());
    }

    //Make sure this only runs for the person being revived
    @SubscribeEvent
    public static void TickProgress(PlayerTickEvent.Pre event) {
       if (event.getEntity().level().isClientSide) return;

        FallenData cap = FallenData.get(event.getEntity());

        //make sure other player isn't null
        if (cap.getOtherPlayer() == null) return;

        //If tick progress finishes, revive the fallen player and take whatever you need to take from the reviver
        if (cap.getProgress() < 1) return;

        //Make sure this person is fallen.
        if (!cap.isFallen()) return;

        Player fellPlayer = event.getEntity();

        Player reviver = fellPlayer.getServer().getPlayerList().getPlayer(cap.getOtherPlayer());
        takeFromReviver(reviver, fellPlayer);
        revivePlayer(fellPlayer, false);
    }

    public static void takeFromReviver(Player reviver, Player fallen) {
        if (reviver == null) return;

        FallenData cap = FallenData.get(fallen);

        //Take penalty amount from reviver
        if (!reviver.isCreative()) {
            int amount = (int) cap.getPenaltyAmount(reviver);
            int leftoverAmount = 0;
            switch (cap.getPenaltyType()) {
                case NONE:
                    break;
                case HEALTH:
                    leftoverAmount = Math.max(0, Math.round(amount - reviver.getAbsorptionAmount()));
                    reviver.setAbsorptionAmount(reviver.getAbsorptionAmount()-amount);
                    reviver.setHealth(Math.max(1, reviver.getHealth() - leftoverAmount));
                    break;
                case EXPERIENCE:
                    reviver.giveExperienceLevels(-amount);
                    break;
                case FOOD:
                    FoodData food = reviver.getFoodData();
                    leftoverAmount = (int) Math.max(0,Math.round(amount - food.getSaturationLevel()));
                    ((FoodMixin)food).setSaturationLevel(Math.max(0, food.getSaturationLevel() - amount));
                    food.setFoodLevel(Math.max(0,food.getFoodLevel() - leftoverAmount));
                    ((ServerPlayer)reviver).connection.send(new ClientboundSetHealthPacket(reviver.getHealth(),
                            reviver.getFoodData().getFoodLevel(), reviver.getFoodData().getSaturationLevel()));
                    break;
                case ITEM:
                    Item penaltyItem = cap.getPenaltyItem().getItem();
                    Inventory playerInv = reviver.getInventory();
                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        ItemStack currStack = playerInv.getItem(a);
                        if (currStack.getItem() == penaltyItem) {
                            int takeAway = (Math.min(amount, currStack.getCount()));
                            amount -= takeAway;
                            currStack.setCount(currStack.getCount() - takeAway);
                        }
                        if (amount == 0) break;
                    }
                    break;
            }
        }


        cap = FallenData.get(reviver);
        cap.setOtherPlayer(null);
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(reviver, new SyncClientCapMsg(reviver.getUUID(), cap.writeNBT()));
    }

    public static void revivePlayer(Player fallen, boolean isCommand){
        FallenData cap = FallenData.get(fallen);

        //region Set the revived players health
        float healAmount;
        if (ReviveMeConfig.revivedHealth <= 0) {
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
        if (ReviveMeConfig.revivedFood < 0) {
            foodAmount = 40;
        }
        //Percentage
        else if (ReviveMeConfig.revivedFood >= 0 && ReviveMeConfig.revivedFood < 1) {
            foodAmount = (float) (40 * ReviveMeConfig.revivedFood);
        }
        //Flat value
        else {
            foodAmount = ReviveMeConfig.revivedFood.floatValue();
        }
        //Now set their food level
        fallen.getFoodData().setFoodLevel((int) Math.min(foodAmount,20));
        //Then their saturation
        ((FoodMixin)fallen.getFoodData()).setSaturationLevel(Math.max(0, foodAmount-20));
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

        fallen.level().playSound(null, fallen.getX(), fallen.getY(), fallen.getZ(),
                SoundInit.REVIVED, SoundSource.PLAYERS, 1.0F, MathUtil.randomFloat(0.7F, 1.0F));

        NetworkInit.sendMessage(fallen.getDisplayName().copy().append(Component.translatable("revive-me.commands.revive_pass")),
                isCommand, fallen);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(fallen, new SyncClientCapMsg(fallen.getUUID(), cap.writeNBT()));
    }
}
