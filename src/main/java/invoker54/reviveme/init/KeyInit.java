package invoker54.reviveme.init;

import invoker54.invocore.client.keybind.CustomKeybind;
import invoker54.invocore.client.keybind.KeybindsInit;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.event.CallForHelpEvent;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.ReviveMathUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.BeginReviveMsg;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;


@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyInit {
    public static final ModLogger LOGGER = ModLogger.getLogger(KeyInit.class, ReviveMeConfig.debugMode);

    public static CustomKeybind callForHelpKey;

    public static CustomKeybind leftOption;
    public static CustomKeybind rightOption;
    public static CustomKeybind tooltip;

    @SubscribeEvent
    public static void initializeKeys(FMLClientSetupEvent event) {
        callForHelpKey = KeybindsInit.addBind(new CustomKeybind("callForHelpKey", GLFW.GLFW_KEY_R, ReviveMe.MOD_ID,
                (action) -> {
                    if (action != GLFW.GLFW_PRESS) return;
                    if (ClientUtil.mC.screen != null) return;
                    FallenCapability cap = FallenCapability.get(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    boolean isSneaking = ClientUtil.mC.player.isCrouching();
                    if (!isSneaking && cap.isCallingForHelp()) return;

                    CallForHelpEvent.sendCallToServer(isSneaking, cap.isCallingForHelp());
                }));

        leftOption = KeybindsInit.addBind(new CustomKeybind(new KeyBinding("key." + ReviveMe.MOD_ID + "." + "leftOption",
                KeyConflictContext.GUI, InputMappings.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, "key.category." + ReviveMe.MOD_ID),
                (action) -> {
                    if (action == GLFW.GLFW_REPEAT) return;
                    if (ClientUtil.mC.screen != null) return;
                    FallenCapability cap = FallenCapability.get(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    boolean isClick = action == GLFW.GLFW_PRESS;

                    if (ClientUtil.mC.player.isCrouching() && isClick){
                        FallenItemScreenEvent.switchReviveScreens();
                        return;
                    }
                    else if (isClick && FallenItemScreenEvent.isItemScreenActive){
                        FallenItemScreenEvent.selectedPage = (int) ReviveMathUtil
                                .clampLoop(FallenItemScreenEvent.selectedPage + 1, 0, FallenItemScreenEvent.pageList.size()-1);
                    }

                    VanillaKeybindHandler.attackHeld = isClick;
                }));
        ClientRegistry.registerKeyBinding(leftOption.keyBind);

        rightOption = KeybindsInit.addBind(new CustomKeybind(new KeyBinding("key." + ReviveMe.MOD_ID + "." + "rightOption",
                KeyConflictContext.GUI, InputMappings.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_2, "key.category." + ReviveMe.MOD_ID),
                (action) -> {
                    if (action == GLFW.GLFW_REPEAT) return;
                    if (ClientUtil.mC.screen != null) return;
                    FallenCapability cap = FallenCapability.get(ClientUtil.getPlayer());
                    VanillaKeybindHandler.useHeld = action == GLFW.GLFW_PRESS;

                    if (!VanillaKeybindHandler.useHeld) return;
                    Entity crossHairEntity = ClientUtil.mC.crosshairPickEntity;
                    if (!(crossHairEntity instanceof PlayerEntity)) return;
                    if (!FallenCapability.get((LivingEntity) crossHairEntity).isFallen());

                    NetworkHandler.INSTANCE.sendToServer(new BeginReviveMsg(crossHairEntity.getStringUUID()));
                }));
        ClientRegistry.registerKeyBinding(rightOption.keyBind);

        tooltip = KeybindsInit.addBind(new CustomKeybind(new KeyBinding("key." + ReviveMe.MOD_ID + "." + "tooltip",
                KeyConflictContext.GUI, KeyModifier.SHIFT, InputMappings.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.category." + ReviveMe.MOD_ID),
                (action) -> {}));
        ClientRegistry.registerKeyBinding(tooltip.keyBind);
    }
}