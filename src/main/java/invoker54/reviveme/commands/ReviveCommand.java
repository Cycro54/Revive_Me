package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ReviveCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("reviveme")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes(ReviveCommand::revivePlayer)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ReviveCommand::revivePlayer)
                        )
        );
    }

    private static int revivePlayer(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity fallen;
        Entity caller = commandContext.getSource().getEntity();
        try {
            fallen = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!(commandContext.getSource().getEntity() instanceof PlayerEntity)){
                return 1;
            }
            fallen = commandContext.getSource().getPlayerOrException();
        }

        FallenCapability cap = FallenCapability.get(fallen);
        if (fallen.isDeadOrDying() || !cap.isFallen()){
            InvoText failTxt = InvoText.translate("revive-me.commands.revive_fail", fallen.getDisplayName());
            NetworkHandler.sendMessage(failTxt.getText(), true, fallen);
            return 1;
        }

        ReviveMeConfig.configReviveData.revivePlayer(fallen, true, (PlayerEntity) caller, "command");
        return 1;
    }
}
