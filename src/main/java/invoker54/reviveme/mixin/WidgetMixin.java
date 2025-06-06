package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.gui.widget.Widget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Widget.class)
public class WidgetMixin {

    @Inject(

            method = "isValidClickButton",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isValidClickButtonMix(int buttonID, CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.mC.screen == null) return;
        if (!(ClientUtil.mC.screen instanceof InventoryScreen)) return;
        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;
        if (ReviveMeConfig.interactWithInventory == ReviveMeConfig.INTERACT_WITH_INVENTORY.YES) return;
        cir.setReturnValue(false);
    }
}
