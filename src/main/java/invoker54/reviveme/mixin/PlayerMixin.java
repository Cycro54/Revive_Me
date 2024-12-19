package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenData;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Shadow @Final private Inventory inventory;

    @Inject(
            method = "canEat(Z)Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void canEat(boolean p_36392_, CallbackInfoReturnable<Boolean> cir){
        FallenData cap = FallenData.get(this.inventory.player);
        if (!cap.isFallen()) return;
        cir.setReturnValue(false);
    }
}
