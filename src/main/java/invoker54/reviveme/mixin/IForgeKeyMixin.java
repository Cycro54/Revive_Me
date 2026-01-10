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

    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(IForgeKeyMixin.class, ReviveMeConfig.debugMode);

//    @Inject(
//            method = "set(Lnet/minecraft/client/util/InputMappings$Input;Z)V",
//            at = {
//                    @At(value = "HEAD")
//            },
//            cancellable = true
//    )
//    private static void set(InputMappings.Input input, boolean isDown, CallbackInfo ci) {
//        if (ClientUtil.getWorld() == null) return;
//        if (ClientUtil.getPlayer() == null) return;
//        if (VanillaKeybindHandler.getKey(ClientUtil.mC.options.keyUse).equals(input))
//            VanillaKeybindHandler.useHeld = isDown;
//        if (VanillaKeybindHandler.getKey(ClientUtil.mC.options.keyAttack).equals(input))
//            VanillaKeybindHandler.attackHeld = isDown;
//    }

    //TODO: Remove this later...
//    @Inject(
//            method = "click",
//            at = {
//                    @At(value = "HEAD")
//            },
//            cancellable = true
//    )
//    private static void click(InputMappings.Input input, CallbackInfo ci){
//        if (ClientUtil.getWorld() == null) return;
//        if (ClientUtil.getPlayer() == null) return;
//        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
//        if (!cap.isFallen()) return;
//
//        KeyBinding keybinding = MAP.lookupActive(input);
//        if (keybinding == null) ci.cancel();
//    }

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

        return InputMappings.Type.KEYSYM.getOrCreate(-1);
    }

    @Inject(
            method = "isDown()Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isDown(CallbackInfoReturnable<Boolean> cir) {
        if (!this.isDown) return;
        KeyBinding keyBinding = ((KeyBinding) (Object) this);
        if (VanillaKeybindHandler.canBeDown(keyBinding)) return;

        this.setDown(false);
    }
}
