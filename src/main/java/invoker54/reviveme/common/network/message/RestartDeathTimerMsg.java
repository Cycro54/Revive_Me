package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
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
                CompoundTag tag = new CompoundTag();
                FallenCapability reviverCap = FallenCapability.GetFallCap(reviverPlayer);
                reviverCap.setOtherPlayer(null);

                tag.put(reviverPlayer.getStringUUID(), reviverCap.writeNBT());
                NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> reviverPlayer),
                        new SyncClientCapMsg(tag));
            }

            Player fallenPlayer = list.getPlayer(UUID.fromString(msg.fallenPlayer));
            if (fallenPlayer != null){
                CompoundTag tag = new CompoundTag();
                FallenCapability fallenCap = FallenCapability.GetFallCap(fallenPlayer);
                fallenCap.setOtherPlayer(null);
                fallenCap.resumeFallTimer();
                tag.put(fallenPlayer.getStringUUID(), fallenCap.writeNBT());
                NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> fallenPlayer),
                        new SyncClientCapMsg(tag));
            }
        });
        context.setPacketHandled(true);
    }
}