package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {

    @Inject(
            method = "tickNonPassenger",
            at = {
                    @At(value = "HEAD")
            })
    private void enableFallenCreative(Entity p_8648_, CallbackInfo ci){
        FallenCapability.FALLEN_HAS_CREATIVE = true;
    }

    @Inject(
            method = "tickNonPassenger",
            at = {
                    @At(value = "TAIL")
            })
    private void disableFallenCreative(Entity p_8648_, CallbackInfo ci){
        FallenCapability.FALLEN_HAS_CREATIVE = false;
    }
}
