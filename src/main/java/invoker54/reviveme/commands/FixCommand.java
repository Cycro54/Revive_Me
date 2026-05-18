package invoker54.reviveme.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import invoker54.reviveme.init.NetworkInit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class FixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("revivemefix")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes((context -> fixPlayer(context, ImmutableList.of(context.getSource().getPlayerOrException()))))
                        .then(Commands.argument("players", EntityArgument.players())
                                .executes((context -> fixPlayer(context, new ArrayList<>(EntityArgument.getPlayers(context, "players")))))
                        )
        );
    }

    private static int fixPlayer(CommandContext<CommandSourceStack> commandContext, List<? extends Player> players) throws CommandSyntaxException {
        int fixCount = 0;
        Entity commandEntity = commandContext.getSource().getEntity();
        Player fixer = commandEntity instanceof Player ? (Player) commandEntity : null;

        for (Player player : players){
            if (!player.isAlive()) continue;
            FallenData cap = FallenData.get(player);
            NetworkInit.sendMessage(InvoText.translate("revive_me.commands.fix",
                    player.getDisplayName()).getText(),true, player);
            fixCount++;

            //This should fix the player if they are downed
            if (cap.isFallen()){
                DamageSource damageSource = cap.getDamageSource();
                if (damageSource == null) damageSource = player.damageSources().fellOutOfWorld();

                //If they are out of time, smite them.
                if (cap.timeRanOut() && ReviveMeConfig.dieWhenTimerEnds){
                    cap.forceDeath();
                    continue;
                }

                cap.setFallen(false);
                cap.removeOriginalEffects(true);
                FallEvent.cancelEvent(player, damageSource);
            }

            //This should fix the player if they are no longer fallen
            else {
                ReviveMeConfig.configReviveData.revivePlayer(player, true, fixer, "command");
            }
        }
        if (commandEntity == null) return fixCount;

        InvoText countText = InvoText.translate("revive_me.commands.fix.count", InvoText.literal(""+fixCount).getText(),
                InvoText.translate(fixCount == 1 ? "revive_me.commands.count.single" : "revive_me.commands.count.multiple").getText());
        NetworkInit.sendMessage(countText.getText(), true, commandEntity);

        return fixCount;
    }
}
