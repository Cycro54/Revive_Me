package invoker54.reviveme.common.network.payload;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

import static invoker54.reviveme.ReviveMe.makeResource;

public record BeginReviveMsg(String fallenUUID) implements CustomPacketPayload {
    private static ModLogger LOGGERT = ModLogger.getLogger(BeginReviveMsg.class, ReviveMeConfig.debugMode);

    public static final CustomPacketPayload.Type<BeginReviveMsg> TYPE =
            new CustomPacketPayload.Type<>(makeResource(NetworkInit.createID(BeginReviveMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, BeginReviveMsg> CODEC =
            StreamCodec.of(BeginReviveMsg::encode, BeginReviveMsg::new);

    public static void encode (FriendlyByteBuf buffer, BeginReviveMsg msg){
        buffer.writeUtf(msg.fallenUUID());
    }

    public BeginReviveMsg(FriendlyByteBuf buf){
        this(buf.readUtf());
    }

    public static void register(PayloadRegistrar registrar){
        registrar.playToServer(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        Player player = context.player();
                        if (player == null) return;
                        if (!player.isAlive()) return;
                        FallenData cap = FallenData.get(player);
                        if (cap.isFallen()) return;

                        Player targPlayer = player.level().getPlayerByUUID(UUID.fromString(msg.fallenUUID));
                        if (targPlayer == null) return;

                        //Check if they are reviving someone else
                        if (cap.getOtherPlayer() != null) return;

                        //Make sure they aren't crouching
                        if (player.isDiscrete()) return;

                        //Grab that target entity (player)
                        //Grab the targets cap too
                        FallenData targCap = FallenData.get(targPlayer);

                        //Make sure the target is fallen and isn't being revived already
                        if (!targCap.isFallen() || targCap.getOtherPlayer() != null) return;

                        //Make sure the player reviving has enough of whatever is required
                        if (!targCap.hasEnough(player)) return;

                        ItemStack handStack = player.getMainHandItem();
                        ReviveItemData itemData = ReviveItemData.getData(handStack, ReviveItemData.USER.REVIVER);
                        if (itemData == null) handStack = null;

                        double reviveSeconds = itemData == null ? ReviveMeConfig.reviveTime : itemData.getReviveSeconds();
                        //Now add the player to the targets fallencapability and vice versa.
                        targCap.setProgress(player.level().getGameTime(), reviveSeconds);
                        targCap.setOtherPlayerAndItem(player.getUUID(), handStack);
                        cap.setProgress(player.level().getGameTime(), reviveSeconds);
                        cap.setOtherPlayerAndItem(targPlayer.getUUID(), handStack);

                        targCap.syncClient(false);
                        cap.syncClient(false);
                    });
                }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
