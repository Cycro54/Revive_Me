package invoker54.reviveme.compatibility.controllable.client.events;

import com.mrcrayfish.controllable.client.ButtonBinding;
import com.mrcrayfish.controllable.event.ControllerEvent;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mrcrayfish.controllable.client.ButtonBindings.*;
import static com.mrcrayfish.controllable.client.ButtonBindings.DEBUG_INFO;
import static com.mrcrayfish.controllable.client.ButtonBindings.NAVIGATE_DOWN;
import static com.mrcrayfish.controllable.client.ButtonBindings.NAVIGATE_LEFT;
import static com.mrcrayfish.controllable.client.ButtonBindings.NAVIGATE_RIGHT;
import static com.mrcrayfish.controllable.client.ButtonBindings.NAVIGATE_UP;
import static com.mrcrayfish.controllable.client.ButtonBindings.NEXT_CREATIVE_TAB;
import static com.mrcrayfish.controllable.client.ButtonBindings.NEXT_RECIPE_TAB;
import static com.mrcrayfish.controllable.client.ButtonBindings.PICKUP_ITEM;
import static com.mrcrayfish.controllable.client.ButtonBindings.PREVIOUS_CREATIVE_TAB;
import static com.mrcrayfish.controllable.client.ButtonBindings.PREVIOUS_RECIPE_TAB;
import static com.mrcrayfish.controllable.client.ButtonBindings.QUICK_MOVE;
import static com.mrcrayfish.controllable.client.ButtonBindings.RADIAL_MENU;
import static com.mrcrayfish.controllable.client.ButtonBindings.SPLIT_STACK;

public class ControllableModEvents {
    private static final ModLogger LOGGER = ModLogger.getLogger(ControllableModEvents.class, ReviveMeConfig.debugMode);

    public static List<ButtonBinding> revive_Me_vanillaBindingList = new ArrayList<>();

    public static boolean isControllableLoaded = false;

    public static boolean isPlayerDown(){
        if (!isControllableLoaded) return false;
        if (ClientUtil.getWorld() == null) return false;
        Player player = ClientUtil.getPlayer();
        if (player == null) return false;
        FallenCapability cap = FallenCapability.get(player);
        return cap.isFallen();
    }

    @SubscribeEvent
    public void onMove(ControllerEvent.Move event){
        if (!isPlayerDown()) return;

        event.setCanceled(!ReviveMeConfig.canMove);
    }

    public static void init(){
        if (revive_Me_vanillaBindingList.isEmpty()){
            revive_Me_vanillaBindingList.addAll(Arrays.asList(SCROLL_LEFT, SCROLL_RIGHT, PAUSE_GAME, NEXT_CREATIVE_TAB, PREVIOUS_CREATIVE_TAB, NEXT_RECIPE_TAB, PREVIOUS_RECIPE_TAB,
                    NAVIGATE_UP, NAVIGATE_DOWN, NAVIGATE_LEFT, NAVIGATE_RIGHT, PICKUP_ITEM, QUICK_MOVE, SPLIT_STACK,
                    DEBUG_INFO, RADIAL_MENU));
        }
    }
}
