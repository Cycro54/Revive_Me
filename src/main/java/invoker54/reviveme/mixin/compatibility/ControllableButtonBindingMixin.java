package invoker54.reviveme.mixin.compatibility;

import com.mrcrayfish.controllable.client.ButtonBinding;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mrcrayfish.controllable.client.ButtonBindings.*;
import static invoker54.reviveme.compatibility.controllable.client.events.ControllableModEvents.revive_Me_vanillaBindingList;

@Mixin(ButtonBinding.class)
public abstract class ControllableButtonBindingMixin {
    @Unique
    private static final ModLogger LOGGERT =
            ModLogger.getLogger(ControllableButtonBindingMixin.class, ReviveMeConfig.debugMode);

    @Shadow
    public abstract String getLabelKey();

    @Unique
    private static boolean revive_Me_1_16_5$_isPlayerDown() {
        if (ClientUtil.mC.screen != null) return false;
        if (ClientUtil.getWorld() == null) return false;
        Player player = ClientUtil.getPlayer();
        if (player == null) return false;
        FallenCapability cap = FallenCapability.get(player);
        if (!cap.isFallen()) return false;

        return cap.isFallen();
    }

    @Inject(
            remap = false,
            method = "isButtonDown",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    public void isButtonDown(CallbackInfoReturnable<Boolean> cir) {
        if (!revive_Me_1_16_5$_isPlayerDown()) return;
        KeyMapping keyBinding = VanillaKeybindHandler.getOrCreateKey(this.getLabelKey());
        if (revive_Me_vanillaBindingList.contains(((ButtonBinding)(Object)this))) return;
        if (VanillaKeybindHandler.canBeDown(keyBinding)) return;
        cir.setReturnValue(false);
    }

    @Inject(
            remap = false,
            method = "isButtonPressed",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    public void isButtonPressed(CallbackInfoReturnable<Boolean> cir) {
        if (!revive_Me_1_16_5$_isPlayerDown()) return;
        if  (revive_Me_vanillaBindingList.contains(((ButtonBinding)(Object)this))) return;
        KeyMapping keyBinding = VanillaKeybindHandler.getOrCreateKey(this.getLabelKey());
        if (VanillaKeybindHandler.isAllowedKeybind(keyBinding)) return;
        cir.setReturnValue(false);
    }
//
//    @Inject(
//            remap = false,
//            method = "isButtonDown",
//            at = {
//                    @At(value = "HEAD")
//            },
//            cancellable = true)
//    public void isButtonDown(CallbackInfoReturnable<Boolean> cir) {
//        if (!revive_Me_1_16_5$_isPlayerDown()) return;
//        KeyMapping keyBinding = VanillaKeybindHandler.getOrCreateKey(this.getLabelKey());
//        LOGGERT.error("Keybinding found: " + keyBinding.getName());
//        if (ClientUtil.mC.options.keyUse.equals(keyBinding))
//            VanillaKeybindHandler.useHeld = this.pressed && this.isActiveAndValidContext();
//        if (ClientUtil.mC.options.keyAttack.equals(keyBinding))
//            VanillaKeybindHandler.attackHeld = this.pressed && this.isActiveAndValidContext();
//
//        boolean isAllowed = VanillaKeybindHandler.isAllowedKeybind(keyBinding);
//
//        //This is for jumping
//        if (keyBinding.equals(ClientUtil.mC.options.keyJump)) {
//            switch (ReviveMeConfig.canJump) {
//                case YES: {
//                    isAllowed = true;
//                    break;
//                }
//                case LIQUID_ONLY: {
//                    if (ClientUtil.getPlayer().level.getFluidState(ClientUtil.getPlayer().blockPosition()).isEmpty())
//                        isAllowed = false;
//                    break;
//                }
//                case NO: {
//                    isAllowed = false;
//                    break;
//                }
//            }
//        }
//
//        if (!isAllowed) cir.setReturnValue(false);
//    }
}
