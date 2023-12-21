package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.event.CapabilityEvents;
import invoker54.reviveme.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class SyncServerCapMsg {
    //The data
    private final Tag nbtData;

    public SyncServerCapMsg(Tag nbtData){
        this.nbtData = nbtData;
    }

    public static void Encode(SyncServerCapMsg msg, FriendlyByteBuf buffer){
        buffer.writeNbt((CompoundTag) msg.nbtData);
    }

    public static SyncServerCapMsg Decode(FriendlyByteBuf buffer){
        return new SyncServerCapMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncServerCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who sent this cap data? " + context.getSender());

            PlayerList list = ServerLifecycleHooks.getCurrentServer().getPlayerList();

            CompoundTag nbt = (CompoundTag) msg.nbtData;

            //Make a list of everyone tracking the people in the NBT data
            ArrayList<UUID> trackingPlayers = new ArrayList<>();

            for (String key : nbt.getAllKeys()) {
                //Make sure player can be found
                Player player = list.getPlayer(UUID.fromString(key));
                if (player == null) continue;

                //Adds all players tracking current key (the UUID)
                trackingPlayers.addAll(CapabilityEvents.playerTracking.get(UUID.fromString(key)));

                //System.out.println("Player found!");
                FallenCapability.GetFallCap(player).readNBT(nbt.get(key));
            }

            //region Start removing duplicates from list
            Set<UUID> set = new LinkedHashSet<>(trackingPlayers);

            trackingPlayers.clear();

            trackingPlayers.addAll(set);
            //endregion

            //Finally send that cap data to the players
            for (UUID trackingPlayer : trackingPlayers) {
                NetworkHandler.sendToPlayer(list.getPlayer(trackingPlayer), new SyncClientCapMsg(nbt));
            }

        });
        context.setPacketHandled(true);
    }
}
