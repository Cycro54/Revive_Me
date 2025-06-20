package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static invoker54.reviveme.ReviveMe.makeResource;

public record SyncConfigMsg(CompoundTag configTag) implements CustomPacketPayload {
    public static final Type<SyncConfigMsg> TYPE =
            new Type<>(makeResource(NetworkInit.createID(SyncConfigMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, SyncConfigMsg> CODEC =
            StreamCodec.of(SyncConfigMsg::encode, SyncConfigMsg::new);

    public SyncConfigMsg(FriendlyByteBuf buf){
        this(buf.readNbt());
    }

    public static void encode(FriendlyByteBuf buf, SyncConfigMsg msg){
        buf.writeNbt(msg.configTag);
    }

    public static void register(PayloadRegistrar registrar){
        registrar.playToClient(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        ReviveMeConfig.deserialize(msg.configTag);
                    });
                }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
