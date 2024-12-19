package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class PlayerRenderEvent {
    //This will make the player look like they are crouching (on client side ONLY)
    @SubscribeEvent
    public static void onRender(RenderPlayerEvent.Pre event){
        if (FallenData.get(event.getEntity()).isFallen()){
            PlayerModel<AbstractClientPlayer> player = event.getRenderer().getModel();
            if (ReviveMeConfig.fallenPose == ReviveMeConfig.FALLEN_POSE.CROUCH) player.crouching = true;
        }
    }
}
