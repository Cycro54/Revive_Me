package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public class SacrificeItemsMsg {

    //This is how the Network Handler will handle the message
    public static void handle(SacrificeItemsMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) return;
            if (!player.isAlive()) return;

            FallenCapability cap = FallenCapability.GetFallCap(player);

            if (!cap.usedSacrificedItems() && cap.getItemList().size() != 0) {
                //Take the items.
                for (Item item : cap.getItemList()) {
                    Inventory playerInv = player.getInventory();
                    int amountToLose = (int) Math.round(Math.max(1, playerInv.countItem(item) * ReviveMeConfig.sacrificialItemPercent));

                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        ItemStack currStack = playerInv.getItem(a);
                        if (currStack.is(item)) {
                            int takeAway = (Math.min(amountToLose, currStack.getCount()));
                            amountToLose -= takeAway;
                            currStack.setCount(currStack.getCount() - takeAway);
                        }
                        if (amountToLose == 0) break;
                    }
                }

                cap.setSacrificialItems(new ArrayList<>());
                //Make sure the capability know this path has been used
                cap.setSacrificedItemsUsed(true);
                //Revive the player.
                FallenTimerEvent.revivePlayer(player);
            }
            else if (ReviveMeConfig.canGiveUp){
                //Make them vulnerable
                player.setInvulnerable(false);

                //Then make them take damage from the saved damage source
                player.hurt(cap.getDamageSource().bypassArmor().bypassInvul(), Float.MAX_VALUE);
            }
        });

        context.setPacketHandled(true);
    }
}
