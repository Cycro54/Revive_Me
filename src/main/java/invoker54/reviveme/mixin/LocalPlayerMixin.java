package invoker54.reviveme.mixin;

import com.mojang.authlib.GameProfile;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.ProfilePublicKey;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {
    @Shadow
    public abstract boolean isCrouching();

    @Shadow
    public abstract boolean isShiftKeyDown();

    @Unique
    private static final ModLogger LOGGERT =
            ModLogger.getLogger(LocalPlayerMixin.class, ReviveMeConfig.debugMode);

    public LocalPlayerMixin(ClientLevel p_234112_, GameProfile p_234113_, @Nullable ProfilePublicKey p_234114_) {
        super(p_234112_, p_234113_, p_234114_);
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
