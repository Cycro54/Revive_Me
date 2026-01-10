package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncConfigMsg;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraftforge.fml.network.PacketDistributor;

public class ReloadCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("revivemereload")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes(ReloadCommand::reload)
        );
    }

    private static int reload(CommandContext<CommandSource> commandContext) {
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncConfigMsg(ReviveMeConfig.serialize()));
        NetworkHandler.sendMessage(InvoText.translate("revive-me.commands.reload").getText(),
                true, commandContext.getSource().getEntity());

        return 1;
    }
}
