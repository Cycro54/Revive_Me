package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class RestartDeathTimerMsg {

    private final String fallenPlayer;
    private final String reviverPlayer;

    public RestartDeathTimerMsg(String fallenPlayer, String reviverPlayer){
        this.fallenPlayer = fallenPlayer;
        this.reviverPlayer = reviverPlayer;
    }

    public static void Encode(RestartDeathTimerMsg msg, FriendlyByteBuf buffer){
        buffer.writeUtf(msg.fallenPlayer);
        buffer.writeUtf(msg.reviverPlayer);
    }

    public static RestartDeathTimerMsg Decode(FriendlyByteBuf buffer) {
        return new RestartDeathTimerMsg(buffer.readUtf(), buffer.readUtf());}

    //This is how the Network Handler will handle the message
    public static void handle(RestartDeathTimerMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who sent this cap data? " + context.getSender());

            PlayerList list = ServerLifecycleHooks.getCurrentServer().getPlayerList();
            //Reviver player cap
            Player reviverPlayer = list.getPlayer(UUID.fromString(msg.reviverPlayer));
            if (reviverPlayer != null) {
                FallenCapability reviverCap = FallenCapability.get(reviverPlayer);
                reviverCap.setOtherPlayerAndItem(null, null);

                reviverCap.syncClient(true);
            }

            if (msg.fallenPlayer.isEmpty()) return;
            Player fallenPlayer = list.getPlayer(UUID.fromString(msg.fallenPlayer));
            if (fallenPlayer != null){
                FallenCapability fallenCap = FallenCapability.get(fallenPlayer);
                fallenCap.setOtherPlayerAndItem(null, null);
                fallenCap.resumeFallTimer();

                fallenCap.syncClient(true);
            }
        });
        context.setPacketHandled(true);
    }
}
