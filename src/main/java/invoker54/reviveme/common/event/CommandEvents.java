package invoker54.reviveme.common.event;

import com.mojang.brigadier.context.ParsedCommandNode;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CommandEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void onCommand(CommandEvent event){
//        LOGGER.debug("START COMMAND EXECUTION");
        List<ParsedCommandNode<CommandSourceStack>> nodes = event.getParseResults().getContext().getNodes();
        if (nodes.isEmpty()) return;
        String rootName = nodes.get(0).getNode().getName();
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof Player player)) return;
        if (player == null) return;
//        LOGGER.debug("What's the root name? " + rootName);
//        LOGGER.debug("Who did the command" + player.getName().getString());
        if (!FallenCapability.GetFallCap(player).isFallen()) return;
        if (ReviveMeConfig.blockedCommands.contains("/") || ReviveMeConfig.blockedCommands.contains(rootName)){
            player.sendSystemMessage(Component.translatable("revive-me.chat.blocked_command"));
            event.setCanceled(true);
        }
    }
}
