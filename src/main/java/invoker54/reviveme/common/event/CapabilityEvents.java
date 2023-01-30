package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.api.FallenProvider;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CapabilityEvents {
    public static final Map<UUID, ArrayList<UUID>> playerTracking = new HashMap<>();

    @SubscribeEvent
    public static void AttachCapability(AttachCapabilitiesEvent<Entity> event) {

            if (event.getObject() instanceof PlayerEntity) {
                event.addCapability(ReviveMe.FALLEN_LOC, new FallenProvider(event.getObject().level));
            }

    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        //System.out.println("LOGGED IN BROS");
        UUID playerUUID = event.getPlayer().getUUID();

        playerTracking.putIfAbsent(playerUUID, new ArrayList<>());

        FallenCapability cap = FallenCapability.GetFallCap(event.getPlayer());

        CompoundNBT nbt = new CompoundNBT();
        nbt.put(playerUUID.toString(), cap.writeNBT());

        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientCapMsg(nbt));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){
        playerTracking.remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onStartTrack(PlayerEvent.StartTracking event){
        if (!(event.getTarget() instanceof PlayerEntity)) return;
        //System.out.println("Start tracking: " + event.getTarget().getDisplayName());

        //Get the target player
        PlayerEntity targPlayer = (PlayerEntity) event.getTarget();

        //Grab their cap data
        FallenCapability cap = FallenCapability.GetFallCap(targPlayer);

        //Now add themselves to the targets list of players who are tracking them.
        playerTracking.putIfAbsent(targPlayer.getUUID(), new ArrayList<>());
        playerTracking.get(targPlayer.getUUID()).add(event.getPlayer().getUUID());

        //Turn it into a CompoundNBT
        CompoundNBT nbt = new CompoundNBT();
        nbt.put(targPlayer.getStringUUID(),cap.writeNBT());

        //Finally send cap data to the player who is now tracking targPlayer
        NetworkHandler.sendToPlayer(event.getPlayer(), new SyncClientCapMsg(nbt));
    }

    @SubscribeEvent
    public static void onStopTrack(PlayerEvent.StopTracking event){
        if (!(event.getTarget() instanceof PlayerEntity)) return;
        //System.out.println("Stop tracking: " + event.getTarget().getDisplayName());

        //Get the target player
        PlayerEntity targPlayer = (PlayerEntity) event.getTarget();

        if (playerTracking.get(targPlayer.getUUID()) == null) return;

        //Now add themselves to the targets list of players who are tracking them.
        playerTracking.get(targPlayer.getUUID()).remove(event.getPlayer().getUUID());
    }

    @SubscribeEvent
    public static void onWorldJoin(EntityJoinWorldEvent event){
        if (event.getWorld().isClientSide) return;
        if (!(event.getEntity() instanceof PlayerEntity)) return;

        PlayerEntity player = (PlayerEntity) event.getEntity();
        FallenCapability cap = FallenCapability.GetFallCap(player);

        CompoundNBT nbt = new CompoundNBT();
        nbt.put(player.getStringUUID(), cap.writeNBT());

        NetworkHandler.sendToPlayer(player, new SyncClientCapMsg(nbt));
    }
}