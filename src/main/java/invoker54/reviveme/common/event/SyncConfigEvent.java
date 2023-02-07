package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SyncConfigEvent {

    @SubscribeEvent
    public static void onConfigChanged(final ModConfigEvent eventConfig){
        //System.out.println("What's the config type? " + eventConfig.getConfig().getType());

        if(eventConfig.getConfig().getSpec() == ReviveMeConfig.COMMON_SPEC){
            //System.out.println("SYNCING CONFIG SHTUFF");
            ReviveMeConfig.bakeConfig();
        }
    }

}
