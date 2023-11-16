package invoker54.reviveme.init;

import com.mojang.brigadier.CommandDispatcher;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.commands.FixCommand;
import invoker54.reviveme.commands.ReviveCommand;
import net.minecraft.command.CommandSource;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CommandInit {

    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event){
        CommandDispatcher<CommandSource> commandDispatcher = event.getDispatcher();

        ReviveCommand.register(commandDispatcher);
        FixCommand.register(commandDispatcher);
    }
}
