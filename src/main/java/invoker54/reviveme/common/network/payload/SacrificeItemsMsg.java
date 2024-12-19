package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.ArrayList;

import static invoker54.reviveme.ReviveMe.makeResource;

public record SacrificeItemsMsg() implements CustomPacketPayload {
    public static final Type<SacrificeItemsMsg> TYPE =
            new Type<>(makeResource(NetworkInit.createID(SacrificeItemsMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, SacrificeItemsMsg> CODEC =
            StreamCodec.of((A,B)->{}, friendlyByteBuf -> new SacrificeItemsMsg());

    public static void register(PayloadRegistrar registrar){
        registrar.playToServer(TYPE, CODEC, (msg, context) -> {
            context.enqueueWork(()->{
                Player player = context.player();
                if (player == null) return;
                if (!player.isAlive()) return;

                FallenData cap = FallenData.get(player);

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
                    cap.kill(player);
                }
            });
        }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
