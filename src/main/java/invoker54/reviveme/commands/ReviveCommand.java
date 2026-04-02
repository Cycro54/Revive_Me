package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class ReviveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("reviveme")
                        .requires((commandSource ->
                                commandSource.permissions().hasPermission(Permissions.COMMANDS_ADMIN)))
                        .executes(ReviveCommand::revivePlayer)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ReviveCommand::revivePlayer)
                        )
        );
    }

    private static int revivePlayer(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer fallen;
        Entity caller = commandContext.getSource().getEntity();
        try {
            fallen = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!(commandContext.getSource().getEntity() instanceof Player)){
                return 1;
            }
            fallen = commandContext.getSource().getPlayerOrException();
        }

        FallenData cap = FallenData.get(fallen);
        if (fallen.isDeadOrDying() || !cap.isFallen()){
            InvoText failTxt = InvoText.translate("revive_me.commands.revive_fail", fallen.getDisplayName());
            NetworkInit.sendMessage(failTxt.getText(), true, fallen);
            return 1;
        }

        ReviveMeConfig.configReviveData.revivePlayer(fallen, true, (Player) caller, "command");
        return 1;
    }
}
