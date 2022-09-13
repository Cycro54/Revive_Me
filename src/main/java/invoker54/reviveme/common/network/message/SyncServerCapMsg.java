package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.event.CapabilityEvents;
import invoker54.reviveme.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.management.PlayerList;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncServerCapMsg {
    //The data
    private INBT nbtData;

    public SyncServerCapMsg(INBT nbtData){
        this.nbtData = nbtData;
    }

    public static void Encode(SyncServerCapMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
    }

    public static SyncServerCapMsg Decode(PacketBuffer buffer){
        return new SyncServerCapMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncServerCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who sent this cap data? " + context.getSender());

            PlayerList list = ServerLifecycleHooks.getCurrentServer().getPlayerList();

            CompoundNBT nbt = (CompoundNBT) msg.nbtData;

            //Make a list of everyone tracking the people in the NBT data
            ArrayList<UUID> trackingPlayers = new ArrayList<>();

            for (String key : nbt.getAllKeys()) {
                //Make sure player can be found
                if (list.getPlayer(UUID.fromString(key)) == null) {
                    //System.out.println("Player can't be found! ");

                    return;
                }

                //Adds all players tracking current key (the UUID)
                trackingPlayers.addAll(CapabilityEvents.playerTracking.get(UUID.fromString(key)));

                //System.out.println("Player found!");
                FallenCapability.GetFallCap(list.getPlayer(UUID.fromString(key))).readNBT(nbt.get(key));
            }

            //region Start removing duplicates from list
            Set<UUID> set = new LinkedHashSet<>(trackingPlayers);

            trackingPlayers.clear();

            trackingPlayers.addAll(set);
            //endregion

            //Finally send that cap data to the players
            for (int i = 0; i < trackingPlayers.size(); i++){
                NetworkHandler.sendToPlayer(list.getPlayer(trackingPlayers.get(i)), new SyncClientCapMsg(nbt, ""));
            }

        });
        context.setPacketHandled(true);
    }
}
