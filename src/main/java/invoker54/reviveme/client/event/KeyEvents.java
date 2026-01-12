package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
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
