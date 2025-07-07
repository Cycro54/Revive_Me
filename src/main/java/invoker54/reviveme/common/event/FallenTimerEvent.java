package invoker54.reviveme.common.event;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.MathUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import invoker54.reviveme.init.EffectInit;
import invoker54.reviveme.init.SoundInit;
import invoker54.reviveme.mixin.FoodMixin;
import net.minecraft.entity.Pose;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SUpdateHealthPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.FoodStats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallenTimerEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallenTimerEvent.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void changeGamemode(PlayerEvent.PlayerChangeGameModeEvent event){
        PlayerEntity player = event.getPlayer();
        if (!(FallenCapability.GetFallCap(player).isFallen())) return;
        if (event.getNewGameMode() != GameType.CREATIVE && event.getNewGameMode() != GameType.SPECTATOR) return;
        revivePlayer(player,false);
    }

    @SubscribeEvent
    public static void TickDownTimer(TickEvent.PlayerTickEvent event) {
        //System.out.println("Game time is: " + event.player.level.getGameTime());
        if (event.side == LogicalSide.CLIENT) return;

        if (event.phase == TickEvent.Phase.END) return;

        if (event.player.isDeadOrDying()) return;

        FallenCapability cap = FallenCapability.GetFallCap(event.player);

        if (!cap.isFallen() || cap.getOtherPlayer() != null) return;

        //Make sure they aren't sprinting.
        if (event.player.isSprinting()) event.player.setSprinting(false);

        //Make sure they aren't healing
        if (event.player.getHealth() != 1) {
            event.player.setHealth(1);
        }

        //Make sure they have no food either
        event.player.getFoodData().setFoodLevel(0);

        //Finally make sure they have all the required effects.
        FallEvent.modifyPotionEffects(event.player);

        if (!cap.shouldDie()) return;

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
        if (cap.getProgress(true) < 1) return;

        //Make sure this person is fallen.
        if (!cap.isFallen()) return;

        PlayerEntity fellPlayer = event.player;

        PlayerEntity reviver = fellPlayer.getServer().getPlayerList().getPlayer(cap.getOtherPlayer());
        takeFromReviver(reviver, fellPlayer);
        revivePlayer(fellPlayer, false);
    }

    public static void takeFromReviver(PlayerEntity reviver, PlayerEntity fallen) {
        if (reviver == null) return;

        FallenCapability cap = FallenCapability.GetFallCap(fallen);

        //Take penalty amount from reviver
        if (!reviver.isCreative()) {
            int amount = (int) cap.getPenaltyAmount(reviver);
            int leftoverAmount = 0;
            switch (ReviveMeConfig.penaltyType) {
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
                    FoodStats food = reviver.getFoodData();
                    leftoverAmount = (int) Math.max(0,Math.round(amount - food.getSaturationLevel()));
                    ((FoodMixin)food).setSaturationLevel(Math.max(0, food.getSaturationLevel() - amount));
                    food.setFoodLevel(Math.max(0,food.getFoodLevel() - leftoverAmount));
                    ((ServerPlayerEntity)reviver).connection.send(new SUpdateHealthPacket(reviver.getHealth(),
                            reviver.getFoodData().getFoodLevel(), reviver.getFoodData().getSaturationLevel()));
                    break;
                case ITEM: {
                    ItemStack penaltyStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ReviveMeConfig.penaltyItem)));
                    penaltyStack.getOrCreateTag().merge(ReviveMeConfig.penaltyItemData);
                    PlayerInventory playerInv = reviver.inventory;
                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        ItemStack currStack = playerInv.getItem(a);
                        if (!penaltyStack.sameItem(currStack)) continue;
                        if (!ItemStack.tagMatches(penaltyStack, currStack)) continue;

                        int takeAway = (Math.min(amount, currStack.getCount()));
                        amount -= takeAway;
                        currStack.setCount(currStack.getCount() - takeAway);

                        if (amount == 0) break;
                    }
                    break;
                }
            }
        }


        cap = FallenCapability.GetFallCap(reviver);
        cap.setOtherPlayer(null);
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(reviver.getStringUUID(), cap.writeNBT());

        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> reviver),
                new SyncClientCapMsg(nbt));
    }

    public static void revivePlayer(PlayerEntity fallen, boolean isCommand){
        FallenCapability cap = FallenCapability.GetFallCap(fallen);

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
        //Load the saved effects
        cap.loadEffects(fallen);

        //Add the fallen potion effect
        fallen.addEffect(new EffectInstance(EffectInit.FALLEN_EFFECT, (int) (ReviveMeConfig.fallenPenaltyTimer * 20), cap.getPenaltyMultiplier()));

        //Add invulnerability if it isn't 0
        if (ReviveMeConfig.reviveInvulnTime != 0) {
            fallen.addEffect(new EffectInstance(Effects.DAMAGE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
            fallen.addEffect(new EffectInstance(Effects.FIRE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
        }

        cap.setFallen(false);
        fallen.setPose(Pose.STANDING);

        CompoundNBT nbt = new CompoundNBT();
        nbt.put(fallen.getStringUUID(), cap.writeNBT());

        fallen.level.playSound(null, fallen.getX(), fallen.getY(), fallen.getZ(),
                SoundInit.REVIVED, SoundCategory.PLAYERS, 1.0F, MathUtil.randomFloat(0.7F, 1.0F));

        if (!fallen.level.isClientSide) {
            InvoText reviveTxt = InvoText.translate("revive-me.commands.revive_pass",
                    fallen.getDisplayName());
            NetworkHandler.sendMessage(reviveTxt.getText(), isCommand, fallen);

            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> fallen),
                    new SyncClientCapMsg(nbt));
        }
    }
}