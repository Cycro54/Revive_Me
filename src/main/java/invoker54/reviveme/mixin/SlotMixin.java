package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {
    @Inject(
            remap = true,
            method = "mayPickup(Lnet/minecraft/entity/player/PlayerEntity;)Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void mayPickupMix(PlayerEntity player, CallbackInfoReturnable<Boolean> cir) {
        if (player.level == null) return;
        FallenCapability cap = FallenCapability.GetFallCap(player);
        if (cap.isFallen()) {
            cir.setReturnValue(false);
        }
    }
}
