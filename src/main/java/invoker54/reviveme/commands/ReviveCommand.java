package invoker54.reviveme.commands;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ReviveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("reviveme")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes((context -> revivePlayer(context, ImmutableList.of(context.getSource().getPlayerOrException()))))
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes((context -> revivePlayer(context, new ArrayList<>(EntityArgument.getPlayers(context, "players")))))
                        )
        );
    }

    private static int revivePlayer(CommandContext<CommandSourceStack> commandContext, List<? extends Player> players) throws CommandSyntaxException {
        int reviveCount = 0;
        Entity commandEntity = commandContext.getSource().getEntity();
        Player reviver = commandEntity instanceof Player ? (Player) commandEntity : null;

        for (Player fallen : players){
            FallenData cap = FallenData.get(fallen);
            if (fallen.isDeadOrDying() || !cap.isFallen()){
                InvoText failTxt = InvoText.translate("revive_me.commands.revive_fail", fallen.getDisplayName());
                NetworkInit.sendMessage(failTxt.getText(), true, fallen);
                continue;
            }

            ReviveMeConfig.configReviveData.revivePlayer(fallen, true, reviver, "command");
            reviveCount++;
        }
        if (commandEntity == null) return reviveCount;

        InvoText countText = InvoText.translate("revive_me.commands.revive.count", InvoText.literal(""+reviveCount).getText(),
                InvoText.translate(reviveCount == 1 ? "revive_me.commands.count.single" : "revive_me.commands.count.multiple").getText());
        NetworkInit.sendMessage(countText.getText(), true, commandEntity);

        return reviveCount;
    }
}
