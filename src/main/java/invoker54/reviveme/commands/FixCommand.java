package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;

public class FixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("revivemefix")
                        .executes(FixCommand::fixPlayer)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(FixCommand::fixPlayer)
                        )
        );
    }

    private static int fixPlayer(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer caller;
        try {
            caller = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!commandContext.getSource().isPlayer()){
                return 1;
            }
            caller = commandContext.getSource().getPlayer();
        }
        FallenCapability cap = FallenCapability.GetFallCap(caller);

        if (!caller.isAlive()){
            return 1;
        }
        
        //This should fix the player if they are downed
        if (cap.isFallen()){
            caller.server.getPlayerList().broadcastSystemMessage(
                    caller.getDisplayName().copy().append(Component.translatable("revive-me.commands.fix")), false);

            //Set last damage source for later
            if (cap.getDamageSource() == null)
                cap.setDamageSource(DamageSource.OUT_OF_WORLD);

            //If they are out of time, smite them.
            if (cap.shouldDie()){
                caller.setInvulnerable(false);
                caller.hurt(cap.getDamageSource().bypassInvul().bypassArmor(), Float.MAX_VALUE);

                return 1;
            }

            //Set penalty type and amount
            cap.setPenalty(ReviveMeConfig.penaltyType, ReviveMeConfig.penaltyAmount, ReviveMeConfig.penaltyItem);
            //System.out.println(ReviveMeConfig.penaltyType);

            //Make them invulnerable to all damage (besides void and creative of course.)
            caller.setInvulnerable(true);

            caller.removeAllEffects();

            //Make it so they can't move very fast.
            caller.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 99999, 3, false, false, false));

            //Dismount the caller if riding something
            caller.stopRiding();

            //stop them from using an item if they are using one
            caller.stopUsingItem();

            //regenerates sacrificial item list (if you don't already have one)
            if (!cap.usedSacrificedItems() && cap.getItemList().size() == 0){
                //Generate a sacrificial item list
                ArrayList<Item> items = new ArrayList<>();
                for (ItemStack itemStack : caller.getInventory().items) {
                    if (items.contains(itemStack.getItem())) continue;
                    if (!itemStack.isStackable()) continue;
                    if (itemStack.isEmpty()) continue;
                    items.add(itemStack.getItem());
                }
//                LOGGER.debug("What are the contents? " + items);
                //Remove all except 4
                while (items.size() > 4) {
                    items.remove(caller.level.random.nextInt(items.size()));
                }
//                LOGGER.debug("What are the contents? " + items);

                //Now add it to the players capability
                cap.setSacrificialItems(items);
            }

            //Finally send capability code to all players
            CompoundTag nbt = new CompoundTag();

            //System.out.println("Am I fallen?: " + FallenCapability.GetFallCap(caller).isFallen());
            if (cap.getOtherPlayer() != null) {

                Player otherPlayer = caller.level.getPlayerByUUID(cap.getOtherPlayer());
                if (otherPlayer != null) {
                    FallenCapability otherCap = FallenCapability.GetFallCap(otherPlayer);
                    otherCap.resumeFallTimer();
                    otherCap.setOtherPlayer(null);

                    nbt.put(otherPlayer.getStringUUID(), otherCap.writeNBT());
                }
                cap.setOtherPlayer(null);
            }
            nbt.put(caller.getStringUUID(), cap.writeNBT());

            ServerPlayer finalCaller = caller;
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> finalCaller),
                    new SyncClientCapMsg(nbt));
        }
        
        //This should fix the player if they are no longer fallen
        else {
            FallenTimerEvent.revivePlayer(caller);

            caller.server.getPlayerList().broadcastSystemMessage(
                    caller.getDisplayName().copy().append(Component.translatable("revive-me.commands.fix")), false);
        }
        
        return 1;
    }
}
