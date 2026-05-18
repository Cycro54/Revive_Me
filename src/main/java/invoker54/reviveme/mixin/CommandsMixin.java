package invoker54.reviveme.mixin;

import invoker54.reviveme.common.potion.FallenPotionEffect;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(
            method = "performCommand",
            at = {
                    @At(value = "HEAD")
            })
    private void preCommand(CommandSource source, String p_197059_2_, CallbackInfoReturnable<Integer> cir){
        FallenPotionEffect.bypassConfig = true;
    }

    @Inject(
            method = "performCommand",
            at = {
                    @At(value = "RETURN")
            })
    private void performCommand(CommandSource source, String p_197059_2_, CallbackInfoReturnable<Integer> cir){
        FallenPotionEffect.bypassConfig = false;
    }
}
