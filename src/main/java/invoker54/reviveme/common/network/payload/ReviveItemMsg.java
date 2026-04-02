package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static invoker54.reviveme.ReviveMe.makeResource;

public record ReviveItemMsg(CompoundTag itemNBT) implements CustomPacketPayload {
    public static final Type<ReviveItemMsg> TYPE =
            new Type<>(makeResource(NetworkInit.createID(ReviveItemMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, ReviveItemMsg> CODEC =
            StreamCodec.of(ReviveItemMsg::encode, ReviveItemMsg::new);

    public ReviveItemMsg(FriendlyByteBuf buf){
        this(buf.readNbt());
    }

    public static void encode(FriendlyByteBuf buf, ReviveItemMsg msg){
        buf.writeNbt(msg.itemNBT);
    }


    public static void register(PayloadRegistrar registrar){
        registrar.playToServer(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        Player player = context.player();
                        if (player == null) return;
                        if (!player.isAlive()) return;

                        FallenData cap = FallenData.get(player);
                        ItemStack chosenStack = ItemStack.CODEC.parse(NbtOps.INSTANCE, msg.itemNBT).getOrThrow();
                        boolean isValid = ReviveMeConfig.refreshItems;
                        if (!isValid){
                            for (var reviveItemPair : cap.getReviveItemList(false)){
                                if (!ItemStack.isSameItem(chosenStack, reviveItemPair.getKey())) continue;
                                if (!FallenData.hasSimilarData(chosenStack, reviveItemPair.getKey())) continue;
                                if (reviveItemPair.getRight().getCountRequired() > reviveItemPair.getLeft().getCount()) continue;
                                isValid = true;
                                break;
                            }
                        }
                        ReviveItemData reviveData = isValid ? ReviveItemData.getData(chosenStack, ReviveItemData.USER.FALLEN) : null;

                        if (!cap.isFallen()){
                            cap.syncClient(true);
                            return;
                        }

                        if (reviveData == null || !cap.canSelfRevive()){
                            if (ReviveMeConfig.canGiveUp) cap.forceDeath();
                            return;
                        }

                        if (reviveData.getItemCount(player, chosenStack) < reviveData.getCountRequired()) return;
                        reviveData.takeItemCount(player, chosenStack);
                        reviveData.revivePlayer(player, false, player, "item");                    });
                }
        );
    }


    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
