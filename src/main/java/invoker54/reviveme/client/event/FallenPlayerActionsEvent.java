package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.InstaKillMsg;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class FallenPlayerActionsEvent {
    private static final Minecraft inst = Minecraft.getInstance();
    public static int timeHeld = 0;

    @SubscribeEvent
    public static void onAttack(InputEvent.ClickInputEvent event){
        if (FallenCapability.GetFallCap(inst.player).isFallen())
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void forceDeath(TickEvent.PlayerTickEvent event){
        if(event.side == LogicalSide.SERVER) return;

        if(!FallenCapability.GetFallCap(inst.player).isFallen()) return;

        if(event.phase == TickEvent.Phase.END) return;

        if(inst.options.keyUse.isDown()) {
            timeHeld++;

            if (timeHeld == 40) {
                NetworkHandler.INSTANCE.sendToServer(new InstaKillMsg(inst.player.getUUID()));
                //System.out.println("Who's about to die: " + inst.player.getDisplayName());
            }
        }
        else if (timeHeld != 0) timeHeld = 0;
    }
}
