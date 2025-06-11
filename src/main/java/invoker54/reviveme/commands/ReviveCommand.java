package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.event.FallenTimerEvent;
import invoker54.reviveme.common.network.NetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class ReviveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("reviveme")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes(ReviveCommand::revivePlayer)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ReviveCommand::revivePlayer)
                        )
        );
    }

    private static int revivePlayer(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer caller;
        try {
            caller = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!(commandContext.getSource().getEntity() instanceof Player)){
                return 1;
            }
            caller = (ServerPlayer) commandContext.getSource().getEntity();
        }
        FallenCapability cap = FallenCapability.GetFallCap(caller);

        if (caller.isDeadOrDying() || !cap.isFallen()){
            InvoText failTxt = InvoText.translate("revive-me.commands.revive_fail", caller.getDisplayName());
            NetworkHandler.sendMessage(failTxt.getText(), true, caller);
            return 1;
        }

        FallenTimerEvent.revivePlayer(caller, true);
        return 1;
    }
}
