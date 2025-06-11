package invoker54.reviveme.common.event;

import com.mojang.brigadier.context.ParsedCommandNode;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CommandEvents {
    private static final ModLogger LOGGER = ModLogger.getLogger(CommandEvents.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void onCommand(CommandEvent event) {
//        LOGGER.debug("START COMMAND EXECUTION");
        List<ParsedCommandNode<CommandSourceStack>> nodes = event.getParseResults().getContext().getNodes();
        if (nodes.isEmpty()) return;
        String rootName = nodes.get(0).getNode().getName();
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof Player player)) return;
        if (player == null) return;
//        LOGGER.debug("What's the root name? " + rootName);
//        LOGGER.debug("Who did the command" + player.getName().getString());
        if (!FallenCapability.GetFallCap(player).isFallen()) return;
        if (ReviveMeConfig.blockedCommands.contains("/") || ReviveMeConfig.blockedCommands.contains(rootName)) {
            if (!ReviveMeConfig.silenceCommandMessages) {
                player.sendSystemMessage(Component.translatable("revive-me.chat.blocked_command"));
            }
            event.setCanceled(true);
        }
    }
}
