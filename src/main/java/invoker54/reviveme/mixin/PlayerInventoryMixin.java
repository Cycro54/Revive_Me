package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Inventory.class)
public class PlayerInventoryMixin {
    @Shadow
    @Final
    public net.minecraft.world.entity.player.Player player;
    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(PlayerInventoryMixin.class, ReviveMeConfig.debugMode);

    @Inject(
            method = "swapPaint",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void swapPaint(double moveAmount, CallbackInfo ci){
        if (ClientUtil.getPlayer() == null) return;
        FallenData cap = FallenData.get(ClientUtil.getPlayer());
        if (cap == null) return;
        if (!cap.isFallen()) return;
        if (!FallenItemScreenEvent.isItemScreenActive) return;
        if (!cap.canSelfRevive()) return;
        int multiplier = (int) (Math.abs(moveAmount)/moveAmount);
        FallenItemScreenEvent.changeSelectedItem(multiplier);
        ci.cancel();
    }

    @Inject(
            method = "getSelected",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getSelected(CallbackInfoReturnable<ItemStack> cir){
        if ((this.player) != ClientUtil.getPlayer()) return;
        FallenData cap = FallenData.get((this.player));
        if (!cap.isFallen()) return;
        if (!FallenItemScreenEvent.isItemScreenActive) return;
        if (!cap.canSelfRevive()) return;
        Pair<ItemStack, ReviveItemData> pair = FallenItemScreenEvent.getSelectedPair();
        if (pair == null) return;

        cir.setReturnValue(pair.getKey());
    }
}
