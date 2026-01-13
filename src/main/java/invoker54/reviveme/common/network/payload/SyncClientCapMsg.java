package invoker54.reviveme.common.network.payload;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
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

public record SyncClientCapMsg(String uuid, CompoundTag capDataTag, boolean resetBinds) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SyncClientCapMsg> TYPE =
            new CustomPacketPayload.Type<>(makeResource(NetworkInit.createID(SyncClientCapMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, SyncClientCapMsg> CODEC =
            StreamCodec.of(SyncClientCapMsg::encode, SyncClientCapMsg::new);

    public SyncClientCapMsg(UUID uuid, CompoundTag capDataTag, boolean resetBinds){
        this(uuid.toString(), capDataTag, resetBinds);
    }

    public static void encode(FriendlyByteBuf buf, SyncClientCapMsg msg){
        buf.writeUtf(msg.uuid);
        buf.writeNbt(msg.capDataTag);
        buf.writeBoolean(msg.resetBinds);
    }

    public SyncClientCapMsg(FriendlyByteBuf buf){
        this(buf.readUtf(), buf.readNbt(), buf.readBoolean());
    }

    public static void register(PayloadRegistrar registrar){
        registrar.playToClient(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        Level level = context.player().level();
                        Player player = level.getPlayerByUUID(UUID.fromString(msg.uuid));
                        if (player == null) return;
                        FallenData.get(player).readNBT(msg.capDataTag);

                        if (player != ClientUtil.getPlayer()) return;

                        if (msg.resetBinds) {
                            VanillaKeybindHandler.useHeld = false;
                            VanillaKeybindHandler.attackHeld = false;
                        }

                        if (!ReviveMeConfig.refreshItems) FallenItemScreenEvent.refreshItemData();
                    });
                }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}