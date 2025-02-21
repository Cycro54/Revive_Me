package invoker54.reviveme.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.KeyMappingLookup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Pseudo
@Mixin(KeyMapping.class)
public abstract class IForgeKeyMixin implements IForgeKeyMapping {
    @Shadow
    @Final
    private static Map<String, KeyMapping> ALL;
    @Shadow
    @Final
    private String name;
    @Shadow
    private int clickCount;

    @Shadow @Final private static KeyMappingLookup MAP;
    @Unique
    private static final Logger LOGGER = LogManager.getLogger();


    @Override
    public boolean isActiveAndMatches(InputConstants.Key keyCode) {
        if (ClientUtil.getWorld() != null) {
            FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
            KeyMapping keyMapping = ALL.get(this.name);

            if (cap.isFallen()) {
                if (!VanillaKeybindHandler.isVanillaKeybind(keyMapping)) {
                    return false;
                }
                if (ReviveMeConfig.interactWithInventory == ReviveMeConfig.INTERACT_WITH_INVENTORY.NO
                        && keyMapping.same(ClientUtil.getMinecraft().options.keyInventory) && ClientUtil.getMinecraft().screen == null) {
                    return false;
                }
            }
        }
        return IForgeKeyMapping.super.isActiveAndMatches(keyCode);
    }

    @Inject(
            method = "set(Lcom/mojang/blaze3d/platform/InputConstants$Key;Z)V",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true
    )
    private static void set(InputConstants.Key input, boolean isDown, CallbackInfo ci) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (ClientUtil.getMinecraft().options.keyUse.getKey().equals(input)) VanillaKeybindHandler.useHeld = isDown;
        if (ClientUtil.getMinecraft().options.keyAttack.getKey().equals(input)) VanillaKeybindHandler.attackHeld = isDown;
        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;

        for (KeyMapping keybinding : MAP.getAll(input)) {
            if (keybinding == null) continue;

            if (!VanillaKeybindHandler.isVanillaKeybind(keybinding)) continue;
            keybinding.setDown(isDown);

        }
        ci.cancel();

    }

    @Inject(
            method = "isDown()Z",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isDown(CallbackInfoReturnable<Boolean> cir) {
        if (ClientUtil.getWorld() == null) return;
        Player player = ClientUtil.getPlayer();
        if (player == null) return;
        FallenCapability cap = FallenCapability.GetFallCap(player);


        KeyMapping keyBinding = ALL.get(this.name);
        if (cap.isFallen()) {
            if (!VanillaKeybindHandler.isVanillaKeybind(keyBinding)) cir.setReturnValue(false);
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

        //This is strictly for the use key when reviving or being revived
        if (keyBinding.equals(ClientUtil.getMinecraft().options.keyUse)) {
            if (cap.getOtherPlayer() != null) {
                this.clickCount = 0;
                cir.setReturnValue(false);
            }
        }
    }

}
