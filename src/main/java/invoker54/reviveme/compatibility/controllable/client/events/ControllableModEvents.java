package invoker54.reviveme.compatibility.controllable.client.events;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.event.ControllerEvents;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mrcrayfish.controllable.client.binding.ButtonBindings.*;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.CLOSE_INVENTORY;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.DEBUG_INFO;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.NAVIGATE_DOWN;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.NAVIGATE_LEFT;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.NAVIGATE_RIGHT;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.NAVIGATE_UP;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.NEXT_CREATIVE_TAB;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.NEXT_RECIPE_TAB;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.OPEN_INVENTORY;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.PICKUP_ITEM;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.PREVIOUS_CREATIVE_TAB;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.PREVIOUS_RECIPE_TAB;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.QUICK_MOVE;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.RADIAL_MENU;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.SPLIT_STACK;
import static com.mrcrayfish.controllable.client.binding.ButtonBindings.UNPAUSE_GAME;

public class ControllableModEvents {
    private static final ModLogger LOGGERT = ModLogger.getLogger(ControllableModEvents.class, ReviveMeConfig.debugMode);
    public static boolean isControllableLoaded = false;

    public static List<ButtonBinding> revive_Me_vanillaBindingList = new ArrayList<>();


    public static boolean isPlayerDown(){
        if (ClientUtil.getWorld() == null) return false;
        Player player = ClientUtil.getPlayer();
        if (player == null) return false;
        FallenData cap = FallenData.get(player);
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
        isControllableLoaded = true;

        revive_Me_vanillaBindingList.addAll(Arrays.asList(SCROLL_HOTBAR_LEFT, SCROLL_HOTBAR_RIGHT, PAUSE_GAME, UNPAUSE_GAME, OPEN_INVENTORY, CLOSE_INVENTORY,
                NEXT_CREATIVE_TAB, PREVIOUS_CREATIVE_TAB, NEXT_RECIPE_TAB, PREVIOUS_RECIPE_TAB, NAVIGATE_UP, NAVIGATE_DOWN,
                NAVIGATE_LEFT, NAVIGATE_RIGHT, PICKUP_ITEM, QUICK_MOVE, SPLIT_STACK, DEBUG_INFO, RADIAL_MENU));
    }

}
