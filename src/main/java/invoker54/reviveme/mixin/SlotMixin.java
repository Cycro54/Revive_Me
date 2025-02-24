package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow public abstract boolean hasItem();

    @Shadow public abstract ItemStack getItem();

    @Inject(
            
            method = "mayPickup(Lnet/minecraft/entity/player/PlayerEntity;)Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void mayPickupMix(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        FallenCapability cap = FallenCapability.GetFallCap(player);
        if (!cap.isFallen()) return;
        if (!this.hasItem()) return;
        if (!cap.usedSacrificedItems() && cap.isSacrificialItem(getItem())) cir.setReturnValue(false);
        else {
            cir.setReturnValue(ReviveMeConfig.interactWithInventory == ReviveMeConfig.INTERACT_WITH_INVENTORY.YES);
        }
    }
}
