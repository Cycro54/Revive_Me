package invoker54.reviveme.compatibility.controllable.client.events;

import com.mrcrayfish.controllable.event.ControllerEvent;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ControllableModEvents {
    private static final ModLogger LOGGER = ModLogger.getLogger(ControllableModEvents.class, ReviveMeConfig.debugMode);

    public static boolean isControllableLoaded = false;

    public static boolean isPlayerDown(){
        if (!isControllableLoaded) return false;
        if (ClientUtil.getWorld() == null) return false;
        PlayerEntity player = ClientUtil.getPlayer();
        if (player == null) return false;
        FallenCapability cap = FallenCapability.get(player);
        return cap.isFallen();
    }

    @SubscribeEvent
    public void onMove(ControllerEvent.Move event){
        if (!isPlayerDown()) return;

        event.setCanceled(!ReviveMeConfig.canMove);
    }
}
