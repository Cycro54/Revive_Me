package invoker54.reviveme.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.settings.KeyMappingLookup;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import java.util.Map;

@Pseudo
@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements Comparable<KeyMapping>, net.minecraftforge.client.extensions.IForgeKeyMapping {
    //TODO: Remove all the useless Shadow stuff
    @Shadow
    private int clickCount;
    @Shadow @Final private static KeyMappingLookup MAP;
    @Shadow private boolean isDown;

    @Shadow public abstract String getName();

    @Shadow private InputConstants.Key key;
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow @Final private String name;

    @Shadow public abstract boolean matchesMouse(int p_90831_);

    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(KeyMappingMixin.class, ReviveMeConfig.debugMode);

    @Inject(
            method = "set",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true
    )
    private static void set(InputConstants.Key input, boolean isDown, CallbackInfo ci) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (VanillaKeybindHandler.getKey(ClientUtil.getMinecraft().options.keyUse).equals(input)) VanillaKeybindHandler.useHeld = isDown;
        if (VanillaKeybindHandler.getKey(ClientUtil.getMinecraft().options.keyAttack).equals(input)) VanillaKeybindHandler.attackHeld = isDown;
    }
    //TODO: Remove this later...
//
//    @Inject(
//            method = "click",
//            at = {
//                    @At(value = "HEAD")
//            },
//            cancellable = true
//    )
//    private static void click(InputConstants.Key input, CallbackInfo ci){
//        if (ClientUtil.getWorld() == null) return;
//        if (ClientUtil.getPlayer() == null) return;
//        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
//        if (!cap.isFallen()) return;
//
//        KeyMapping keybinding = MAP.lookupActive(input);
//        if (keybinding == null) return;
//
//        if (!revive_Me_1_16_5$shouldPass(keybinding)) ci.cancel();
//    }

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
        if (!FallenCapability.GetFallCap(ClientUtil.getPlayer()).isFallen()) return;
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
        if (!FallenCapability.GetFallCap(ClientUtil.getPlayer()).isFallen()) return;
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
        FallenCapability cap = FallenCapability.GetFallCap(player);

        if (!cap.isFallen()) return this.key;
        if (VanillaKeybindHandler.isAllowedKeybind((KeyMapping) (Object)this)) return this.key;

        return InputConstants.Type.KEYSYM.getOrCreate(-1);
    }

    @Inject(
            method = "isDown()Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isDown(CallbackInfoReturnable<Boolean> cir) {
        if (!this.isDown) return;
        if (ClientUtil.getWorld() == null) return;
        Player player = ClientUtil.getPlayer();
        if (player == null) return;
        FallenCapability cap = FallenCapability.GetFallCap(player);

        KeyMapping keyBinding = ((KeyMapping)(Object)this);
        if (cap.isFallen()) {
            if (!VanillaKeybindHandler.isAllowedKeybind(keyBinding)) cir.setReturnValue(false);
            if ((!ReviveMeConfig.canMove && VanillaKeybindHandler.isMovementKeybind(keyBinding))) cir.setReturnValue(false);

            //This is for jumping
            if (keyBinding.equals(ClientUtil.getMinecraft().options.keyJump)) {
                switch (ReviveMeConfig.canJump) {
                    case YES:
                        return;
                    case LIQUID_ONLY:
                        if (player.level().getFluidState(player.blockPosition()).isEmpty()) cir.setReturnValue(false);
                        return;
                    case NO:
                        cir.setReturnValue(false);
                        return;
                }
            }
        }
    }
}
