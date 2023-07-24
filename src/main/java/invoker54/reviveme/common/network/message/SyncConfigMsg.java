package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncConfigMsg {

    public CompoundTag configTag;


    public SyncConfigMsg(CompoundTag configTag){
        this.configTag = configTag;
    }

    public static void Encode(SyncConfigMsg msg, FriendlyByteBuf buf){
        buf.writeNbt(msg.configTag);
    }

    public static SyncConfigMsg Decode(FriendlyByteBuf buf){
        return new SyncConfigMsg(buf.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncConfigMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ReviveMeConfig.deserialize(msg.configTag);
        });
        context.setPacketHandled(true);
    }
}
