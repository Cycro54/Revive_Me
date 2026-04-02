package invoker54.reviveme.compatibility.controllable.client.events;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.event.ControllerEvents;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mrcrayfish.controllable.client.binding.ButtonBindings.*;

public class ControllableModEvents {
    private static final ModLogger LOGGERT = ModLogger.getLogger(ControllableModEvents.class, ReviveMeConfig.debugMode);

    public static List<ButtonBinding> revive_Me_vanillaBindingList = new ArrayList<>();

    public static boolean isPlayerDown(){
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


        revive_Me_vanillaBindingList.addAll(Arrays.asList(SCROLL_LEFT, SCROLL_RIGHT, PAUSE_GAME, OPEN_INVENTORY, CLOSE_INVENTORY,
                NEXT_CREATIVE_TAB, PREVIOUS_CREATIVE_TAB, NEXT_RECIPE_TAB, PREVIOUS_RECIPE_TAB, NAVIGATE_UP, NAVIGATE_DOWN,
                NAVIGATE_LEFT, NAVIGATE_RIGHT, PICKUP_ITEM, QUICK_MOVE, SPLIT_STACK, DEBUG_INFO, RADIAL_MENU));
    }
}
