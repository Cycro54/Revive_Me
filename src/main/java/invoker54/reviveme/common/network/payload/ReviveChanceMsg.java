package invoker54.reviveme.common.network.payload;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import static invoker54.reviveme.ReviveMe.makeResource;

public record ReviveChanceMsg() implements CustomPacketPayload {
    public static final Type<ReviveChanceMsg> TYPE =
            new Type<>(makeResource(NetworkInit.createID(ReviveChanceMsg.class)));

    public static final StreamCodec<FriendlyByteBuf, ReviveChanceMsg> CODEC =
            StreamCodec.of((A,B)->{}, friendlyByteBuf -> new ReviveChanceMsg());

    public static void register(PayloadRegistrar registrar){
        registrar.playToServer(TYPE, CODEC, (msg, context) -> {
            context.enqueueWork(()->{
                Player player = context.player();
                if (!player.isAlive()) return;

                FallenData cap = FallenData.get(player);
                boolean willDie = player.level().random.nextFloat() > ReviveMeConfig.reviveChance;


                if ((cap.usedChance() && ReviveMeConfig.canGiveUp) || (!cap.usedChance() && willDie)) {
                    cap.kill(player);
                }
                else if (!cap.usedChance() && !willDie){
                    //And set the revive chance as used
                    cap.setReviveChanceUsed(true);
                    //Revive the player.
                    FallenTimerEvent.revivePlayer(player,  false);
                }
            });
        }
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
