package invoker54.reviveme.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
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
public abstract class KeyMappingMixin implements IKeyMappingExtension {
    @Shadow
    private int clickCount;
    @Shadow
    @Final
    private static KeyMappingLookup MAP;
    @Shadow
    private boolean isDown;
    @Shadow
    @Final
    private static Map<String, KeyMapping> ALL;
    @Shadow
    @Final
    private String name;
    @Unique
    private static final Logger LOGGER = LogManager.getLogger();

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
        if (ClientUtil.getMinecraft().options.keyAttack.getKey().equals(input))
            VanillaKeybindHandler.attackHeld = isDown;
        FallenData cap = FallenData.get(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;
        if (!isDown) return;

        for (KeyMapping keybinding : MAP.getAll(input)) {
            if (keybinding == null) continue;

            keybinding.setDown(revive_Me_1_16_5$shouldPass(keybinding));
        }
        ci.cancel();

    }

    @Unique
    private static boolean revive_Me_1_16_5$shouldPass(KeyMapping keybinding) {
        Player player = ClientUtil.getPlayer();

        boolean isVanilla = VanillaKeybindHandler.isVanillaKeybind(keybinding);
        boolean isKeyInventory = keybinding.same(ClientUtil.getMinecraft().options.keyInventory);
        boolean isKeyDrop = keybinding.same(ClientUtil.getMinecraft().options.keyDrop);
        boolean isSacrificalItem = FallenData.get(player).getItemList().contains(player.getMainHandItem().getItem());
        ReviveMeConfig.INTERACT_WITH_INVENTORY inventoryRule = ReviveMeConfig.interactWithInventory;

        if (!isVanilla) return false;
        else if (inventoryRule == ReviveMeConfig.INTERACT_WITH_INVENTORY.NO && (isKeyInventory || isKeyDrop))
            return false;
        else if (inventoryRule == ReviveMeConfig.INTERACT_WITH_INVENTORY.LOOK_ONLY && isKeyDrop) return false;
        else if (isKeyDrop && isSacrificalItem) return false;

        return true;
    }

    @Inject(
            method = "click",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true
    )
    private static void click(InputConstants.Key input, CallbackInfo ci) {
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        FallenData cap = FallenData.get(ClientUtil.getPlayer());
        if (!cap.isFallen()) return;

        for (KeyMapping keybinding : MAP.getAll(input)) {
            if (keybinding == null) continue;

            if (revive_Me_1_16_5$shouldPass(keybinding)) ++keybinding.clickCount;
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
        if (!this.isDown) return;
        if (ClientUtil.getWorld() == null) return;
        Player player = ClientUtil.getPlayer();
        if (player == null) return;
        FallenData cap = FallenData.get(player);

        KeyMapping keyBinding = ALL.get(this.name);
        if (cap.isFallen()) {
            if (!revive_Me_1_16_5$shouldPass(keyBinding)) {
                cir.setReturnValue(false);
                return;
            }
            if ((!ReviveMeConfig.canMove && VanillaKeybindHandler.isMovementKeybind(keyBinding))) {
                cir.setReturnValue(false);
                return;
            }

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
