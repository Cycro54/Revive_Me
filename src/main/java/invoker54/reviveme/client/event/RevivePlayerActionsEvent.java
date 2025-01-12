package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.network.payload.RestartDeathTimerMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.UUID;

@EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class RevivePlayerActionsEvent {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Minecraft inst = Minecraft.getInstance();

    @SubscribeEvent
    public static void reviveCheck(PlayerTickEvent.Pre event){
        if (!event.getEntity().level().isClientSide) return;
        if(event.getEntity() != inst.player) return;

        FallenData myCap = FallenData.get(inst.player);
        UUID myUUID = inst.player.getUUID();

        if (myCap.getOtherPlayer() == null) return;

        if (myCap.isFallen()) return;

        Player targPlayer = inst.level.getPlayerByUUID(myCap.getOtherPlayer());

        boolean cancelEvent;

        //Check if it's a player
        //System.out.println("Player entity instance? : " + (inst.crosshairPickEntity instanceof Player));

        cancelEvent = !(inst.crosshairPickEntity instanceof Player);

        //Check if that player is being revived by them
        if (!cancelEvent) {
//            //System.out.println("Someone I'm reviving? : " + (FallenData.get((Player)inst.crosshairPickEntity).
//                    compareUUID(myUUID)));
            cancelEvent = !(FallenData.get((Player) inst.crosshairPickEntity).
                    isReviver(myUUID));
        }

        //Check if I'm holding the use button down
        if(!cancelEvent) {
            //System.out.println("Am I holding use down?: " + inst.options.keyUse.isDown());
            cancelEvent = !VanillaKeybindHandler.useKeyDown;
        }

        if (cancelEvent){
//            String targPlayerUUID = "";
            myCap.setOtherPlayer(null);

            if (targPlayer != null) {
                FallenData targCap = FallenData.get(targPlayer);
                targCap.setOtherPlayer(null);

//                targPlayerUUID = targPlayer.getStringUUID();
            }

            PacketDistributor.sendToServer(new RestartDeathTimerMsg());
//            NetworkHandler.INSTANCE.sendToServer(new RestartDeathTimerMsg(targPlayerUUID, inst.player.getStringUUID()));
        }
    }
}