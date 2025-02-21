package invoker54.reviveme.client;

import invoker54.invocore.client.ClientUtil;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class VanillaKeybindHandler {
    private static final Logger LOGGER = LogManager.getLogger();
    private static List<KeyMapping> vanillaKeyMappings;
    private static List<KeyMapping> movementMappings;
    public static boolean attackHeld = false;
    public static boolean useHeld = false;

    public static void initializeKeybindList(){
//        LOGGER.error("Vanilla key mappings was null! populating...");
        Options options = ClientUtil.getMinecraft().options;

        vanillaKeyMappings = List.of(ArrayUtils.addAll(new KeyMapping[]{
                /*options.keyAttack, options.keyUse,*/ options.keyUp, options.keyLeft, options.keyDown, options.keyRight, options.keyJump, options.keyShift, options.keySprint, /*options.keyDrop,*/ options.keyInventory, options.keyChat, options.keyPlayerList, options.keyPickItem, options.keyCommand, options.keySocialInteractions, options.keyScreenshot, options.keyTogglePerspective, options.keySmoothCamera, options.keyFullscreen, options.keySpectatorOutlines, options.keySwapOffhand, options.keySaveHotbarActivator, options.keyLoadHotbarActivator, options.keyAdvancements}, options.keyHotbarSlots));

        movementMappings = List.of(options.keyShift, options.keyLeft, options.keyUp, options.keyRight, options.keyDown);
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
}
