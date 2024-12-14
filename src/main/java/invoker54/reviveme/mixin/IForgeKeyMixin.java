package invoker54.reviveme.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Pseudo
@Mixin(KeyMapping.class)
public abstract class IForgeKeyMixin implements IForgeKeyMapping {
    @Shadow @Final private static Map<String, KeyMapping> ALL;
    @Shadow @Final private String name;
    @Shadow private int clickCount;
    @Shadow
    boolean isDown;
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
                        && keyMapping.same(ClientUtil.mC.options.keyInventory) && ClientUtil.mC.screen == null) {
                    return false;
                }
            }
        }
        return IForgeKeyMapping.super.isActiveAndMatches(keyCode);
    }



    @Inject(
            remap = true,
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


        KeyMapping KeyBinding = ALL.get(this.name);
        if (cap.isFallen()){
            //This will disable move keybinds if they are disallowed in the config
            if (!ReviveMeConfig.canMove && VanillaKeybindHandler.isMovementKeybind(KeyBinding)){
                cir.setReturnValue(false);
            }
            //This is for jumping
            if (KeyBinding.equals(ClientUtil.mC.options.keyJump)) {
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

        //This is strictly for the use key when reviving or being revived
        if (KeyBinding.equals(ClientUtil.mC.options.keyUse)) {
            VanillaKeybindHandler.useKeyDown = isDown;

            if (cap.getOtherPlayer() != null) {
                this.clickCount = 0;
                cir.setReturnValue(false);
            }
        }
    }

}
