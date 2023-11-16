package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallenTimerEvent;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fml.network.PacketDistributor;

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
            if (!(commandContext.getSource().getEntity() instanceof PlayerEntity)){
                return 1;
            }
            caller = commandContext.getSource().getPlayerOrException();
        }
        FallenCapability cap = FallenCapability.GetFallCap(caller);

        if (!caller.isAlive()){
            return 1;
        }
        
        //This should fix the player if they are downed
        if (cap.isFallen()){
            caller.server.getPlayerList().broadcastMessage(
                    caller.getDisplayName().copy().append(new TranslationTextComponent("revive-me.commands.fix")), ChatType.CHAT, Util.NIL_UUID);

            //Set last damage source for later
            if (cap.getDamageSource() == null)
                cap.setDamageSource(DamageSource.OUT_OF_WORLD);

            //If they are out of time, smite them.
            if (cap.shouldDie()){
                caller.setInvulnerable(false);
                caller.hurt(cap.getDamageSource().bypassInvul().bypassArmor(), Float.MAX_VALUE);

                return 1;
            }

            //Set penalty type and amount
            cap.setPenalty(ReviveMeConfig.penaltyType, ReviveMeConfig.penaltyAmount);
            //System.out.println(ReviveMeConfig.penaltyType);

            //Make them invulnerable to all damage (besides void and creative of course.)
            caller.setInvulnerable(true);

            caller.removeAllEffects();

            //Make it so they can't move very fast.
            caller.addEffect(new EffectInstance(Effects.MOVEMENT_SLOWDOWN, 99999, 3, false, false, false));

            //Dismount the caller if riding something
            caller.stopRiding();

            //stop them from using an item if they are using one
            caller.stopUsingItem();

            //Finally send capability code to all players
            CompoundNBT nbt = new CompoundNBT();

            //System.out.println("Am I fallen?: " + FallenCapability.GetFallCap(caller).isFallen());
            if (cap.getOtherPlayer() != null) {

                PlayerEntity otherPlayer = caller.level.getPlayerByUUID(cap.getOtherPlayer());
                if (otherPlayer != null) {
                    FallenCapability otherCap = FallenCapability.GetFallCap(otherPlayer);
                    otherCap.resumeFallTimer();
                    otherCap.setOtherPlayer(null);

                    nbt.put(otherPlayer.getStringUUID(), otherCap.writeNBT());
                }
                cap.setOtherPlayer(null);
            }
            nbt.put(caller.getStringUUID(), cap.writeNBT());

            ServerPlayerEntity finalCaller = caller;
            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> finalCaller),
                    new SyncClientCapMsg(nbt));
        }
        
        //This should fix the player if they are no longer fallen
        else {
            FallenTimerEvent.revivePlayer(caller);

            caller.server.getPlayerList().broadcastMessage(
                    caller.getDisplayName().copy().append(new TranslationTextComponent("revive-me.commands.fix")), ChatType.CHAT, Util.NIL_UUID);
        }
        
        return 1;
    }
}
