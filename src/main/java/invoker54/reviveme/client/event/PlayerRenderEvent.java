package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class PlayerRenderEvent {
    //TODO: REMOVE THIS IF IT WORKS OUT!!!

    //This will make the player look like they are crouching (on client side ONLY)
//    @SubscribeEvent
//    public static void onRender(RenderPlayerEvent.Pre event){
//        if (FallenCapability.GetFallCap(event.getEntity()).isFallen()){
//            PlayerModel<AbstractClientPlayer> player = event.getRenderer().getModel();
//            if (ReviveMeConfig.fallenPose == ReviveMeConfig.FALLEN_POSE.CROUCH) player.crouching = true;
//        }
//    }
}
