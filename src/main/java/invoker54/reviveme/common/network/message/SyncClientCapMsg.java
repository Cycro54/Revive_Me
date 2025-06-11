package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncClientCapMsg {
    //The data
    private final Tag nbtData;

    public SyncClientCapMsg(Tag nbtData) {
        this.nbtData = nbtData;
    }

    public static void Encode(SyncClientCapMsg msg, FriendlyByteBuf buffer) {
        buffer.writeNbt((CompoundTag) msg.nbtData);
    }

    public static SyncClientCapMsg Decode(FriendlyByteBuf buffer) {
        return new SyncClientCapMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncClientCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Syncing cap data for a client...");

            ClientLevel Level = Minecraft.getInstance().level;
            if (Level == null) return;

            CompoundTag nbt = (CompoundTag) msg.nbtData;

            for (String key : nbt.getAllKeys()) {
                Player player = Level.getPlayerByUUID(UUID.fromString(key));
                if (player == null) continue;

                FallenCapability.GetFallCap(player).readNBT(nbt.get(key));
            }

        });
        context.setPacketHandled(true);
    }
}
