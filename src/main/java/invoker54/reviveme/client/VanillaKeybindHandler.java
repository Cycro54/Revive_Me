package invoker54.reviveme.client;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VanillaKeybindHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static List<KeyMapping> vanillaKeyMappings;
    private static List<KeyMapping> movementMappings;
    private static final Map<String, KeyMapping> foundBindings = new HashMap<>();
    public static boolean attackHeld = false;
    public static boolean useHeld = false;
    public static boolean overrideKeyblock = false;

    public static void initializeKeybindList(){
//        LOGGER.error("Vanilla key mappings was null! populating...");
        Options options = ClientUtil.getMinecraft().options;

        vanillaKeyMappings = List.of(ArrayUtils.addAll(new KeyMapping[]{
                /*options.keyAttack, options.keyUse,*/ options.keyUp, options.keyLeft, options.keyDown, options.keyRight, options.keyJump, options.keyShift, options.keySprint, options.keyDrop, options.keyInventory, options.keyChat, options.keyPlayerList, options.keyPickItem, options.keyCommand, options.keySocialInteractions, options.keyScreenshot, options.keyTogglePerspective, options.keySmoothCamera, options.keyFullscreen, options.keySpectatorOutlines, options.keySwapOffhand, options.keySaveHotbarActivator, options.keyLoadHotbarActivator, options.keyAdvancements}, options.keyHotbarSlots));

        movementMappings = List.of(options.keyLeft, options.keyUp, options.keyRight, options.keyDown);
//        LOGGER.error("Vanilla keybindings new size: " + vanillaKeyMappings.size());
    }

    public static boolean isVanillaKeybind(KeyMapping keyMapping){
        if (vanillaKeyMappings == null) initializeKeybindList();
        if (keyMapping == null) return false;

        return vanillaKeyMappings.contains(keyMapping);
    }

    public static boolean isMovementKeybind(KeyMapping keyMapping){
        if (vanillaKeyMappings == null) initializeKeybindList();
        if (keyMapping == null) return false;

        return movementMappings.contains(keyMapping);
    }

    public static InputConstants.Key getKey(KeyMapping keyBinding) {
        overrideKeyblock = true;
        InputConstants.Key key = keyBinding.getKey();
        overrideKeyblock = false;
        return key;
    }

    public static KeyMapping getOrCreateKey(String name) {
        KeyMapping keyBinding = foundBindings.get(name);
        if (keyBinding != null) return keyBinding;

        //Second go through all keybinds
        for (KeyMapping knownBinding : ClientUtil.getMinecraft().options.keyMappings) {
            if (!knownBinding.getName().equals(name)) continue;
            foundBindings.put(name, knownBinding);
//            LOGGER.warn("FOUND VANILLA MATCH: " + name);
            return knownBinding;
        }

//        LOGGER.warn("Making fake MATCH: " + name);

        KeyMapping fakeBinding = new KeyMapping(name, GLFW.GLFW_KEY_A, "fake");
        foundBindings.put(name, fakeBinding);
        return fakeBinding;
    }

    public static boolean isAllowedKeybind(KeyMapping keybinding){
        Player player = ClientUtil.getPlayer();
        if (keybinding == KeyInit.callForHelpKey.keyBind) return true;
        if (keybinding == KeyInit.leftOption.keyBind) return true;
        if (keybinding == KeyInit.rightOption.keyBind) return true;

        boolean isVanilla = VanillaKeybindHandler.isVanillaKeybind(keybinding);
        boolean isKeyInventory = keybinding == ClientUtil.getMinecraft().options.keyInventory;
        boolean isKeyDrop = keybinding == ClientUtil.getMinecraft().options.keyDrop;
        boolean isKeySwapOffhand = keybinding == ClientUtil.getMinecraft().options.keySwapOffhand;
        boolean isSwapOrDrop = isKeyDrop || isKeySwapOffhand;
        ItemStack mainStack = player.getMainHandItem();
        boolean isSacrificialItem = FallenData.get(player).isSacrificialItem(mainStack);
        ReviveMeConfig.INTERACT_WITH_INVENTORY inventoryRule = ReviveMeConfig.interactWithInventory;
        boolean isAllowedKeybind = false;

        for (String s : ReviveMeConfig.allowedKeybinds){
            if (s.isEmpty()) continue;
            if (!keybinding.getName().contains(s)) continue;
            isAllowedKeybind = true;
            break;
        }

        if (!isVanilla && !isAllowedKeybind) return false;
        else if (inventoryRule == ReviveMeConfig.INTERACT_WITH_INVENTORY.NO && (isKeyInventory || isSwapOrDrop)) return false;
        else if (inventoryRule == ReviveMeConfig.INTERACT_WITH_INVENTORY.LOOK_ONLY && isSwapOrDrop) return false;
        else if (isSwapOrDrop && isSacrificialItem) return false;

        return true;
    }

    public static boolean canBeDown(KeyMapping keyBinding){
        if (ClientUtil.getWorld() == null) return true;
        Player player = ClientUtil.getPlayer();
        if (player == null) return true;
        FallenData cap = FallenData.get(player);
        if (!cap.isFallen()) return true;

        if (!isAllowedKeybind(keyBinding)) return false;
        if ((!ReviveMeConfig.canMove && isMovementKeybind(keyBinding))) return false;



        //This is for jumping
        if (keyBinding.equals(ClientUtil.getMinecraft().options.keyJump)) {
            switch (ReviveMeConfig.canJump) {
                case YES:
                    break;
                case LIQUID_ONLY:
                    if (player.level().getFluidState(player.blockPosition()).isEmpty()) return false;
                    break;
                case NO:
                    return false;
            }
        }

        return true;
    }
}
