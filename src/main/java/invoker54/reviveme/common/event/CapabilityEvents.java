package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.SyncClientCapMsg;
import invoker54.reviveme.common.network.payload.SyncConfigMsg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CapabilityEvents {

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event){
        //System.out.println("LOGGED IN BROS");
        FallenData cap = FallenData.get(event.getEntity());

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(event.getEntity(), new SyncClientCapMsg(event.getEntity().getUUID(), cap.writeNBT()));
        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new SyncConfigMsg(ReviveMeConfig.serialize()));
    }

    @SubscribeEvent
    public static void onStartTrack(PlayerEvent.StartTracking event){
        if (!(event.getTarget() instanceof Player targPlayer)) return;
        //System.out.println("Start tracking: " + event.getTarget().getDisplayName());

        //Grab and send their cap data
        FallenData cap = FallenData.get(targPlayer);
        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new SyncClientCapMsg(event.getTarget().getUUID(), cap.writeNBT()));
    }

    @SubscribeEvent
    public static void onWorldJoin(EntityJoinLevelEvent event){
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof Player player)) return;

        FallenData cap = FallenData.get(player);

        PacketDistributor.sendToPlayer((ServerPlayer) event.getEntity(), new SyncClientCapMsg(event.getEntity().getUUID(), cap.writeNBT()));
    }
}