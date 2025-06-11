package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class PlayerRenderEvent {
    //This will make the player look like they are crouching (on client side ONLY)
    @SubscribeEvent
    public static void onRender(RenderPlayerEvent.Pre event) {
        if (FallenCapability.GetFallCap(event.getEntity()).isFallen()) {
            PlayerModel<AbstractClientPlayer> player = event.getRenderer().getModel();
            if (ReviveMeConfig.fallenPose == ReviveMeConfig.FALLEN_POSE.CROUCH) player.crouching = true;
        }
    }
}
