package invoker54.reviveme.mixin;

import com.mojang.brigadier.ParseResults;
import invoker54.reviveme.common.potion.FallenPotionEffect;
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
    private void preCommand(ParseResults<CommandSourceStack> p_242844_, String p_242841_, CallbackInfoReturnable<Integer> cir){
        FallenPotionEffect.bypassConfig = true;
    }

    @Inject(
            method = "performCommand",
            at = {
                    @At(value = "RETURN")
            })
    private void performCommand(ParseResults<CommandSourceStack> p_242844_, String p_242841_, CallbackInfoReturnable<Integer> cir){
        FallenPotionEffect.bypassConfig = false;
    }
}
