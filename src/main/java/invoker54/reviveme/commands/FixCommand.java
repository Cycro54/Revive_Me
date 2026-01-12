package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.invocore.client.util.InvoText;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import invoker54.reviveme.common.network.NetworkHandler;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class FixCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("revivemefix")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes(FixCommand::fixPlayer)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(FixCommand::fixPlayer)
                        )
        );
    }

    private static int fixPlayer(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
        ServerPlayer fallen;
        Entity caller = commandContext.getSource().getEntity();
        try {
            fallen = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!(commandContext.getSource().getEntity() instanceof ServerPlayer)){
                return 1;
            }
            fallen = (ServerPlayer) commandContext.getSource().getEntity();
        }
        FallenCapability cap = FallenCapability.get(fallen);

        if (!fallen.isAlive()){
            return 1;
        }

        NetworkHandler.sendMessage(InvoText.translate("revive-me.commands.fix",
                fallen.getDisplayName()).getText(),true, fallen);

        //This should fix the player if they are downed
        if (cap.isFallen()){
            DamageSource damageSource = cap.getDamageSource();
            if (damageSource == null) damageSource = DamageSource.OUT_OF_WORLD;

            //If they are out of time, smite them.
            if (cap.timeRanOut() && ReviveMeConfig.dieWhenTimerEnds){
                cap.forceDeath();
                return 1;
            }

            cap.setFallen(false);
            fallen.removeAllEffects();
            FallEvent.cancelEvent(fallen, damageSource);
        }

        //This should fix the player if they are no longer fallen
        else {
            ReviveMeConfig.configReviveData.revivePlayer(fallen, true, (Player) caller, "command");
        }

        return 1;
    }
}