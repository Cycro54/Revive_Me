package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class KeyEvents {
    public static boolean isPostMouse = false;

    @SubscribeEvent
    public static void changeMouseModePre(InputEvent.MouseButton.Pre event){
        isPostMouse = false;
    }

    @SubscribeEvent
    public static void changeMouseModePost(InputEvent.MouseButton.Post event){
        isPostMouse = true;
    }
}
