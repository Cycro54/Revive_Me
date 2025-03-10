package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class InstaKillMsg {

    private final UUID playerID;

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
            PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
            PlayerEntity player = playerList.getPlayer(msg.playerID);

            if (player != null){
                FallenCapability cap = FallenCapability.GetFallCap(player);
                cap.kill(player);
            }
        });

        context.setPacketHandled(true);
    }
}
