package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SelfReviveMsg {

    public int selectedOption;

    public SelfReviveMsg(int selectedOption) {
        this.selectedOption = selectedOption;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(this.selectedOption);
    }

    public static SelfReviveMsg decode(FriendlyByteBuf buf) {
        return new SelfReviveMsg(buf.readInt());
    }

    public static void handle(SelfReviveMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) return;
            if (!player.isAlive()) return;

            FallenCapability cap = FallenCapability.GetFallCap(player);
            if (!cap.canSelfRevive()) {
                cap.kill(player);
            } else {
                cap.useReviveOption(cap.getSelfReviveOption(msg.selectedOption), player);
            }
        });

        context.setPacketHandled(true);
    }
}
