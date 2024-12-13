package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.event.FallEvent;
import invoker54.reviveme.common.event.FallenTimerEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FixCommand {
    private static final Logger LOGGER = LogManager.getLogger();

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
        ServerPlayer caller;
        try {
            caller = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!commandContext.getSource().isPlayer()){
                return 1;
            }
            caller = commandContext.getSource().getPlayer();
        }
        FallenCapability cap = FallenCapability.GetFallCap(caller);

        if (!caller.isAlive()){
            return 1;
        }

        //TODO: Remove this in future versions.
        caller.setInvulnerable(false);
        
        //This should fix the player if they are downed
        if (cap.isFallen()){
            caller.server.getPlayerList().broadcastSystemMessage(
                    caller.getDisplayName().copy().append(Component.translatable("revive-me.commands.fix")), false);

            DamageSource damageSource = cap.getDamageSource();
            if (damageSource == null) damageSource = DamageSource.OUT_OF_WORLD;

            //If they are out of time, smite them.
            if (cap.shouldDie()){
                cap.kill(caller);
                return 1;
            }

            cap.setFallen(false);
            FallEvent.cancelEvent(caller, damageSource);
        }
        
        //This should fix the player if they are no longer fallen
        else {
            FallenTimerEvent.revivePlayer(caller);

            caller.server.getPlayerList().broadcastSystemMessage(
                    caller.getDisplayName().copy().append(Component.translatable("revive-me.commands.fix")), false);
        }
        
        return 1;
    }
}
