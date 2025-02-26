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
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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
                    Inventory playerInv = player.getInventory();
                    //Take the items.
                    for (ItemStack sacrificeStack : cap.getItemList()) {
                        int count = FallenData.countItem(playerInv, sacrificeStack);
                        int amountToLose = (int) Math.round(Math.max(1, count * ReviveMeConfig.sacrificialItemPercent));

                        for (int a = 0; a < playerInv.getContainerSize(); a++) {
                            ItemStack containerStack = playerInv.getItem(a);
                            if (!ItemStack.isSameItemSameComponents(sacrificeStack, containerStack)) continue;
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
        }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
