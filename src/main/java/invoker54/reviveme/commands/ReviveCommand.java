package invoker54.reviveme.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraftforge.network.PacketDistributor;

public class ReviveCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("reviveme")
                        .requires((commandSource -> commandSource.hasPermission(2)))
                        .executes(ReviveCommand::revivePlayer)
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ReviveCommand::revivePlayer)
                        )
        );
    }

    private static int revivePlayer(CommandContext<CommandSourceStack> commandContext) throws CommandSyntaxException {
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

        if (caller.isDeadOrDying() || !cap.isFallen()){
            caller.server.getPlayerList().broadcastSystemMessage(
                    caller.getDisplayName().copy().append(Component.translatable("revive-me.commands.revive_fail").getString()), false);
            return 1;
        }

        //region Set the revived players health
        float healAmount;
        if (ReviveMeConfig.revivedHealth == 0){
            healAmount = caller.getMaxHealth();
        }
        //Percentage
        else if (ReviveMeConfig.revivedHealth > 0 && ReviveMeConfig.revivedHealth < 1){
            healAmount = (float) (caller.getMaxHealth() * ReviveMeConfig.revivedHealth);
        }
        //Flat value
        else {
            healAmount = ReviveMeConfig.revivedHealth.floatValue();
        }
        caller.setHealth(healAmount);
        //endregion

        //region Set the revived players Food
        float foodAmount;
        if (ReviveMeConfig.revivedFood == 0){
            foodAmount = 40;
        }
        //Percentage
        else if (ReviveMeConfig.revivedFood > 0 && ReviveMeConfig.revivedFood < 1){
            foodAmount = (float) (40 * ReviveMeConfig.revivedFood);
        }
        //Flat value
        else {
            foodAmount = ReviveMeConfig.revivedFood.floatValue();
        }
        //Now set their food level
        caller.getFoodData().eat((int) Math.min(20, foodAmount), 0);
        //Then their saturation
        caller.getFoodData().eat(1, Math.max(0, foodAmount - 20)/2);
        //endregion

        //Remove all potion effects
        caller.removeAllEffects();

        //Make it so they aren't invulnerable anymore
        caller.setInvulnerable(false);

        //Add invulnerability if it isn't 0
        if (ReviveMeConfig.reviveInvulnTime != 0) {
            caller.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
            caller.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, (int) (ReviveMeConfig.reviveInvulnTime * 20), 5));
        }

        cap.setFallen(false);
        caller.setPose(Pose.STANDING);

        CompoundTag nbt = new CompoundTag();
        nbt.put(caller.getStringUUID(), cap.writeNBT());

        ServerPlayer finalCaller = caller;
        NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> finalCaller),
                    new SyncClientCapMsg(nbt));

        caller.server.getPlayerList().broadcastSystemMessage(
                caller.getDisplayName().copy().append(Component.translatable("revive-me.commands.revive_pass").getString()), false);
        return 1;
    }
}