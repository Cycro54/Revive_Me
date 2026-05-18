package invoker54.reviveme.commands;

import com.google.common.collect.ImmutableList;
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

import java.util.ArrayList;
import java.util.List;

public class ReviveCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("reviveme")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes((context -> revivePlayer(context, ImmutableList.of(context.getSource().getPlayerOrException()))))
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes((context -> revivePlayer(context, new ArrayList<>(EntityArgument.getPlayers(context, "players")))))
                        )
        );
    }

    private static int revivePlayer(CommandContext<CommandSource> commandContext, List<? extends PlayerEntity> players) throws CommandSyntaxException {
        int reviveCount = 0;
        Entity commandEntity = commandContext.getSource().getEntity();
        PlayerEntity reviver = commandEntity instanceof PlayerEntity ? (PlayerEntity) commandEntity : null;

        for (PlayerEntity fallen : players){
            FallenCapability cap = FallenCapability.get(fallen);
            if (fallen.isDeadOrDying() || !cap.isFallen()){
                InvoText failTxt = InvoText.translate("revive-me.commands.revive_fail", fallen.getDisplayName());
                NetworkHandler.sendMessage(failTxt.getText(), true, fallen);
                continue;
            }

            ReviveMeConfig.configReviveData.revivePlayer(fallen, true, reviver, "command");
            reviveCount++;
        }
        if (commandEntity == null) return reviveCount;

        InvoText countText = InvoText.translate("revive-me.commands.revive.count", InvoText.literal(""+reviveCount).getText(),
                InvoText.translate(reviveCount == 1 ? "revive-me.commands.count.single" : "revive-me.commands.count.multiple").getText());
        NetworkHandler.sendMessage(countText.getText(), true, commandEntity);

        return reviveCount;
    }
}
