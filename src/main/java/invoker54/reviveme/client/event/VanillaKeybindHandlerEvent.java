package invoker54.reviveme.client.event;

import invoker54.reviveme.client.VanillaKeybindHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class VanillaKeybindHandlerEvent {

    @SubscribeEvent
    public static void onScreen(GuiOpenEvent event){
        VanillaKeybindHandler.attackHeld = false;
        VanillaKeybindHandler.useHeld = false;
    }
}
