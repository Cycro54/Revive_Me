package invoker54.reviveme.client.event;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.util.MathUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.network.payload.CallForHelpMsg;
import invoker54.reviveme.init.SoundInit;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class CallForHelpEvent {

    @SubscribeEvent
    public static void callTickEvent(ClientTickEvent.Pre event){
        
        if (ClientUtil.getPlayer() == null) return;
        FallenData cap = FallenData.get(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;
        if (!cap.isCallToggled()) return;
        if (cap.isCallingForHelp()) return;

        sendCallToServer(false, cap.isCallingForHelp());
    }

    public static void sendCallToServer(boolean isSneaking, boolean isCurrentlyCalling){
        PacketDistributor.sendToServer(new CallForHelpMsg(isSneaking));
        if (isCurrentlyCalling) return;

        FallenData.get(ClientUtil.getPlayer()).callForHelp(isSneaking);

        float pitch = MathUtil.randomFloat(0.8F, 1.0F);
        float volume = 4;

        ClientUtil.getPlayer().playSound(SoundInit.CALL_FOR_HELP, volume, pitch);
    }
}
