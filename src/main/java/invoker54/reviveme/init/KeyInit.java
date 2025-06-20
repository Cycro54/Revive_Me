package invoker54.reviveme.init;

import invoker54.invocore.client.keybind.CustomKeybind;
import invoker54.invocore.client.keybind.KeybindsInit;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.MathUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.CallForHelpMsg;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;


@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class KeyInit {
    public static final ModLogger LOGGER = ModLogger.getLogger(KeyInit.class, ReviveMeConfig.debugMode);

    public static CustomKeybind callForHelpKey;

    @SubscribeEvent
    public static void initializeKeys(FMLClientSetupEvent event){
        callForHelpKey = KeybindsInit.addBind(new CustomKeybind("callForHelpKey", GLFW.GLFW_KEY_R, ReviveMe.MOD_ID,
                (action) -> {
                    if (action != GLFW.GLFW_PRESS) return;
                    if (ClientUtil.getMinecraft().screen != null) return;
                    FallenData cap = FallenData.get(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    if (cap.callForHelpCooldown() < 1) return;

                    float pitch = MathUtil.randomFloat(0.8F, 1.0F);
                    float volume = 4;

                    ClientUtil.getPlayer().playSound(SoundInit.CALL_FOR_HELP, volume, pitch);

                    PacketDistributor.sendToServer(new CallForHelpMsg());
                }));
    }
}
