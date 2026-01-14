package invoker54.reviveme.mixin.compatibility;

import com.mrcrayfish.controllable.client.binding.ButtonBinding;
import com.mrcrayfish.controllable.client.binding.context.BindingContext;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mrcrayfish.controllable.client.binding.ButtonBindings.*;

@Mixin(ButtonBinding.class)
public abstract class ControllableButtonBindingMixin {
    @Unique
    private static final ModLogger LOGGERT =
            ModLogger.getLogger(ControllableButtonBindingMixin.class, ReviveMeConfig.debugMode);

    @Shadow
    public abstract String getLabelKey();

    @Shadow
    public abstract BindingContext getContext();

    @Shadow
    private boolean pressed;
    @Unique
    private static List<ButtonBinding> revive_Me_vanillaBindingList = new ArrayList<>();

    @Unique
    private static boolean revive_Me_1_16_5$_isPlayerDown() {
        if (ClientUtil.getMinecraft().screen != null) return false;
        if (ClientUtil.getWorld() == null) return false;
        Player player = ClientUtil.getPlayer();
        if (player == null) return false;
        FallenData cap = FallenData.get(player);

        if (revive_Me_vanillaBindingList.isEmpty()){
            revive_Me_vanillaBindingList.addAll(Arrays.asList(SCROLL_HOTBAR_LEFT, SCROLL_HOTBAR_RIGHT, PAUSE_GAME, NEXT_CREATIVE_TAB, PREVIOUS_CREATIVE_TAB, NEXT_RECIPE_TAB, PREVIOUS_RECIPE_TAB,
                    NAVIGATE_UP, NAVIGATE_DOWN, NAVIGATE_LEFT, NAVIGATE_RIGHT, PICKUP_ITEM, QUICK_MOVE, SPLIT_STACK,
                    DEBUG_INFO, RADIAL_MENU));
        }
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
        this.pressed = false;
        cir.setReturnValue(false);
    }

    @Inject(
            remap = false,
            method = "setPressed",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    public void setPressed(boolean pressed, CallbackInfo ci) {
        if (!revive_Me_1_16_5$_isPlayerDown()) return;
        KeyMapping keyBinding = VanillaKeybindHandler.getOrCreateKey(this.getLabelKey());
        if  (revive_Me_vanillaBindingList.contains(((ButtonBinding)(Object)this))) return;
        if (VanillaKeybindHandler.isAllowedKeybind(keyBinding)) return;
        this.pressed = false;
        ci.cancel();
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
//        if (ClientUtil.getMinecraft().options.keyUse.equals(keyBinding))
//            VanillaKeybindHandler.useHeld = this.pressed && this.isActiveAndValidContext();
//        if (ClientUtil.getMinecraft().options.keyAttack.equals(keyBinding))
//            VanillaKeybindHandler.attackHeld = this.pressed && this.isActiveAndValidContext();
//
//        boolean isAllowed = VanillaKeybindHandler.isAllowedKeybind(keyBinding);
//
//        //This is for jumping
//        if (keyBinding.equals(ClientUtil.getMinecraft().options.keyJump)) {
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
