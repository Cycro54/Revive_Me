package invoker54.reviveme.common.network.message;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.init.KeyInit;
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
    private boolean resetBinds;

    public SyncClientCapMsg(INBT nbtData, boolean resetBinds){
        this.nbtData = nbtData;
        this.resetBinds = resetBinds;
    }

    public static void Encode(SyncClientCapMsg msg, PacketBuffer buffer){
        buffer.writeNbt((CompoundNBT) msg.nbtData);
        buffer.writeBoolean(msg.resetBinds);
    }

    public static SyncClientCapMsg Decode(PacketBuffer buffer){
        return new SyncClientCapMsg(buffer.readNbt(), buffer.readBoolean());
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

                FallenCapability cap = FallenCapability.get(player);

                cap.readNBT(nbt.get(key));
                if (player == ClientUtil.getPlayer() && msg.resetBinds){
                    VanillaKeybindHandler.useHeld = (!cap.isFallen() && !KeyInit.rightOption.keyBind.isDown());
                    VanillaKeybindHandler.attackHeld = false;
                }
            }
        });
        context.setPacketHandled(true);
    }
}
