package invoker54.reviveme;

import com.mojang.brigadier.CommandDispatcher;
import invoker54.reviveme.commands.FixCommand;
import invoker54.reviveme.commands.ReviveCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class CommandInit {

    @SubscribeEvent
    public static void onRegisterCommandEvent(RegisterCommandsEvent event){
        CommandDispatcher<CommandSourceStack> commandDispatcher = event.getDispatcher();

        ReviveCommand.register(commandDispatcher);
        FixCommand.register(commandDispatcher);
    }
}
