package invoker54.reviveme.client;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.event.FallScreenEvent;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VanillaKeybindHandler {
    private static final ModLogger LOGGER = ModLogger.getLogger(VanillaKeybindHandler.class, ReviveMeConfig.debugMode);
    private static List<KeyBinding> vanillaKeyBindings;
    private static List<KeyBinding> movementMappings;
    private static final Map<String, KeyBinding> foundBindings = new HashMap<>();
    public static boolean attackHeld = false;
    public static boolean useHeld = false;
    public static boolean overrideKeyblock = false;

    public static void initializeKeybindList(){
        GameSettings options = ClientUtil.mC.options;

        vanillaKeyBindings = Arrays.asList(ArrayUtils.addAll(new KeyBinding[]{
                /*options.keyAttack, options.keyUse, */ options.keyUp, options.keyLeft, options.keyDown, options.keyRight, options.keyJump, options.keyShift, options.keySprint, options.keyDrop, options.keyInventory, options.keyChat, options.keyPlayerList, options.keyPickItem, options.keyCommand, options.keySocialInteractions, options.keyScreenshot, options.keyTogglePerspective, options.keySmoothCamera, options.keyFullscreen, options.keySpectatorOutlines, options.keySwapOffhand, options.keySaveHotbarActivator, options.keyLoadHotbarActivator, options.keyAdvancements}, (KeyBinding[]) options.keyHotbarSlots));

        movementMappings = Arrays.asList(options.keyShift, options.keyLeft, options.keyUp, options.keyRight, options.keyDown);
//        LOGGER.error("Vanilla keybindings new size: " + vanillaKeyBindings.size());
    }

    public static boolean isVanillaKeybind(KeyBinding KeyBinding){
        if (vanillaKeyBindings == null) initializeKeybindList();
        if (KeyBinding == null) return false;

        return vanillaKeyBindings.contains(KeyBinding);
    }

    public static boolean isMovementKeybind(KeyBinding KeyBinding){
        if (vanillaKeyBindings == null) initializeKeybindList();
        if (KeyBinding == null) return false;

        return movementMappings.contains(KeyBinding);
    }

    public static InputMappings.Input getKey(KeyBinding keyBinding){
        overrideKeyblock = true;
        InputMappings.Input key = keyBinding.getKey();
        overrideKeyblock = false;
        return key;
    }

    public static KeyBinding getOrCreateKey(String name) {
        KeyBinding keyBinding = foundBindings.get(name);
        if (keyBinding != null) return keyBinding;

        //Second go through all keybinds
        for (KeyBinding knownBinding : ClientUtil.mC.options.keyMappings) {
            if (!knownBinding.getName().equals(name)) continue;
            foundBindings.put(name, knownBinding);
            return knownBinding;
        }

        KeyBinding fakeBinding = new KeyBinding(name, GLFW.GLFW_KEY_A, "fake");
        foundBindings.put(name, fakeBinding);
        return fakeBinding;
    }

    public static boolean isAllowedKeybind(KeyBinding keybinding){
        PlayerEntity player = ClientUtil.getPlayer();
        if (keybinding == KeyInit.callForHelpKey.keyBind) return true;
        if (keybinding == KeyInit.leftOption.keyBind) return true;
        if (keybinding == KeyInit.rightOption.keyBind) return true;
        if (keybinding == KeyInit.toggleGUIKey.keyBind) return true;

        boolean isVanilla = VanillaKeybindHandler.isVanillaKeybind(keybinding);
        boolean isKeyInventory = keybinding == ClientUtil.mC.options.keyInventory;
        boolean isKeyDrop = keybinding == ClientUtil.mC.options.keyDrop;
        boolean isKeySwapOffhand = keybinding == ClientUtil.mC.options.keySwapOffhand;
        boolean isSwapOrDrop = isKeyDrop || isKeySwapOffhand;
        ItemStack mainStack = player.getMainHandItem();
        boolean isSacrificialItem = FallenCapability.get(player).isSacrificialItem(mainStack);
        ReviveMeConfig.INTERACT_WITH_INVENTORY inventoryRule = ReviveMeConfig.interactWithInventory;
        boolean isAllowedKeybind = false;

        for (String s : ReviveMeConfig.allowedKeybinds){
            if (s.isEmpty()) continue;
            boolean affectedByGUIToggle = s.contains(";");
            if (affectedByGUIToggle) s = s.replace(";", "");
            if (!keybinding.getName().contains(s)) continue;
            if (affectedByGUIToggle && !FallScreenEvent.guiToggled) continue;
            isAllowedKeybind = true;
            break;
        }

        if (!isVanilla && !isAllowedKeybind) return false;
        else if (inventoryRule == ReviveMeConfig.INTERACT_WITH_INVENTORY.NO && (isKeyInventory || isSwapOrDrop)) return false;
        else if (inventoryRule == ReviveMeConfig.INTERACT_WITH_INVENTORY.LOOK_ONLY && isSwapOrDrop) return false;
        else if (isSwapOrDrop && isSacrificialItem) return false;

        return true;
    }

    public static boolean canBeDown(KeyBinding keyBinding){
        if (ClientUtil.getWorld() == null) return true;
        PlayerEntity player = ClientUtil.getPlayer();
        if (player == null) return true;
        FallenCapability cap = FallenCapability.get(player);
        if (!cap.isFallen()) return true;

        if (!isAllowedKeybind(keyBinding)) return false;
        if ((!ReviveMeConfig.canMove && isMovementKeybind(keyBinding))) return false;



        //This is for jumping
        if (keyBinding.equals(ClientUtil.mC.options.keyJump)) {
            switch (ReviveMeConfig.canJump) {
                case YES:
                    break;
                case LIQUID_ONLY:
                    if (player.level.getFluidState(player.blockPosition()).isEmpty()) return false;
                    break;
                case NO:
                    return false;
            }
        }

        return true;
    }
}
