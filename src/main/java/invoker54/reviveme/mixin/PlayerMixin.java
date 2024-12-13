package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerMixin {

    @Shadow @Final public PlayerInventory inventory;

    @Inject(
            
            method = "canEat(Z)Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void canEat(boolean p_36392_, CallbackInfoReturnable<Boolean> cir){
        FallenCapability cap = FallenCapability.GetFallCap(this.inventory.player);
        if (cap == null) return;
        if (!cap.isFallen()) return;

        cir.setReturnValue(false);
    }
}
