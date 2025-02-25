package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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
                Inventory playerInv = player.getInventory();
                //Take the items.
                for (ItemStack sacrificeStack : cap.getItemList()) {
                    int count = FallenCapability.countItem(playerInv, sacrificeStack);
                    int amountToLose = (int) Math.round(Math.max(1, count * ReviveMeConfig.sacrificialItemPercent));

                    for (int a = 0; a < playerInv.getContainerSize(); a++) {
                        ItemStack containerStack = playerInv.getItem(a);
                        if (!ItemStack.isSameItemSameTags(sacrificeStack, containerStack)) continue;
                        int takeAway = (Math.min(amountToLose, containerStack.getCount()));
                        amountToLose -= takeAway;
                        containerStack.setCount(containerStack.getCount() - takeAway);
                        if (amountToLose == 0) break;
                    }
                }

                cap.setSacrificialItems(null);
                //Make sure the capability know this path has been used
                cap.setSacrificedItemsUsed(true);
                //Revive the player.
                FallenTimerEvent.revivePlayer(player, false);
            }
            else if (ReviveMeConfig.canGiveUp){
               cap.kill(player);
            }
        });

        context.setPacketHandled(true);
    }
}
