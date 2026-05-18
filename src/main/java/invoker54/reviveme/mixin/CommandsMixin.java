package invoker54.reviveme.mixin;

import invoker54.reviveme.common.potion.FallenPotionEffect;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
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
    private void preCommand(CommandSourceStack p_82118_, String p_82119_, CallbackInfoReturnable<Integer> cir){
        FallenPotionEffect.bypassConfig = true;
    }

    @Inject(
            method = "performCommand",
            at = {
                    @At(value = "RETURN")
            })
    private void performCommand(CommandSourceStack p_82118_, String p_82119_, CallbackInfoReturnable<Integer> cir){
        FallenPotionEffect.bypassConfig = false;
    }
}
