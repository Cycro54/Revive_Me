package invoker54.reviveme.init;

import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.keybind.CustomKeybind;
import invoker54.invocore.common.MathUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.CallForHelpMsg;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;


@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyInit {
    public static final Logger LOGGER = LogManager.getLogger();

    public static CustomKeybind callForHelpKey;

    @SubscribeEvent
    public static void initializeKeys(FMLClientSetupEvent event){
        callForHelpKey = new CustomKeybind("callForHelpKey", GLFW.GLFW_KEY_R, ReviveMe.MOD_ID,
                (action)->{
                    if (action != GLFW.GLFW_PRESS) return;
                    if (ClientUtil.mC.screen != null) return;
                    FallenCapability cap =  FallenCapability.GetFallCap(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    if (cap.callForHelpCooldown() < 1) return;

                    float pitch = MathUtil.randomFloat(0.8F, 1.0F);
                    float volume = MathUtil.randomFloat(0.8F, 1.0F);

                    ClientUtil.getPlayer().playSound(SoundInit.CALL_FOR_HELP, volume, pitch);

                    NetworkHandler.INSTANCE.sendToServer(new CallForHelpMsg());
                });
    }
}
