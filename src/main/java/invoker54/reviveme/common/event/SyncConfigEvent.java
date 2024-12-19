package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.SyncConfigMsg;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class SyncConfigEvent {

    @SubscribeEvent
    public static void onUpdateConfig(ServerTickEvent.Post event){
        if (ReviveMeConfig.isDirty()){
            //Then finally send the config data to all players
            PacketDistributor.sendToAllPlayers(new SyncConfigMsg(ReviveMeConfig.serialize()));

            ReviveMeConfig.markDirty(false);
        }
    }

}
