package invoker54.reviveme.common.network.payload;

import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.MathUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.NetworkInit;
import invoker54.reviveme.init.SoundInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;

import static invoker54.reviveme.ReviveMe.makeResource;

public record CallForHelpMsg() implements CustomPacketPayload {
    private static final ModLogger LOGGER = ModLogger.getLogger(CallForHelpMsg.class, ReviveMeConfig.debugMode);

    public static final CustomPacketPayload.Type<CallForHelpMsg> TYPE =
            new CustomPacketPayload.Type<>(makeResource(NetworkInit.createID(CallForHelpMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, CallForHelpMsg> CODEC =
            StreamCodec.of((A,B)->{}, friendlyByteBuf -> new CallForHelpMsg());

    public static void register(PayloadRegistrar registrar) {
        registrar.playToServer(TYPE, CODEC, (msg, context) -> {
                    context.enqueueWork(()->{
                        Player player = context.player();
                        if (player.isDeadOrDying()) return;
                        FallenData cap = FallenData.get(player);
                        cap.callForHelp();

                        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncClientCapMsg(player.getUUID(), cap.writeNBT()));

                        float pitch = MathUtil.randomFloat(0.7f, 1.4F);
                        float volume = MathUtil.randomFloat(1.0f, 1.5F);

                        player.playSound(SoundInit.CALL_FOR_HELP, volume, pitch);
                    });
                }
        );
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}