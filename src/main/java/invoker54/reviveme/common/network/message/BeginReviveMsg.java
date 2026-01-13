package invoker54.reviveme.common.network.message;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class BeginReviveMsg {
    private static ModLogger LOGGERT = ModLogger.getLogger(BeginReviveMsg.class, ReviveMeConfig.debugMode);
    public String fallenUUID;

    public BeginReviveMsg(String fallenUUID) {
        this.fallenUUID = fallenUUID;
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.fallenUUID);
    }

    public static BeginReviveMsg decode(FriendlyByteBuf buf) {
        return new BeginReviveMsg(buf.readUtf());
    }

    public static void handle(BeginReviveMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) return;
            if (!player.isAlive()) return;
            FallenCapability cap = FallenCapability.get(player);
            if (cap.isFallen()) return;

            Player targPlayer = player.level.getPlayerByUUID(UUID.fromString(msg.fallenUUID));
            if (targPlayer == null) return;

            //Check if they are reviving someone else
            if (cap.getOtherPlayer() != null) return;

            //Make sure they aren't crouching
            if (player.isDiscrete()) return;

            //Grab that target entity (player)
            //Grab the targets cap too
            FallenCapability targCap = FallenCapability.get(targPlayer);

            //Make sure the target is fallen and isn't being revived already
            if (!targCap.isFallen() || targCap.getOtherPlayer() != null) return;

            //Make sure the player reviving has enough of whatever is required
            if (!targCap.hasEnough(player)) return;

            ItemStack handStack = player.getMainHandItem();
            ReviveItemData itemData = ReviveItemData.getData(handStack, ReviveItemData.USER.REVIVER);
            if (itemData == null) handStack = null;

            double reviveSeconds = itemData == null ? ReviveMeConfig.reviveTime : itemData.getReviveSeconds();
            //Now add the player to the targets fallencapability and vice versa.
            targCap.setProgress(player.level.getGameTime(), reviveSeconds);
            targCap.setOtherPlayerAndItem(player.getUUID(), handStack);
            cap.setProgress(player.level.getGameTime(), reviveSeconds);
            cap.setOtherPlayerAndItem(targPlayer.getUUID(), handStack);

            cap.syncClient(false);
            targCap.syncClient(false);
        });

        context.setPacketHandled(true);
    }
}
