package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncServerCapMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class RevivePlayerActionsEvent {
    private static Minecraft inst = Minecraft.getInstance();

    @SubscribeEvent
    public static void reviveCheck(TickEvent.PlayerTickEvent event){
        if (event.side == LogicalSide.SERVER) return;

        if(event.phase == TickEvent.Phase.END) return;

        if(event.player != inst.player) return;

        FallenCapability myCap = FallenCapability.GetFallCap(inst.player);
        UUID myUUID = inst.player.getUUID();

        if (myCap.getOtherPlayer() == null) return;

        if (myCap.isFallen()) return;

        PlayerEntity targPlayer = inst.level.getPlayerByUUID(myCap.getOtherPlayer());

        boolean cancelEvent = false;

        //Check if it's a player
        //System.out.println("Player entity instance? : " + (inst.crosshairPickEntity instanceof PlayerEntity));

        cancelEvent = !(inst.crosshairPickEntity instanceof PlayerEntity);

        //Check if that player is being revived by them
        if (!cancelEvent) {
//            //System.out.println("Someone I'm reviving? : " + (FallenCapability.GetFallCap((PlayerEntity)inst.crosshairPickEntity).
//                    compareUUID(myUUID)));
            cancelEvent = !(FallenCapability.GetFallCap((PlayerEntity) inst.crosshairPickEntity).
                    compareUUID(myUUID));
        }

        if(!cancelEvent) {
            //System.out.println("Am I holding use down?: " + inst.options.keyUse.isDown());
            cancelEvent = !inst.options.keyUse.isDown();
        }

        if (cancelEvent){

            myCap.setOtherPlayer(null);

            if (targPlayer != null) {
                FallenCapability targCap = FallenCapability.GetFallCap(targPlayer);
                targCap.setOtherPlayer(null);
                targCap.resumeFallTimer();

                CompoundNBT nbt = new CompoundNBT();
                nbt.put(targPlayer.getStringUUID(), targCap.writeNBT());
                nbt.put(inst.player.getStringUUID(), myCap.writeNBT());

                NetworkHandler.INSTANCE.sendToServer(new SyncServerCapMsg(nbt));
            }
        }
    }
}
