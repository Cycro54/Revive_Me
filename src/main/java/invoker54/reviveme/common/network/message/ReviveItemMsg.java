package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

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

            ItemStack chosenStack = ItemStack.of(msg.itemNBT);
            ReviveItemData reviveData = ReviveItemData.getData(chosenStack, ReviveItemData.USER.FALLEN);

            //When doing items, only things that will stop death is canGiveUp and they use a null item,


            FallenCapability cap = FallenCapability.get(player);
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
