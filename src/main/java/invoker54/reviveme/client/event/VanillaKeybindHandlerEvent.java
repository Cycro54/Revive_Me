package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class VanillaKeybindHandlerEvent {

    @SubscribeEvent
    public static void onScreen(ScreenEvent.Opening event){
        VanillaKeybindHandler.attackHeld = false;
        VanillaKeybindHandler.useHeld = false;
    }
}
