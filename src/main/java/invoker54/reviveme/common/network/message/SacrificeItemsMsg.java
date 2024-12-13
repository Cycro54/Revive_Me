package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.ArrayList;
import java.util.function.Supplier;

public class SacrificeItemsMsg {

    //This is how the Network Handler will handle the message
    public static void handle(SacrificeItemsMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player == null) return;
            if (!player.isAlive()) return;

            FallenCapability cap = FallenCapability.GetFallCap(player);

            if (!cap.usedSacrificedItems() && cap.getItemList().size() != 0) {
                //Take the items.
                for (Item item : cap.getItemList()) {
                    PlayerInventory playerInv = player.inventory;
                    int amountToLose = (int) Math.round(Math.max(1, playerInv.countItem(item) * ReviveMeConfig.sacrificialItemPercent));

                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        ItemStack currStack = playerInv.getItem(a);
                        if (currStack.getItem() == item) {
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
                cap.kill(player);
            }
        });

        context.setPacketHandled(true);
    }
}
