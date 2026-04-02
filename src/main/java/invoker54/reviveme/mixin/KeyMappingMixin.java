package invoker54.reviveme.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import net.neoforged.neoforge.client.settings.KeyMappingLookup;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import java.util.Map;

@Pseudo
@Mixin(KeyMapping.class)
public abstract class KeyMappingMixin implements Comparable<KeyMapping>, IKeyMappingExtension {
    @Shadow
    private boolean isDown;

    @Shadow
    private InputConstants.Key key;

    @Shadow
    public abstract void setDown(boolean p_225593_1_);

    @Unique
    private static final ModLogger LOGGERT = ModLogger.getLogger(KeyMappingMixin.class, ReviveMeConfig.debugMode);

    @Inject(
            method = "matches",
            at = {
                    @At("HEAD")
            },
            cancellable = true
    )
    private void matches(KeyEvent event, CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenData.get(ClientUtil.getPlayer()).isFallen()) return;
        if (VanillaKeybindHandler.isAllowedKeybind(((KeyMapping)(Object)this))) return;
//        LOGGERT.warn("{matches} This failed: " + this.name);
        cir.setReturnValue(false);
    }

    @Inject(
            method = "matchesMouse",
            at = {
                    @At("HEAD")
            },
            cancellable = true
    )
    private void matchesMouse(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenData.get(ClientUtil.getPlayer()).isFallen()) return;
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
        FallenData cap = FallenData.get(player);

        if (!cap.isFallen()) return this.key;
        if (VanillaKeybindHandler.isAllowedKeybind((KeyMapping) (Object)this)) return this.key;
//        LOGGERT.warn("{getKey} This failed: " + this.name);

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
        KeyMapping keyBinding = ((KeyMapping) (Object) this);
        if (VanillaKeybindHandler.canBeDown(keyBinding)) return;
//        LOGGERT.warn("{isDown} This failed: " + this.name);

        this.setDown(false);
    }
}
