package invoker54.reviveme.compatibility.controllable.client.events;

import com.mrcrayfish.controllable.event.ControllerEvents;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.entity.player.Player;

public class ControllableModEvents {
    private static final ModLogger LOGGERT = ModLogger.getLogger(ControllableModEvents.class, ReviveMeConfig.debugMode);

    public static boolean isControllableLoaded = false;

    public static boolean isPlayerDown(){
        if (!isControllableLoaded) return false;
        if (ClientUtil.getWorld() == null) return false;
        Player player = ClientUtil.getPlayer();
        if (player == null) return false;
        FallenCapability cap = FallenCapability.get(player);
        return cap.isFallen();
    }

    public static ControllerEvents.UpdateMovement updateMovement(){

        return () -> {
            if (!isPlayerDown()) return false;
            return !ReviveMeConfig.canMove;
        };

    }

    public static void init() {
        ControllerEvents.UPDATE_MOVEMENT.register(ControllableModEvents.updateMovement());
    }
}
