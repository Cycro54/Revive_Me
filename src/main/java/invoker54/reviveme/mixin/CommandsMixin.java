package invoker54.reviveme.mixin;

import com.mojang.brigadier.ParseResults;
import invoker54.reviveme.common.potion.FallenPotionEffect;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {

    @Inject(
            method = "performCommand",
            at = {
                    @At(value = "HEAD")
            })
    private void preCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci){
        FallenPotionEffect.bypassConfig = true;
    }

    @Inject(
            method = "performCommand",
            at = {
                    @At(value = "RETURN")
            })
    private void performCommand(ParseResults<CommandSourceStack> parseResults, String command, CallbackInfo ci){
        FallenPotionEffect.bypassConfig = false;
    }
}
