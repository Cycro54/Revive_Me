package invoker54.reviveme.init;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.keybind.CustomKeybind;
import invoker54.invocore.client.keybind.KeybindsInit;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.event.CallForHelpEvent;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.client.event.KeyEvents;
import invoker54.reviveme.client.event.ReviveToolTipEvents;
import invoker54.reviveme.common.ReviveMathUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.BeginReviveMsg;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
                    if (callForHelpKey.keyBind.getKey().getType() == InputConstants.Type.MOUSE && !KeyEvents.isPostMouse)
                        return;
                    if (action != GLFW.GLFW_PRESS) return;
                    if (ClientUtil.getMinecraft().screen != null) return;
                    FallenCapability cap = FallenCapability.get(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    boolean isSneaking = ClientUtil.getMinecraft().player.isCrouching();
                    if (!isSneaking && cap.isCallingForHelp()) return;

                    CallForHelpEvent.sendCallToServer(isSneaking, cap.isCallingForHelp());
                }));

        leftOption = KeybindsInit.addBind(new CustomKeybind(new KeyMapping("key." + ReviveMe.MOD_ID + "." + "leftOption",
                KeyConflictContext.GUI, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, "key.category." + ReviveMe.MOD_ID),
                (action) -> {
                    if (leftOption.keyBind.getKey().getType() == InputConstants.Type.MOUSE && !KeyEvents.isPostMouse)
                        return;
                    if (action == GLFW.GLFW_REPEAT) return;
                    if (ClientUtil.getMinecraft().screen != null) return;
                    FallenCapability cap = FallenCapability.get(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    boolean isClick = action == GLFW.GLFW_PRESS;

                    if (ClientUtil.getMinecraft().player.isCrouching() && isClick) {
                        FallenItemScreenEvent.switchReviveScreens();
                        return;
                    } else if (isClick && FallenItemScreenEvent.isItemScreenActive) {
                        FallenItemScreenEvent.selectedPage = (int) ReviveMathUtil
                                .clampLoop(FallenItemScreenEvent.selectedPage + 1, 0, FallenItemScreenEvent.pageList.size() - 1);
                    }

                    VanillaKeybindHandler.attackHeld = isClick;
                }));
//        ClientRegistry.registerKeyBinding(leftOption.keyBind);
//        ClientUtil.getMinecraft().options.keyMappings = ArrayUtils.add(options.keyMappings, key);

        rightOption = KeybindsInit.addBind(new CustomKeybind(new KeyMapping("key." + ReviveMe.MOD_ID + "." + "rightOption",
                KeyConflictContext.GUI, InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_2, "key.category." + ReviveMe.MOD_ID),
                (action) -> {
                    if (rightOption.keyBind.getKey().getType() == InputConstants.Type.MOUSE && !KeyEvents.isPostMouse)
                        return;
                    if (action == GLFW.GLFW_REPEAT) return;
                    if (ClientUtil.getMinecraft().screen != null) return;
                    FallenCapability cap = FallenCapability.get(ClientUtil.getPlayer());
                    VanillaKeybindHandler.useHeld = action == GLFW.GLFW_PRESS;

                    if (!VanillaKeybindHandler.useHeld) return;
                    Entity crossHairEntity = ClientUtil.getMinecraft().crosshairPickEntity;
                    if (!(crossHairEntity instanceof Player)) return;
                    if (!FallenCapability.get((LivingEntity) crossHairEntity).isFallen()) ;

                    NetworkHandler.INSTANCE.sendToServer(new BeginReviveMsg(crossHairEntity.getStringUUID()));
                }));
//        ClientRegistry.registerKeyBinding(rightOption.keyBind);

        tooltip = KeybindsInit.addBind(new CustomKeybind(new KeyMapping("key." + ReviveMe.MOD_ID + "." + "tooltip",
                KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, "key.category." + ReviveMe.MOD_ID),
                (action) -> {
                    if (tooltip.keyBind.getKey().getType() == InputConstants.Type.MOUSE && !KeyEvents.isPostMouse)
                        return;
                    if (action == GLFW.GLFW_PRESS) ReviveToolTipEvents.isKeybindDown = true;
                    if (action == GLFW.GLFW_RELEASE) ReviveToolTipEvents.isKeybindDown = false;
                }));
//        ClientRegistry.registerKeyBinding(tooltip.keyBind);
    }
}
