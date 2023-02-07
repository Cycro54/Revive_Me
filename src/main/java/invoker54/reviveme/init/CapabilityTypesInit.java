package invoker54.reviveme.init;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = ReviveMe.MOD_ID)
public class CapabilityTypesInit {

    @SubscribeEvent
    public static void registerCaps(RegisterCapabilitiesEvent event){
        event.register(FallenCapability.class);
    }
}
