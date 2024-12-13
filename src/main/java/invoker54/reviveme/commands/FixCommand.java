package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.event.FallEvent;
import invoker54.reviveme.common.event.FallenTimerEvent;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
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
        ServerPlayer caller;
        try {
            caller = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!(commandContext.getSource().getEntity() instanceof ServerPlayer)){
                return 1;
            }
            caller = (ServerPlayer) commandContext.getSource().getEntity();
        }
        FallenCapability cap = FallenCapability.GetFallCap(caller);

        if (!caller.isAlive()){
            return 1;
        }
        
        //This should fix the player if they are downed
        if (cap.isFallen()){
            for(Player player1 : caller.server.getPlayerList().getPlayers()){
                player1.sendMessage(caller.getDisplayName().copy().append
                        (new TranslatableComponent("revive-me.commands.fix").getString()), Util.NIL_UUID);
            }

            DamageSource damageSource = cap.getDamageSource();
            if (damageSource == null) damageSource = DamageSource.OUT_OF_WORLD;
            //TODO: Remove this in future versions.
            caller.setInvulnerable(false);

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

            for(Player player1 : caller.server.getPlayerList().getPlayers()){
                player1.sendMessage(caller.getDisplayName().copy().append
                        (new TranslatableComponent("revive-me.commands.fix").getString()), Util.NIL_UUID);
            }
        }
        
        return 1;
    }
}
