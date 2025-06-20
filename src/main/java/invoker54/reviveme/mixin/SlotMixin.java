package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
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

    @Shadow @Final public Container container;

    @Inject(
            method = "mayPickup(Lnet/minecraft/world/entity/player/Player;)Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void mayPickupMix(Player player, CallbackInfoReturnable<Boolean> cir) {
        FallenData cap = FallenData.get(player);
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
        if (!(this.container instanceof Inventory)) return;
        FallenData cap = FallenData.get(((Inventory) this.container).player);
        if (!cap.isFallen()) return;
        if (stack.isEmpty()) return;
        if (cap.isSacrificialItem(stack)) cir.setReturnValue(false);
        else {
            cir.setReturnValue(ReviveMeConfig.interactWithInventory == ReviveMeConfig.INTERACT_WITH_INVENTORY.YES);
        }
    }
}
