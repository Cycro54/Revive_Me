package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.Pose;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class PosChangeEvent {

    //This will make it seem like the player is crouching (though visually client-side it won't be.)
    @SubscribeEvent
    public static void onTick (TickEvent.PlayerTickEvent event){
        if(event.phase == TickEvent.Phase.START) return;

        if(FallenCapability.GetFallCap(event.player).isFallen())
            event.player.setPose(Pose.CROUCHING);
    }
}
