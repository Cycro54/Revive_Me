package invoker54.reviveme.common.network.message;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class InstaKillMsg {

    private UUID playerID;

    public InstaKillMsg(UUID playerID){
        this.playerID = playerID;
    }

    public static void Encode(InstaKillMsg msg, PacketBuffer buffer){
        buffer.writeUUID(msg.playerID);
    }

    public static InstaKillMsg Decode(PacketBuffer buffer){
        return new InstaKillMsg(buffer.readUUID());
    }

    //This is how the Network Handler will handle the message
    public static void handle(InstaKillMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who's the sender? : " + context.getSender().getName());
            //System.out.println("Does it match send UUID? : " + (context.getSender().getUUID().equals(msg.playerID)));
            ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(msg.playerID).kill();
        });

        context.setPacketHandled(true);
    }
}
