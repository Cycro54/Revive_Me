package invoker54.reviveme.common.event;

import com.mojang.brigadier.context.ParsedCommandNode;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.TranslationTextComponent;
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
        List<ParsedCommandNode<CommandSource>> nodes = event.getParseResults().getContext().getNodes();
        if (nodes.isEmpty()) return;
        String rootName = nodes.get(0).getNode().getName();
        PlayerEntity player = (PlayerEntity) event.getParseResults().getContext().getSource().getEntity();
        if (player == null) return;
//        LOGGER.debug("What's the root name? " + rootName);
//        LOGGER.debug("Who did the command" + player.getName().getString());
        if (!FallenCapability.GetFallCap(player).isFallen()) return;
        if (ReviveMeConfig.blockedCommands.contains(rootName)){
            player.sendMessage(new TranslationTextComponent("revive-me.chat.blocked_command"), Util.NIL_UUID);
            event.setCanceled(true);
        }
    }
}
