package invoker54.reviveme.mixin;

import com.mojang.authlib.GameProfile;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayerEntity {

    @Shadow
    public abstract boolean isCrouching();

    @Shadow
    public abstract boolean isShiftKeyDown();

    @Unique
    private static final ModLogger LOGGERT =
            ModLogger.getLogger(LocalPlayerMixin.class, ReviveMeConfig.debugMode);

    public LocalPlayerMixin(ClientWorld p_108548_, GameProfile p_108549_) {
        super(p_108548_, p_108549_);
    }

    @Inject(
            method = "isMovingSlowly",
            at = {@At("HEAD")},
            cancellable = true
    )
    private void isMovingSlowly(CallbackInfoReturnable<Boolean> cir){
        if (!FallenCapability.get(this).isFallen()) return;
        if (this.isShiftKeyDown()) return;
        cir.setReturnValue(false);
    }

    @Inject(
            method = "isCrouching",
            at = {@At("HEAD")},
            cancellable = true
    )
    private void isCrouching(CallbackInfoReturnable<Boolean> cir){
        if (!FallenCapability.get(this).isFallen()) return;
        cir.setReturnValue(this.isShiftKeyDown() || this.getPose() == Pose.CROUCHING);
    }

}
