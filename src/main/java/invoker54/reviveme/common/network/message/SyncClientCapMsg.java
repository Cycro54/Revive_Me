package invoker54.reviveme.common.network.message;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncClientCapMsg {
    public static ModLogger LOGGER = ModLogger.getLogger(SyncClientCapMsg.class, ReviveMeConfig.debugMode);
    //The data
    private String uuid;
    private CompoundTag capDataTag;
    private boolean resetBinds;

    public SyncClientCapMsg(String uuid, CompoundTag capDataTag, boolean resetBinds){
        this.uuid = uuid;
        this.capDataTag = capDataTag;
        this.resetBinds = resetBinds;
    }

    public static void Encode(SyncClientCapMsg msg, FriendlyByteBuf buffer){
        buffer.writeUtf(msg.uuid);
        buffer.writeNbt(msg.capDataTag);
        buffer.writeBoolean(msg.resetBinds);
    }

    public static SyncClientCapMsg Decode(FriendlyByteBuf buffer){
        return new SyncClientCapMsg(buffer.readUtf(), buffer.readNbt(), buffer.readBoolean());
    }

    //This is how the Network Handler will handle the message
    public static void handle(SyncClientCapMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Syncing cap data for a client...");

            Level world = ClientUtil.getWorld();
            if (world == null) return;
            Player player = world.getPlayerByUUID(UUID.fromString(msg.uuid));

            if (player == null) return;
            FallenCapability data = FallenCapability.get(player);
            boolean wasFallen = data.isFallen();
            data.readNBT(msg.capDataTag);

            if (player != ClientUtil.getPlayer()) return;

            if (msg.resetBinds) {
                VanillaKeybindHandler.useHeld = false;
                VanillaKeybindHandler.attackHeld = false;
            }

            if (!ReviveMeConfig.refreshItems && (!wasFallen)) FallenItemScreenEvent.refreshItemData();
        });
        context.setPacketHandled(true);
    }
}
