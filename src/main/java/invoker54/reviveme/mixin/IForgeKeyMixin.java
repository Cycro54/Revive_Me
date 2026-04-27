package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.extensions.IForgeKeybinding;
import net.minecraftforge.client.settings.KeyBindingMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Pseudo
@Mixin(KeyBinding.class)
public abstract class IForgeKeyMixin implements IForgeKeybinding {
    @Shadow
    @Final
    private static KeyBindingMap MAP;
    @Shadow
    private boolean isDown;

    @Shadow
    private InputMappings.Input key;

    @Shadow
    public abstract void setDown(boolean p_225593_1_);

    @Shadow
    public int clickCount;
    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(IForgeKeyMixin.class, ReviveMeConfig.debugMode);

    @Unique
    private boolean reviveMe$ShouldRun = true;

    @Inject(
            method = "matches",
            at = {
                    @At("HEAD")
            },
            cancellable = true
    )
    private void matches(int keysym, int scancode, CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;
        if (VanillaKeybindHandler.isAllowedKeybind(((KeyBinding) (Object) this))) return;
        cir.setReturnValue(false);
    }

    @Inject(
            method = "matchesMouse",
            at = {
                    @At("HEAD")
            },
            cancellable = true
    )
    private void matchesMouse(int key, CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;
        if (VanillaKeybindHandler.isAllowedKeybind(((KeyBinding) (Object) this))) return;
        cir.setReturnValue(false);
    }

    @Nonnull
    @Override
    public InputMappings.Input getKey() {
        if (ClientUtil.getWorld() == null) return this.key;
        if (ClientUtil.getPlayer() == null) return this.key;
        if (VanillaKeybindHandler.overrideKeyblock) return this.key;
        PlayerEntity player = ClientUtil.getPlayer();
        FallenCapability cap = FallenCapability.get(player);

        if (!cap.isFallen()) return this.key;
        if (VanillaKeybindHandler.isAllowedKeybind((KeyBinding) (Object) this)) return this.key;

        return InputMappings.Type.KEYSYM.getOrCreate(314);
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
        KeyBinding keyBinding = ((KeyBinding) (Object) this);

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
