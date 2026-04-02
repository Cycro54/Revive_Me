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
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.BeginReviveMsg;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;


@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class KeyInit {
    public static final ModLogger LOGGER = ModLogger.getLogger(KeyInit.class, ReviveMeConfig.debugMode);

    public static CustomKeybind callForHelpKey;

    public static CustomKeybind leftOption;
    public static CustomKeybind rightOption;
    public static CustomKeybind tooltip;

    public static KeyMapping.Category mainCategory;

    @SubscribeEvent
    public static void initializeKeys(FMLClientSetupEvent event) {
        mainCategory = CustomKeybind.getCategory(ReviveMe.MOD_ID, ReviveMe.MOD_ID);
        callForHelpKey = KeybindsInit.addBind(new CustomKeybind("callForHelpKey", GLFW.GLFW_KEY_R, ReviveMe.MOD_ID,
                (action) -> {
                    if (callForHelpKey.keyBind.getKey().getType() == InputConstants.Type.MOUSE && !KeyEvents.isPostMouse)
                        return;
                    if (action != GLFW.GLFW_PRESS) return;
                    if (ClientUtil.getMinecraft().screen != null) return;
                    FallenData cap = FallenData.get(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    boolean isSneaking = ClientUtil.getMinecraft().player.isShiftKeyDown();
                    if (!isSneaking && cap.isCallingForHelp()) return;

                    CallForHelpEvent.sendCallToServer(isSneaking, cap.isCallingForHelp());
                }));
        callForHelpKey.keyBind.setKeyConflictContext(new CustomContext());

        leftOption = KeybindsInit.addBind(new CustomKeybind(new KeyMapping("key." + ReviveMe.MOD_ID + "." + "leftOption",
                new CustomContext(), InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_1, mainCategory),
                (action) -> {
                    if (leftOption.keyBind.getKey().getType() == InputConstants.Type.MOUSE && !KeyEvents.isPostMouse)
                        return;
//                    LOGGER.warn("I am here: " + action);
                    if (action == GLFW.GLFW_REPEAT) return;
                    if (ClientUtil.getMinecraft().screen != null) return;
                    FallenData cap = FallenData.get(ClientUtil.getPlayer());
                    if (!cap.isFallen()) return;
                    boolean isClick = action == GLFW.GLFW_PRESS;
//                    LOGGER.error("THIS IS RUNNING TOOO: " + (isClick));


                    if (ClientUtil.getMinecraft().player.isShiftKeyDown() && isClick) {
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
                new CustomContext(), InputConstants.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_2, mainCategory),
                (action) -> {
                    if (rightOption.keyBind.getKey().getType() == InputConstants.Type.MOUSE && !KeyEvents.isPostMouse)
                        return;
                    if (action == GLFW.GLFW_REPEAT) return;
                    if (ClientUtil.getMinecraft().screen != null) return;
                    FallenData cap = FallenData.get(ClientUtil.getPlayer());
                    VanillaKeybindHandler.useHeld = action == GLFW.GLFW_PRESS;

                    if (!VanillaKeybindHandler.useHeld) return;
                    Entity crossHairEntity = ClientUtil.getMinecraft().crosshairPickEntity;
                    if (!(crossHairEntity instanceof Player)) return;
                    if (!FallenData.get((LivingEntity) crossHairEntity).isFallen()) ;

                    ClientPacketDistributor.sendToServer(new BeginReviveMsg(crossHairEntity.getStringUUID()));
                }));
//        ClientRegistry.registerKeyBinding(rightOption.keyBind);

        tooltip = KeybindsInit.addBind(new CustomKeybind(new KeyMapping("key." + ReviveMe.MOD_ID + "." + "tooltip",
                KeyConflictContext.GUI, KeyModifier.SHIFT, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, mainCategory),
                (action) -> {
                    if (tooltip.keyBind.getKey().getType() == InputConstants.Type.MOUSE && !KeyEvents.isPostMouse)
                        return;
                    if (action == GLFW.GLFW_PRESS) ReviveToolTipEvents.isKeybindDown = true;
                    if (action == GLFW.GLFW_RELEASE) ReviveToolTipEvents.isKeybindDown = false;
                }));
//        ClientRegistry.registerKeyBinding(tooltip.keyBind);
    }

    public static class CustomContext implements IKeyConflictContext{

        @Override
        public boolean isActive() {
            if (ClientUtil.getPlayer() == null) return false;
            FallenData data = FallenData.get(ClientUtil.getPlayer());
            if (!data.isFallen() && ClientUtil.getMinecraft().crosshairPickEntity == null) return false;
            return true;
        }

        @Override
        public boolean conflicts(IKeyConflictContext iKeyConflictContext) {
            return false;
        }
    }
}
