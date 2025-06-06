package invoker54.reviveme.client;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

public class VanillaKeybindHandler {
    private static final ModLogger LOGGER = ModLogger.getLogger(VanillaKeybindHandler.class, ReviveMeConfig.debugMode);
    private static List<KeyBinding> vanillaKeyBindings;
    private static List<KeyBinding> movementMappings;
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
}
