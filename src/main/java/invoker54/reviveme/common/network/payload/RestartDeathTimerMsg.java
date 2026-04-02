package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
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

                        Player fallenPlayer = reviverCap.getOtherPlayer() == null ?
                                null : reviverPlayer.level().getServer().getPlayerList().getPlayer(reviverCap.getOtherPlayer());

                        reviverCap.setOtherPlayerAndItem(null, null);
                        reviverCap.syncClient(true);

                        if (fallenPlayer == null) return;
                        FallenData fallenCap = FallenData.get(fallenPlayer);
                        fallenCap.setOtherPlayerAndItem(null, null);
                        fallenCap.resumeFallTimer();
                        fallenCap.syncClient(true);
                    });
                }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
