package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public class SlotMixin {
    @Inject(
            remap = true,
            method = "mayPickup(Lnet/minecraft/world/entity/player/Player;)Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void mayPickupMix(Player player, CallbackInfoReturnable<Boolean> cir) {
        FallenCapability cap = FallenCapability.GetFallCap(player);
        if (cap.isFallen()) {
            cir.setReturnValue(false);
        }
    }
}
