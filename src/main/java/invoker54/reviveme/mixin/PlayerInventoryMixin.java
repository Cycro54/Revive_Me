package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public class PlayerInventoryMixin {
    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(PlayerInventoryMixin.class, ReviveMeConfig.debugMode);

    @Inject(
            method = "swapPaint",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void swapPaint(double moveAmount, CallbackInfo ci){
        if (ClientUtil.getPlayer() == null) return;
        FallenCapability cap = FallenCapability.get(ClientUtil.getPlayer());
        if (cap == null) return;
        if (!cap.isFallen()) return;
        if (!FallenItemScreenEvent.isItemScreenActive) return;
        int multiplier = (int) (Math.abs(moveAmount)/moveAmount);
        FallenItemScreenEvent.changeSelectedItem(multiplier);
        ci.cancel();
    }
}
