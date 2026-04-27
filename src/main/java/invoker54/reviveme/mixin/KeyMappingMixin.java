package invoker54.reviveme.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Pseudo
@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements Comparable<KeyMapping>, net.minecraftforge.client.extensions.IForgeKeyMapping {
    @Shadow
    private boolean isDown;

    @Shadow
    private InputConstants.Key key;

    @Shadow
    public abstract void setDown(boolean p_225593_1_);

    @Shadow
    private int clickCount;
    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(KeyMappingMixin.class, ReviveMeConfig.debugMode);

    @Unique
    private boolean reviveMe$ShouldRun = true;

    @Inject(
            method = "matches",
            at = {
                    @At("HEAD")
            },
            cancellable = true
    )
    private void matches(int keysym, int scancode, CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;
        if (VanillaKeybindHandler.isAllowedKeybind(((KeyMapping)(Object)this))) return;
        cir.setReturnValue(false);
    }

    @Inject(
            method = "matchesMouse",
            at = {
                    @At("HEAD")
            },
            cancellable = true
    )
    private void matchesMouse(int key, CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;
        if (VanillaKeybindHandler.isAllowedKeybind(((KeyMapping)(Object)this))) return;
        cir.setReturnValue(false);
    }

    @Nonnull
    @Override
    public InputConstants.Key getKey() {
        if (ClientUtil.getWorld() == null) return this.key;
        if (ClientUtil.getPlayer() == null) return this.key;
        if (VanillaKeybindHandler.overrideKeyblock) return this.key;
        Player player = ClientUtil.getPlayer();
        FallenCapability cap = FallenCapability.get(player);

        if (!cap.isFallen()) return this.key;
        if (VanillaKeybindHandler.isAllowedKeybind((KeyMapping) (Object)this)) return this.key;

        return InputConstants.Type.KEYSYM.getOrCreate(314);
    }

    @Inject(
            method = "isDown()Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isDown(CallbackInfoReturnable<Boolean> cir) {
        if (!this.isDown) return;
        if (!this.reviveMe$ShouldRun) return;
        this.reviveMe$ShouldRun = false;
        KeyMapping keyBinding = ((KeyMapping) (Object) this);

        try {
            boolean canBeDown = VanillaKeybindHandler.canBeDown(keyBinding);
            if (!canBeDown) {
                this.clickCount = 0;
                this.setDown(false);
            }
        }
        catch (Exception e){
            LOGGERT.error("[Revive Me!] Something went wrong in my 'isDown' MIXIN!");
            e.printStackTrace();
        }
        this.reviveMe$ShouldRun = true;
    }
}
