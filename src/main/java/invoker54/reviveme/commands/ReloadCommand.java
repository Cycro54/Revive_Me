package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncConfigMsg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.network.PacketDistributor;

public class ReloadCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("revivemereload")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes(ReloadCommand::reload)
        );
    }

    private static int reload(CommandContext<CommandSourceStack> commandContext) {
        NetworkHandler.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncConfigMsg(ReviveMeConfig.serialize()));
        NetworkHandler.sendMessage(InvoText.translate("revive_me.commands.reload").getText(),
                true, commandContext.getSource().getEntity());

        return 1;
    }
}
