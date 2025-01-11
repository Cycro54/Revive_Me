package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ReviveChanceMsg {
    //This is how the Network Handler will handle the message
    public static void handle(ReviveChanceMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            PlayerEntity player = context.getSender();
            if (player == null) return;
            if (!player.isAlive()) return;

            FallenCapability cap = FallenCapability.GetFallCap(player);
            boolean willDie = player.level.random.nextFloat() > ReviveMeConfig.reviveChance;


            if ((cap.usedChance() && ReviveMeConfig.canGiveUp) || (!cap.usedChance() && willDie)) {
                cap.kill(player);
            }
            else if (!cap.usedChance() && !willDie){
                //And set the revive chance as used
                cap.setReviveChanceUsed(true);
                //Revive the player.
                FallenTimerEvent.revivePlayer(player, false);
            }
        });

        context.setPacketHandled(true);
    }
}
