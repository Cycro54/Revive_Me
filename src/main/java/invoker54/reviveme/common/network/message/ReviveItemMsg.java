package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.function.Supplier;

public class ReviveItemMsg {

    public CompoundNBT itemNBT;

    public ReviveItemMsg(CompoundNBT itemData){
        this.itemNBT = itemData;
    }

    public void encode (PacketBuffer buffer){
        buffer.writeNbt(this.itemNBT);
    }

    public static ReviveItemMsg decode(PacketBuffer buf){
        return new ReviveItemMsg(buf.readNbt());
    }

    public static void handle(ReviveItemMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player == null) return;
            if (!player.isAlive()) return;

            FallenCapability cap = FallenCapability.get(player);
            ItemStack chosenStack = ItemStack.of(msg.itemNBT);
            boolean isValid = ReviveMeConfig.refreshItems;
            if (!isValid){
                for (Pair<ItemStack, ReviveItemData> reviveItemPair : cap.getReviveItemList(false)){
                    if (!chosenStack.sameItem(reviveItemPair.getKey())) continue;
                    if (!ItemStack.tagMatches(chosenStack, reviveItemPair.getKey())) continue;
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
            reviveData.revivePlayer(player, false, player, "item");
        });

        context.setPacketHandled(true);
    }
}
