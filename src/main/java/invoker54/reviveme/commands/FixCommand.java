package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.event.FallEvent;
import invoker54.reviveme.common.event.FallenTimerEvent;
import invoker54.reviveme.common.network.NetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TranslationTextComponent;

public class FixCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(
                Commands.literal("revivemefix")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes(FixCommand::fixPlayer)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(FixCommand::fixPlayer)
                        )
        );
    }

    private static int fixPlayer(CommandContext<CommandSource> commandContext) throws CommandSyntaxException {
        ServerPlayerEntity caller;
        try {
            caller = EntityArgument.getPlayer(commandContext, "player");
        }
        catch (Exception e){
            if (!(commandContext.getSource().getEntity() instanceof ServerPlayerEntity)){
                return 1;
            }
            caller = (ServerPlayerEntity) commandContext.getSource().getEntity();
        }
        FallenCapability cap = FallenCapability.GetFallCap(caller);

        if (!caller.isAlive()){
            return 1;
        }

        //TODO: Remove this in future versions.
        caller.setInvulnerable(false);

        NetworkHandler.sendMessage((new TranslationTextComponent("revive-me.commands.fix")),
                true, caller);

        //This should fix the player if they are downed
        if (cap.isFallen()){
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
            FallenTimerEvent.revivePlayer(caller, true);
        }

        return 1;
    }
}
