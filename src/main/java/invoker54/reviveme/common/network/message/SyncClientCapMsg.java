package invoker54.reviveme.common.network.message;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncClientCapMsg {
    public static ModLogger LOGGER = ModLogger.getLogger(SyncClientCapMsg.class, ReviveMeConfig.debugMode);
    //The data
    private String uuid;
    private CompoundNBT capDataTag;
    private boolean resetBinds;

    public SyncClientCapMsg(String uuid, CompoundNBT capDataTag, boolean resetBinds){
        this.uuid = uuid;
        this.capDataTag = capDataTag;
        this.resetBinds = resetBinds;
    }

    public static void Encode(SyncClientCapMsg msg, PacketBuffer buffer){
        buffer.writeUtf(msg.uuid);
        buffer.writeNbt(msg.capDataTag);
        buffer.writeBoolean(msg.resetBinds);
    }

    public static SyncClientCapMsg Decode(PacketBuffer buffer){
        return new SyncClientCapMsg(buffer.readUtf(), buffer.readNbt(), buffer.readBoolean());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncClientCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Syncing cap data for a client...");

            ClientWorld world = Minecraft.getInstance().level;
            if (world == null) return;
            PlayerEntity player = world.getPlayerByUUID(UUID.fromString(msg.uuid));

            if (player == null) return;
            FallenCapability data = FallenCapability.get(player);
            data.readNBT(msg.capDataTag);

            if (player != ClientUtil.getPlayer()) return;

            if (msg.resetBinds) {
                VanillaKeybindHandler.useHeld = false;
                VanillaKeybindHandler.attackHeld = false;
            }

            FallenItemScreenEvent.refreshItemData();
        });
        context.setPacketHandled(true);
    }
}
