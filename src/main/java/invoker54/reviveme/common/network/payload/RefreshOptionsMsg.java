package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static invoker54.reviveme.ReviveMe.makeResource;

public record RefreshOptionsMsg() implements CustomPacketPayload {
    public static final Type<RefreshOptionsMsg> TYPE =
            new Type<>(makeResource(NetworkInit.createID(RefreshOptionsMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, RefreshOptionsMsg> CODEC =
            StreamCodec.of((a,b) ->{}, friendlyByteBuf -> new RefreshOptionsMsg());

    public static void register(PayloadRegistrar registrar){
        registrar.playToClient(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        boolean isOptionsDisabled = ReviveMeConfig.selfReviveOptions.isEmpty();
                        if (isOptionsDisabled) return;
                        if (!FallenItemScreenEvent.isItemScreenActive) return;

                        FallenItemScreenEvent.switchReviveScreens();
                    });
                }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
