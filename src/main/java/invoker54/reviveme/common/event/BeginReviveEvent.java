package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class BeginReviveEvent {

    //This will run on client who right clicked, and server
    @SubscribeEvent
    public static void useOnPlayer(PlayerInteractEvent.EntityInteract event) {
        if (!event.getSide().isServer()) return;

        PlayerEntity revplayer = event.getPlayer();

        FallenCapability myCap = FallenCapability.GetFallCap(revplayer);

        //Make sure the player isn't fallen
        if (myCap.isFallen()) return;

        //Make sure target is a player
        if (!(event.getTarget() instanceof PlayerEntity)) return;

        //Grab that target entity (player)
        PlayerEntity targPlayer = (PlayerEntity) event.getTarget();
        //Grab the targets cap too
        FallenCapability targCap = FallenCapability.GetFallCap(targPlayer);

        //Make sure the target is fallen and isn't being revived already
        if (!targCap.isFallen() || targCap.getOtherPlayer() != null) return;

        //Make sure the player reviving has enough of whatever is required
        switch (targCap.getPenaltyType()) {
            case NONE:
                break;
            case HEALTH:
                if (revplayer.getHealth() < targCap.getPenaltyAmount(targPlayer))
                    return;
                break;
            case EXPERIENCE:
                if (revplayer.totalExperience < targCap.getPenaltyAmount(targPlayer))
                    return;
                break;
            case FOOD:
                if ((revplayer.getFoodData().getFoodLevel() + revplayer.getFoodData().getSaturationLevel()) < targCap.getPenaltyAmount(targPlayer))
                    return;
                break;
        }

        //Now add the player to the targets fallencapability and vice versa.
        targCap.setProgress((int) revplayer.level.getGameTime(), ReviveMeConfig.reviveTime);
        targCap.setOtherPlayer(revplayer.getUUID());
        myCap.setProgress((int) revplayer.level.getGameTime(), ReviveMeConfig.reviveTime);
        myCap.setOtherPlayer(targPlayer.getUUID());


        //Make sure the fallen client has this data too
        CompoundNBT nbt = new CompoundNBT();

        nbt.put(revplayer.getStringUUID(), myCap.writeNBT());
        nbt.put(targPlayer.getStringUUID(), targCap.writeNBT());

        NetworkHandler.sendToPlayer(targPlayer, new SyncClientCapMsg(nbt, ""));
        NetworkHandler.sendToPlayer(revplayer, new SyncClientCapMsg(nbt, ""));

    }
}
