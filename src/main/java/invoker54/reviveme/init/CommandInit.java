package invoker54.reviveme.init;

import com.mojang.brigadier.CommandDispatcher;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.commands.FixCommand;
import invoker54.reviveme.commands.ReviveCommand;
import net.minecraft.commands.CommandSourceStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class CommandInit {

    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event){
        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();

        ReviveCommand.register(commandDispatcher);
        FixCommand.register(commandDispatcher);
    }
}