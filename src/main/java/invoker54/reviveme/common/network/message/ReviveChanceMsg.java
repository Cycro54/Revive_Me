package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReviveChanceMsg {
    //This is how the Network Handler will handle the message
    public static void handle(ReviveChanceMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            Player player = context.getSender();
            if (player == null) return;
            if (!player.isAlive()) return;

            FallenCapability cap = FallenCapability.GetFallCap(player);

            if (player.level.random.nextFloat() > ReviveMeConfig.reviveChance || cap.usedChance()) {
                //Make them vulnerable
                player.setInvulnerable(false);

                //Then make them take damage from the saved damage source
                player.hurt(cap.getDamageSource().bypassArmor().bypassInvul(), Float.MAX_VALUE);
            }
            else {
                //And set the revive chance as used
                cap.setReviveChanceUsed(true);
                //Revive the player.
                FallenTimerEvent.revivePlayer(player);
            }
        });

        context.setPacketHandled(true);
    }
}
