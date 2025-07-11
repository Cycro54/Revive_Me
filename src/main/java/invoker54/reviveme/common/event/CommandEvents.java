package invoker54.reviveme.common.event;

import com.mojang.brigadier.context.ParsedCommandNode;
import invoker54.invocore.common.ModLogger;
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

import java.util.List;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CommandEvents {
    private static final ModLogger LOGGER = ModLogger.getLogger(CommandEvents.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void onCommand(CommandEvent event){
//        LOGGER.debug("START COMMAND EXECUTION");
        List<ParsedCommandNode<CommandSource>> nodes = event.getParseResults().getContext().getNodes();
        if (nodes.isEmpty()) return;
        String rootName = nodes.get(0).getNode().getName();
        if (!(event.getParseResults().getContext().getSource().getEntity() instanceof PlayerEntity)) return;
        PlayerEntity player = (PlayerEntity) event.getParseResults().getContext().getSource().getEntity();
        if (player == null) return;
//        LOGGER.debug("What's the root name? " + rootName);
//        LOGGER.debug("Who did the command" + player.getName().getString());
        if (!FallenCapability.GetFallCap(player).isFallen()) return;
        boolean whitelist = ReviveMeConfig.blockedCommands.contains("//");
//        LOGGER.warn("Is it whitelist? " + whitelist);
        boolean blockEverything = ReviveMeConfig.blockedCommands.contains("/");
//        LOGGER.warn("Is it blocking everything? " + blockEverything);
        boolean isCommandInList = ReviveMeConfig.blockedCommands.stream().anyMatch(s -> !s.isEmpty() && rootName.contains(s));
        if (blockEverything || isCommandInList && !whitelist || !isCommandInList && whitelist){
            if (!ReviveMeConfig.silenceCommandMessages) {
                player.sendMessage(new TranslationTextComponent("revive-me.chat.blocked_command"), Util.NIL_UUID);
            }
            event.setCanceled(true);
        }
    }
}
