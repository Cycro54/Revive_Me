package invoker54.reviveme.common.event;

import com.mojang.brigadier.context.ParsedCommandNode;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;

import java.util.List;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CommandEvents {
    private static final ModLogger LOGGER = ModLogger.getLogger(CommandEvents.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void onCommand(CommandEvent event){
//        LOGGER.debug("START COMMAND EXECUTION");
        List<ParsedCommandNode<CommandSourceStack>> nodes = event.getParseResults().getContext().getNodes();
        if (nodes.isEmpty()) return;
        String rootName = nodes.get(0).getNode().getName();
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof ServerPlayer player)) return;
        if (player == null) return;
//        LOGGER.debug("What's the root name? " + rootName);
//        LOGGER.debug("Who did the command" + player.getName().getString());
        if (!FallenData.get(player).isFallen()) return;
        boolean whitelist = ReviveMeConfig.blockedCommands.contains("//");
//        LOGGER.warn("Is it whitelist? " + whitelist);
        boolean blockEverything = ReviveMeConfig.blockedCommands.contains("/");
//        LOGGER.warn("Is it blocking everything? " + blockEverything);
        boolean isCommandInList = ReviveMeConfig.blockedCommands.stream().anyMatch(s -> !s.isEmpty() && rootName.contains(s));
        if (blockEverything || isCommandInList && !whitelist || !isCommandInList && whitelist){
            if (!ReviveMeConfig.silenceCommandMessages) {
                player.sendSystemMessage(Component.translatable("revive_me.chat.blocked_command"), false);
            }
            event.setCanceled(true);
        }
    }
}
