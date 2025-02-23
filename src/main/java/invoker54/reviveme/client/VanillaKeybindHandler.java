package invoker54.reviveme.client;

import invoker54.invocore.client.ClientUtil;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class VanillaKeybindHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static List<KeyBinding> vanillaKeyBindings;
    private static List<KeyBinding> movementMappings;
    public static boolean attackHeld = false;
    public static boolean useHeld = false;

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
}
