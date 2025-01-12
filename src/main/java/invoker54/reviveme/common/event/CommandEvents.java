package invoker54.reviveme.common.event;

import com.mojang.brigadier.context.ParsedCommandNode;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.CommandEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CommandEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onCommand(CommandEvent event){
//        LOGGER.debug("START COMMAND EXECUTION");
        List<ParsedCommandNode<CommandSourceStack>> nodes = event.getParseResults().getContext().getNodes();
        if (nodes.isEmpty()) return;
        String rootName = nodes.get(0).getNode().getName();
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof Player)) return;
        Player player = (Player) event.getParseResults().getContext().getSource().getEntity();
        if (player == null) return;
//        LOGGER.debug("What's the root name? " + rootName);
//        LOGGER.debug("Who did the command" + player.getName().getString());
        if (!FallenData.get(player).isFallen()) return;
        if (ReviveMeConfig.blockedCommands.contains("/") || ReviveMeConfig.blockedCommands.contains(rootName)){
            if (!ReviveMeConfig.silenceCommandMessages) {
                player.sendSystemMessage(Component.translatable("revive-me.chat.blocked_command"));
            }
            event.setCanceled(true);
        }
    }
}
