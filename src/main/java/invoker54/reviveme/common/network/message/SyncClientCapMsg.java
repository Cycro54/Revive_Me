package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncClientCapMsg {
    //The data
    private INBT nbtData;
    private String playerID = "";

    public SyncClientCapMsg(INBT nbtData, String playerID){
        this.nbtData = nbtData;
        this.playerID = playerID;
    }

    public static void Encode(SyncClientCapMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
        buffer.writeUtf(msg.playerID);

    }

    public static SyncClientCapMsg Decode(PacketBuffer buffer){
        return new SyncClientCapMsg(buffer.readNbt(), buffer.readUtf());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncClientCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Syncing cap data for a client...");

            ClientWorld world = Minecraft.getInstance().level;

            CompoundNBT nbt = (CompoundNBT) msg.nbtData;

            for (String key : nbt.getAllKeys()) {
                FallenCapability.GetFallCap(world.getPlayerByUUID(UUID.fromString(key))).readNBT(nbt.get(key));
            }

        });
        context.setPacketHandled(true);
    }
}
