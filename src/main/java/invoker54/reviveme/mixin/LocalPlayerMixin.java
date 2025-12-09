package invoker54.reviveme.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {
//TODO: REMOVE THIS IF IT WORKS OUT!!!
//    @Inject(
//            method = "isMovingSlowly",
//            at = {
//                    @At(value = "HEAD")
//            }, cancellable = true)
//    private void isMovingSlowly(CallbackInfoReturnable<Boolean> cir){
//        if (ClientUtil.getPlayer() == null) return;
//        if (!FallenCapability.GetFallCap(ClientUtil.getPlayer()).isFallen()) return;
//        cir.setReturnValue(ClientUtil.getPlayer().isCrouching());
//    }
}
