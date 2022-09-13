package invoker54.reviveme.init;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityTypesInit {

    public static void registerCaps(){
        //Fallen capability
        CapabilityManager.INSTANCE.register(FallenCapability.class, new FallenCapability.FallenNBTStorage(), FallenCapability::new);
    }
}
