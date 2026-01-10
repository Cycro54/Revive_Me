package invoker54.reviveme.client.event;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.MathUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.CallForHelpMsg;
import invoker54.reviveme.init.SoundInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class CallForHelpEvent {

    @SubscribeEvent
    public static void callTickEvent(TickEvent.ClientTickEvent event){
        if (event.phase == TickEvent.Phase.END) return;
        if (ClientUtil.getPlayer() == null) return;
        FallenCapability cap = FallenCapability.get(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;
        if (!cap.isCallToggled()) return;
        if (cap.isCallingForHelp()) return;

        sendCallToServer(false, cap.isCallingForHelp());
    }

    public static void sendCallToServer(boolean isSneaking, boolean isCurrentlyCalling){
        NetworkHandler.INSTANCE.sendToServer(new CallForHelpMsg(isSneaking));
        if (isCurrentlyCalling) return;

        FallenCapability.get(ClientUtil.getPlayer()).callForHelp(isSneaking);

        float pitch = MathUtil.randomFloat(0.8F, 1.0F);
        float volume = 4;

        ClientUtil.getPlayer().playSound(SoundInit.CALL_FOR_HELP, volume, pitch);
    }
}
