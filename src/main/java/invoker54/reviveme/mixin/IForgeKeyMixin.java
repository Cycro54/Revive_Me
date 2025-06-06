package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.extensions.IForgeKeybinding;
import net.minecraftforge.client.settings.KeyBindingMap;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Pseudo
@Mixin(KeyBinding.class)
public abstract class IForgeKeyMixin implements IForgeKeybinding {
    @Shadow
    private int clickCount;
    @Shadow @Final private static KeyBindingMap MAP;
    @Shadow private boolean isDown;

    @Shadow public abstract String getName();

    @Shadow private InputMappings.Input key;
    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(IForgeKeyMixin.class, ReviveMeConfig.debugMode);

    @Inject(
            method = "set(Lnet/minecraft/client/util/InputMappings$Input;Z)V",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true
    )
    private static void set(InputMappings.Input input, boolean isDown, CallbackInfo ci) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (VanillaKeybindHandler.getKey(ClientUtil.mC.options.keyUse).equals(input)) VanillaKeybindHandler.useHeld = isDown;
        if (VanillaKeybindHandler.getKey(ClientUtil.mC.options.keyAttack).equals(input)) VanillaKeybindHandler.attackHeld = isDown;
        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;
        if (!isDown) return;

        for (KeyBinding keybinding : MAP.lookupAll(input)) {
            if (keybinding == null) continue;

            keybinding.setDown(revive_Me_1_16_5$shouldPass(keybinding));
        }
        ci.cancel();

    }

    @Unique
    private static boolean revive_Me_1_16_5$shouldPass(KeyBinding keybinding){
        PlayerEntity player = ClientUtil.getPlayer();

        boolean isVanilla = VanillaKeybindHandler.isVanillaKeybind(keybinding);
        boolean isKeyInventory = keybinding == ClientUtil.mC.options.keyInventory;
        boolean isKeyDrop = keybinding == ClientUtil.mC.options.keyDrop;
        boolean isKeySwapOffhand = keybinding == ClientUtil.mC.options.keySwapOffhand;
        boolean isSwapOrDrop = isKeyDrop || isKeySwapOffhand;
        ItemStack mainStack = player.getMainHandItem();
        boolean isSacrificialItem = FallenCapability.GetFallCap(player).isSacrificialItem(mainStack);
        ReviveMeConfig.INTERACT_WITH_INVENTORY inventoryRule = ReviveMeConfig.interactWithInventory;

        if (!isVanilla) return false;
        else if (inventoryRule == ReviveMeConfig.INTERACT_WITH_INVENTORY.NO && (isKeyInventory || isSwapOrDrop)) return false;
        else if (inventoryRule == ReviveMeConfig.INTERACT_WITH_INVENTORY.LOOK_ONLY && isSwapOrDrop) return false;
        else if (isSwapOrDrop && isSacrificialItem) return false;

        return true;
    }

    @Inject(
            method = "click",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true
    )
    private static void click(InputMappings.Input input, CallbackInfo ci){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;

        KeyBinding keybinding = MAP.lookupActive(input);
        if (keybinding == null) return;

        if (!revive_Me_1_16_5$shouldPass(keybinding)) ci.cancel();
    }

    @Nonnull
    @Override
    public InputMappings.Input getKey() {
        if (ClientUtil.getWorld() == null) return this.key;
        if (VanillaKeybindHandler.overrideKeyblock) return this.key;
        PlayerEntity player = ClientUtil.getPlayer();
        FallenCapability cap = FallenCapability.GetFallCap(player);

        if (!cap.isFallen()) return this.key;
        if (revive_Me_1_16_5$shouldPass(this.getKeyBinding())) return this.key;
        if (this.getKeyBinding() == KeyInit.callForHelpKey.keyBind) return this.key;

        for (String s : ReviveMeConfig.allowedKeybinds){
            if (s.isEmpty()) continue;
            if (!this.getName().contains(s)) continue;
            return this.key;
        }
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
        if (ClientUtil.getWorld() == null) return;
        PlayerEntity player = ClientUtil.getPlayer();
        if (player == null) return;
        FallenCapability cap = FallenCapability.GetFallCap(player);

        KeyBinding keyBinding = this.getKeyBinding();
        if (cap.isFallen()) {
            if (!revive_Me_1_16_5$shouldPass(keyBinding)) cir.setReturnValue(false);
            if ((!ReviveMeConfig.canMove && VanillaKeybindHandler.isMovementKeybind(keyBinding))) cir.setReturnValue(false);

            //This is for jumping
            if (keyBinding.equals(ClientUtil.mC.options.keyJump)) {
                switch (ReviveMeConfig.canJump) {
                    case YES:
                        return;
                    case LIQUID_ONLY:
                        if (player.level.getFluidState(player.blockPosition()).isEmpty()) cir.setReturnValue(false);
                        return;
                    case NO:
                        cir.setReturnValue(false);
                        return;
                }
            }
        }
    }
}
