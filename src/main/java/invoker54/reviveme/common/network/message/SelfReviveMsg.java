package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SelfReviveMsg {

    public int selectedOption;

    public SelfReviveMsg(int selectedOption){
        this.selectedOption = selectedOption;
    }

    public void encode (PacketBuffer buffer){
        buffer.writeInt(this.selectedOption);
    }

    public static SelfReviveMsg decode(PacketBuffer buf){
        return new SelfReviveMsg(buf.readInt());
    }

    public static void handle(SelfReviveMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player == null) return;
            if (!player.isAlive()) return;

            FallenCapability cap = FallenCapability.get(player);
            if (!cap.isFallen()){
                cap.syncClient(true);
                return;
            }

            boolean isRightDeathOption = ((player.isDiscrete() || ReviveMeConfig.selfReviveOptions.size() == 1) && msg.selectedOption == 1);

            //If they can't self revive, or they are holding down sneak while using the right self revive option OR There is only one self revive option
            if ((!cap.canSelfRevive() || isRightDeathOption) && ReviveMeConfig.canGiveUp){
                cap.forceDeath();
            }
            else if (cap.canSelfRevive() && !isRightDeathOption){
                cap.useReviveOption(cap.getSelfReviveOption(msg.selectedOption));
            }
        });

        context.setPacketHandled(true);
    }
}
