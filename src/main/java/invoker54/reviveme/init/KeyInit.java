package invoker54.reviveme.init;

import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.keybind.CustomKeybind;
import invoker54.invocore.common.MathUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.network.payload.CallForHelpMsg;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;


@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class KeyInit {
    public static final Logger LOGGER = LogManager.getLogger();

    public static CustomKeybind callForHelpKey;

    @SubscribeEvent
    public static void initializeKeys(FMLClientSetupEvent event){
        callForHelpKey = new CustomKeybind("callForHelpKey", GLFW.GLFW_KEY_R, ReviveMe.MOD_ID,
                (action)->{
                    if (action != GLFW.GLFW_PRESS) return;
                    if (ClientUtil.getMinecraft().screen != null) return;
                    FallenData cap =  FallenData.get(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    if (cap.callForHelpCooldown() < 1) return;

                    float pitch = MathUtil.randomFloat(0.8F, 1.0F);
                    float volume = MathUtil.randomFloat(0.8F, 1.0F);

                    ClientUtil.getPlayer().playSound(SoundInit.CALL_FOR_HELP, volume, pitch);

                    PacketDistributor.sendToServer(new CallForHelpMsg());
                });
    }
}
