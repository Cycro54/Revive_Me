package invoker54.reviveme.common.event;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.event.FallScreenEvent;
import invoker54.reviveme.common.api.FallenProvider;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import invoker54.reviveme.common.network.message.SyncConfigMsg;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
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
    private static final ModLogger LOGGER = ModLogger.getLogger(FallScreenEvent.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void AttachCapability(AttachCapabilitiesEvent<Entity> event) {

            if (event.getObject() instanceof Player) {
                event.addCapability(ReviveMe.FALLEN_LOC, new FallenProvider(event.getObject().level()));
            }

    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        //System.out.println("LOGGED IN BROS");
        UUID playerUUID = event.getEntity().getUUID();

        playerTracking.putIfAbsent(playerUUID, new ArrayList<>());

        FallenCapability cap = FallenCapability.GetFallCap(event.getEntity());

        CompoundTag nbt = new CompoundTag();
        nbt.put(playerUUID.toString(), cap.writeNBT());

        NetworkHandler.sendToPlayer(event.getEntity(), new SyncClientCapMsg(nbt));
        NetworkHandler.sendToPlayer(event.getEntity(), new SyncConfigMsg(ReviveMeConfig.serialize()));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){
        playerTracking.remove(event.getEntity().getUUID());
        if (!ReviveMeConfig.dieOnDisconnect) return;

        Player player = event.getEntity();
        if (!player.isAlive()) return;
        FallenCapability cap = FallenCapability.GetFallCap(player);
        if (!cap.isFallen()) return;
        cap.kill(player);
    }

    @SubscribeEvent
    public static void onStartTrack(PlayerEvent.StartTracking event){
        if (!(event.getTarget() instanceof Player targPlayer)) return;
        //System.out.println("Start tracking: " + event.getTarget().getDisplayName());

        //Get the target player

        //Grab their cap data
        FallenCapability cap = FallenCapability.GetFallCap(targPlayer);

        //Now add themselves to the targets list of players who are tracking them.
        playerTracking.putIfAbsent(targPlayer.getUUID(), new ArrayList<>());
        playerTracking.get(targPlayer.getUUID()).add(event.getEntity().getUUID());

        //Turn it into a CompoundTag
        CompoundTag nbt = new CompoundTag();
        nbt.put(targPlayer.getStringUUID(),cap.writeNBT());

        //Finally send cap data to the player who is now tracking targPlayer
        NetworkHandler.sendToPlayer(event.getEntity(), new SyncClientCapMsg(nbt));
    }

    @SubscribeEvent
    public static void onStopTrack(PlayerEvent.StopTracking event){
        if (!(event.getTarget() instanceof Player targPlayer)) return;
        //System.out.println("Stop tracking: " + event.getTarget().getDisplayName());

        //Get the target player

        if (playerTracking.get(targPlayer.getUUID()) == null) return;

        //Now add themselves to the targets list of players who are tracking them.
        playerTracking.get(targPlayer.getUUID()).remove(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void onWorldJoin(EntityJoinLevelEvent event){
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;

        FallenCapability cap = FallenCapability.GetFallCap(player);

        CompoundTag nbt = new CompoundTag();
        nbt.put(player.getStringUUID(), cap.writeNBT());

        NetworkHandler.sendToPlayer(player, new SyncClientCapMsg(nbt));
    }
}