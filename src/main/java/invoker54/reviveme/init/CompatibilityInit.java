package invoker54.reviveme.init;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.compatibility.controllable.client.events.ControllableModEvents;
import net.neoforged.fml.ModList;

public class CompatibilityInit {
    private static final ModLogger LOGGER = ModLogger.getLogger(CompatibilityInit.class, ReviveMeConfig.debugMode);

    public static void ControllableInit(){
        if (ModList.get().isLoaded("controllable")){
            LOGGER.error("CONTROLLABLE WAS FOUND");
            ControllableModEvents.init();
//            MinecraftForge.EVENT_BUS.register(new ControllableModEvents());
        }
        else {
            LOGGER.error("CONTROLLABLE WAS NOT FOUND");
        }

    }
}
