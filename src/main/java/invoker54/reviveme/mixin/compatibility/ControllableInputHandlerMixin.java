package invoker54.reviveme.mixin.compatibility;

import com.mrcrayfish.controllable.client.InputHandler;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenData;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InputHandler.class)
public class ControllableInputHandlerMixin {

    @Inject(
            remap = false,
            method = "handleBindingPressed",
            at = {
                    @At(value = "RETURN")
            },
            cancellable = true)
    public void handleBindingPressed(CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.getPlayer() == null) return;
        FallenData fallenData = FallenData.get(ClientUtil.getPlayer());
        boolean isFallen = fallenData.isFallen();
        boolean isLookingAtFallen = false;
        if (ClientUtil.getMinecraft().crosshairPickEntity instanceof Player){
            isLookingAtFallen = FallenData.get((Player)ClientUtil.getMinecraft().crosshairPickEntity).isFallen();
        }

        if (!isFallen && !isLookingAtFallen) return;

        cir.setReturnValue(false);
    }

}
