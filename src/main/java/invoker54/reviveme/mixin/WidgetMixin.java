package invoker54.reviveme.mixin;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractWidget.class)
public class WidgetMixin {

    @Inject(

            method = "isValidClickButton",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isValidClickButtonMix(int buttonID, CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.getMinecraft().screen == null) return;
        if (!(ClientUtil.getMinecraft().screen instanceof InventoryScreen)) return;
        FallenData cap = FallenData.get(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;
        if (ReviveMeConfig.interactWithInventory == ReviveMeConfig.INTERACT_WITH_INVENTORY.YES) return;
        cir.setReturnValue(false);
    }
}