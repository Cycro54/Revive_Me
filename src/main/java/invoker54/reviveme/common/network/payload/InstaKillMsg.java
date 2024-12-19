package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static invoker54.reviveme.ReviveMe.makeResource;

public record InstaKillMsg() implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<InstaKillMsg> TYPE =
            new CustomPacketPayload.Type<>(makeResource(NetworkInit.createID(InstaKillMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, InstaKillMsg> CODEC =
            StreamCodec.of((A,B)->{}, friendlyByteBuf -> new InstaKillMsg());

    public static void register(PayloadRegistrar registrar){
        registrar.playToServer(TYPE, CODEC, (msg, context) -> {
            context.enqueueWork(()->{
                FallenData cap = FallenData.get(context.player());
                if (!cap.isFallen()) return;
                cap.kill(context.player());
            });
        }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
