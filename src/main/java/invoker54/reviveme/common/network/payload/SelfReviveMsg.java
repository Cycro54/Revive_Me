package invoker54.reviveme.common.network.payload;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static invoker54.reviveme.ReviveMe.makeResource;

public record SelfReviveMsg(int selectedOption) implements CustomPacketPayload {
    private static final ModLogger LOGGER = ModLogger.getLogger(SelfReviveMsg.class, ReviveMeConfig.debugMode);

    public static final CustomPacketPayload.Type<SelfReviveMsg> TYPE =
            new CustomPacketPayload.Type<>(makeResource(NetworkInit.createID(SelfReviveMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, SelfReviveMsg> CODEC =
            StreamCodec.of(SelfReviveMsg::encode, SelfReviveMsg::new);

    public SelfReviveMsg(FriendlyByteBuf buf){
        this(buf.readInt());
    }

    public static void encode(FriendlyByteBuf buf, SelfReviveMsg msg){
        buf.writeInt(msg.selectedOption);
    }

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        Player player = context.player();
                        if (player == null) return;
                        if (!player.isAlive()) return;

                        FallenData cap = FallenData.get(player);
                        if (!cap.canSelfRevive()) {
                            cap.kill(player);
                        } else {
                            cap.useReviveOption(cap.getSelfReviveOption(msg.selectedOption), player);
                        }
                    });
                }
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
