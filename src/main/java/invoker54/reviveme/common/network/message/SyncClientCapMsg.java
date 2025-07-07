package invoker54.reviveme.common.network.message;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncClientCapMsg {
    //The data
    private INBT nbtData;

    public SyncClientCapMsg(INBT nbtData){
        this.nbtData = nbtData;
    }

    public static void Encode(SyncClientCapMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
    }

    public static SyncClientCapMsg Decode(PacketBuffer buffer){
        return new SyncClientCapMsg(buffer.readNbt());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncClientCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Syncing cap data for a client...");

            ClientWorld world = Minecraft.getInstance().level;
            if (world == null) return;

            CompoundNBT nbt = (CompoundNBT) msg.nbtData;

            for (String key : nbt.getAllKeys()) {
                PlayerEntity player = world.getPlayerByUUID(UUID.fromString(key));
                if (player == null) continue;

                FallenCapability cap = FallenCapability.GetFallCap(player);

                cap.readNBT(nbt.get(key));
                if (player == ClientUtil.getPlayer() && cap.isFallen()){
                    VanillaKeybindHandler.useHeld = false;
                    VanillaKeybindHandler.attackHeld = false;
                }
            }
        });
        context.setPacketHandled(true);
    }
}
