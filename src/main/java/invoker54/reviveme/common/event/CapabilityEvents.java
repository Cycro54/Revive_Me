package invoker54.reviveme.common.event;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.event.FallScreenEvent;
import invoker54.reviveme.common.api.FallenProvider;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncConfigMsg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CapabilityEvents {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallScreenEvent.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void AttachCapability(AttachCapabilitiesEvent<Entity> event) {

        if (event.getObject() instanceof Player) {
            event.addCapability(ReviveMe.FALLEN_LOC, new FallenProvider((Player) event.getObject()));
        }

    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        //System.out.println("LOGGED IN BROS");
        FallenCapability cap = FallenCapability.get((LivingEntity) event.getEntity());
        if (cap.isFallen() && ReviveMeConfig.pauseFallenTimerOnDisconnect) cap.resumeFallTimer();

        cap.syncClient(false);
        NetworkHandler.sendToPlayer(event.getEntity(), new SyncConfigMsg(ReviveMeConfig.serialize()));
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){
        Player player = (Player) event.getEntity();
        if (!player.isAlive()) return;
        FallenCapability cap = FallenCapability.get(player);
        if (!cap.isFallen()) return;
        //Do this just in case.
        cap.pauseTimerOnLogout();
        cap.removeOriginalEffects(true);
        if (!ReviveMeConfig.dieOnDisconnect) return;
        cap.forceDeath();
    }

    @SubscribeEvent
    public static void onStartTrack(PlayerEvent.StartTracking event){
        if (!(event.getTarget() instanceof Player)) return;
        Player targPlayer = (Player) event.getTarget();
        //System.out.println("Start tracking: " + event.getTarget().getDisplayName());

        //Grab and send their cap data
        FallenCapability cap = FallenCapability.get(targPlayer);
        cap.syncClient(false);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event){
        if (event.getEntity().level.isClientSide) return;

        FallenCapability cap = FallenCapability.get((LivingEntity) event.getEntity());
        cap.syncClient(false);
    }

    @SubscribeEvent
    public static void onWorldJoin(EntityJoinLevelEvent event){
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        FallenCapability cap = FallenCapability.get(player);
        cap.syncClient(false);
    }
}