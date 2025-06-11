package invoker54.reviveme.client.event;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.RestartDeathTimerMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class RevivePlayerActionsEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(RevivePlayerActionsEvent.class, ReviveMeConfig.debugMode);
    private static final Minecraft inst = Minecraft.getInstance();

    @SubscribeEvent
    public static void reviveCheck(TickEvent.PlayerTickEvent event){
        if (event.side == LogicalSide.SERVER) return;

        if(event.phase == TickEvent.Phase.END) return;

        if(event.player != inst.player) return;

        FallenCapability myCap = FallenCapability.GetFallCap(inst.player);
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
//            //System.out.println("Someone I'm reviving? : " + (FallenCapability.GetFallCap((Player)inst.crosshairPickEntity).
//                    compareUUID(myUUID)));
            cancelEvent = !(FallenCapability.GetFallCap((Player) inst.crosshairPickEntity).
                    isReviver(myUUID));
        }

        //Check if I'm holding the use button down
        if(!cancelEvent) {
            //System.out.println("Am I holding use down?: " + inst.options.keyUse.isDown());
            cancelEvent = !VanillaKeybindHandler.useHeld;
        }

        if (cancelEvent){
            String targPlayerUUID = "";
            myCap.setOtherPlayer(null);

            if (targPlayer != null) {
                FallenCapability targCap = FallenCapability.GetFallCap(targPlayer);
                targCap.setOtherPlayer(null);

                targPlayerUUID = targPlayer.getStringUUID();
            }

            NetworkHandler.INSTANCE.sendToServer(new RestartDeathTimerMsg(targPlayerUUID, inst.player.getStringUUID()));
        }
    }


    @SubscribeEvent
    public static void reviveItemUse(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) return;

        if (event.phase == TickEvent.Phase.END) return;

        if (event.player != inst.player) return;

        if (!(inst.crosshairPickEntity instanceof Player)) return;

        FallenCapability cap = FallenCapability.GetFallCap((Player) inst.crosshairPickEntity);
        if (!cap.isFallen()) return;
        if (!inst.player.isUsingItem()) return;

        inst.gameMode.releaseUsingItem(event.player);
    }
}