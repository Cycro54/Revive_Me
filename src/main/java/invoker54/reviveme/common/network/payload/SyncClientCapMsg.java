package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.UUID;

import static invoker54.reviveme.ReviveMe.makeResource;

public record SyncClientCapMsg(String uuid, CompoundTag capDataTag) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncClientCapMsg> TYPE =
            new CustomPacketPayload.Type<>(makeResource(NetworkInit.createID(SyncClientCapMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, SyncClientCapMsg> CODEC =
            StreamCodec.of(SyncClientCapMsg::encode, SyncClientCapMsg::new);

    public SyncClientCapMsg(UUID uuid, CompoundTag capDataTag){
        this(uuid.toString(), capDataTag);
    }

    public static void encode(FriendlyByteBuf buf, SyncClientCapMsg msg){
        buf.writeUtf(msg.uuid);
        buf.writeNbt(msg.capDataTag);
    }

    public SyncClientCapMsg(FriendlyByteBuf buf){
        this(buf.readUtf(), buf.readNbt());
    }

    public static void register(PayloadRegistrar registrar){
        registrar.playToClient(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        Level level = context.player().level();
                        Player player = level.getPlayerByUUID(UUID.fromString(msg.uuid));
                        if (player == null) return;
                        FallenData.get(player).readNBT(msg.capDataTag);
                    });
                }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
