package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncConfigMsg;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class SyncConfigEvent {
    @SubscribeEvent
    public static void onUpdateConfig(TickEvent.ServerTickEvent event){
        if (event.type == TickEvent.Type.CLIENT) return;
        if (event.phase == TickEvent.Phase.START) return;
        if (ReviveMeConfig.isDirty()){
            //Then finally send the config data to all players
            NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncConfigMsg(ReviveMeConfig.serialize()));

            ReviveMeConfig.markDirty(false);
        }
    }
}
