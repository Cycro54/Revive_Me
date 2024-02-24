//package invoker54.reviveme.common.event;
//
//import invoker54.reviveme.ReviveMe;
//import invoker54.reviveme.common.capability.FallenCapability;
//import invoker54.reviveme.common.config.ReviveMeConfig;
//import invoker54.reviveme.common.network.NetworkHandler;
//import invoker54.reviveme.common.network.message.SyncClientCapMsg;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraft.world.entity.player.Player;
//import net.minecraftforge.event.entity.player.PlayerInteractEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//
//@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
//public class InteractionEvents {
//    private static final Logger LOGGER = LogManager.getLogger();
//
//    //This will run on client who right clicked, and server
//    @SubscribeEvent
//    public static void useOnPlayer(PlayerInteractEvent.EntityInteract event) {
//        if (event.getEntity().level.isClientSide) return;
//
//        Player revplayer = event.getEntity();
//
//        FallenCapability myCap = FallenCapability.GetFallCap(revplayer);
//
//        //Make sure the player isn't fallen
//        if (myCap.isFallen()) return;
//
//        //Make sure they aren't crouching
//        if (revplayer.isDiscrete()) return;
//
//        //Make sure target is a player
//        if (!(event.getTarget() instanceof Player targPlayer)) return;
//
//        //Grab that target entity (player)
//        //Grab the targets cap too
//        FallenCapability targCap = FallenCapability.GetFallCap(targPlayer);
//
//        //Make sure the target is fallen and isn't being revived already
//        if (!targCap.isFallen() || targCap.getOtherPlayer() != null) return;
//
//        //Make sure the player reviving has enough of whatever is required
//        if (!targCap.hasEnough(revplayer)) return;
//
//        //Now add the player to the targets fallencapability and vice versa.
//        targCap.setProgress((int) revplayer.level.getGameTime(), ReviveMeConfig.reviveTime);
//        targCap.setOtherPlayer(revplayer.getUUID());
//        myCap.setProgress((int) revplayer.level.getGameTime(), ReviveMeConfig.reviveTime);
//        myCap.setOtherPlayer(targPlayer.getUUID());
//
//
//        //Make sure the fallen client has this data too
//        CompoundTag nbt = new CompoundTag();
//
//        nbt.put(revplayer.getStringUUID(), myCap.writeNBT());
//        nbt.put(targPlayer.getStringUUID(), targCap.writeNBT());
//
//        NetworkHandler.sendToPlayer(targPlayer, new SyncClientCapMsg(nbt));
//        NetworkHandler.sendToPlayer(revplayer, new SyncClientCapMsg(nbt));
//    }
//}