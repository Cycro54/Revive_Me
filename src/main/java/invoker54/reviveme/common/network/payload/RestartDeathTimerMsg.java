package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static invoker54.reviveme.ReviveMe.makeResource;

public record RestartDeathTimerMsg() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<RestartDeathTimerMsg> TYPE =
            new CustomPacketPayload.Type<>(makeResource(NetworkInit.createID(RestartDeathTimerMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, RestartDeathTimerMsg> CODEC =
            StreamCodec.of((A,B)->{}, friendlyByteBuf -> new RestartDeathTimerMsg());

    public static void register(PayloadRegistrar registrar){
        registrar.playToServer(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        Player reviverPlayer = context.player();
                        FallenData reviverCap = FallenData.get(reviverPlayer);
                        if (reviverCap.getOtherPlayer() == null) return;
                        Player fallenPlayer = reviverPlayer.level().getPlayerByUUID(reviverCap.getOtherPlayer());
                        if (fallenPlayer == null) return;
                        FallenData fallenCap = FallenData.get(fallenPlayer);

                        reviverCap.setOtherPlayer(null);
                        fallenCap.setOtherPlayer(null);

                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(reviverPlayer, new SyncClientCapMsg(reviverPlayer.getUUID(), reviverCap.writeNBT()));
                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(fallenPlayer, new SyncClientCapMsg(fallenPlayer.getUUID(), fallenCap.writeNBT()));
                    });
                }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
