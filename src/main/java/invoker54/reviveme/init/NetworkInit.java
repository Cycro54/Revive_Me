package invoker54.reviveme.init;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.*;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Locale;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public class NetworkInit {
    private static final Logger LOGGER = LogManager.getLogger();


    @SubscribeEvent
    public static void registerNetwork(final RegisterPayloadHandlersEvent event){
        PayloadRegistrar registrar = event.registrar("7");
        InstaKillMsg.register(registrar);
        RestartDeathTimerMsg.register(registrar);
        SelfReviveMsg.register(registrar);
        SyncClientCapMsg.register(registrar);
        SyncConfigMsg.register(registrar);
        CallForHelpMsg.register(registrar);
    }

    public static String createID(Class<?> msgClass){
        List<String> list = List.of(msgClass.getSimpleName().split("/(?=[A-Z])/"));
        StringBuilder id = new StringBuilder();
        for (String s : list){
            if (!id.isEmpty()) id.append("_");
            id.append(s);
        }
        return id.toString().toLowerCase(Locale.ROOT);
    }


    public static void sendMessage(MutableComponent component, boolean isCommand, Entity trackedEntity){
        if (ReviveMeConfig.silenceCommandMessages && isCommand) return;
        if (ReviveMeConfig.silenceRegularMessages && !isCommand) return;

        if (ReviveMeConfig.universalChatMessages) {
            trackedEntity.getServer().getPlayerList().broadcastSystemMessage(component, false);
        }
        else {
            ((ServerChunkCache) trackedEntity.level().getChunkSource()).broadcastAndSend(trackedEntity, new ClientboundSystemChatPacket(component, false));
        }

    }
}
