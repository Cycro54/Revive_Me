package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class SlotMixin {
    @Shadow public abstract boolean hasItem();

    @Shadow public abstract ItemStack getItem();

    @Shadow @Final
    public IInventory container;

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
        if (cap.isSacrificialItem(getItem())) cir.setReturnValue(false);
        else {
            cir.setReturnValue(ReviveMeConfig.interactWithInventory == ReviveMeConfig.INTERACT_WITH_INVENTORY.YES);
        }
    }

    @Inject(

            method = "mayPlace",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void mayPlaceMix(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (!(this.container instanceof PlayerInventory)) return;
        FallenCapability cap = FallenCapability.GetFallCap(((PlayerInventory) this.container).player);
        if (!cap.isFallen()) return;
        if (stack.isEmpty()) return;
        if (cap.isSacrificialItem(stack)) cir.setReturnValue(false);
        else {
            cir.setReturnValue(ReviveMeConfig.interactWithInventory == ReviveMeConfig.INTERACT_WITH_INVENTORY.YES);
        }
    }
}
