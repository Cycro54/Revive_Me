package invoker54.reviveme.common.network;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.message.*;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkHandler {
    //Increment the first number if you add new stuff to NetworkHandler class
    //Increment the middle number each time you make a new Message
    //Increment the last number each time you fix a bug
    private static final String PROTOCOL_VERSION = "1.6.0";

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(

            //Name of the channel
            new ResourceLocation(ReviveMe.MOD_ID, "network"),
            //Supplier<String> that returns protocol version
            () -> PROTOCOL_VERSION,
            //Checks incoming network protocol version for client (so it's pretty much PROTOCOL_VERSION == INCOMING_PROTOCOL_VERSION)
            PROTOCOL_VERSION::equals,
            //Checks incoming network protocol version for server (If they don't equal, it won't work.)
            PROTOCOL_VERSION::equals
    );

    public static void init(){
        //This is how you avoid sending anything to the server when you don't need to.
        // (change encode with an empty lambda, and just make decode create a new instance of the target message class)
        //INSTANCE.registerMessage(0, SpawnDiamondMsg.class, (message, buf) -> {}, it -> new SpawnDiamondMsg(), SpawnDiamondMsg::handle);
        INSTANCE.registerMessage(0, SyncClientCapMsg.class, SyncClientCapMsg::Encode, SyncClientCapMsg::Decode, SyncClientCapMsg::handle);
        INSTANCE.registerMessage(1, RestartDeathTimerMsg.class, RestartDeathTimerMsg::Encode, RestartDeathTimerMsg::Decode, RestartDeathTimerMsg::handle);
        INSTANCE.registerMessage(2, SyncConfigMsg.class, SyncConfigMsg::Encode, SyncConfigMsg::Decode, SyncConfigMsg::handle);
        INSTANCE.registerMessage(3, CallForHelpMsg.class, (msg,buf)->{}, it -> new CallForHelpMsg(), CallForHelpMsg::handle);
        INSTANCE.registerMessage(4, SelfReviveMsg.class, SelfReviveMsg::encode, SelfReviveMsg::decode, SelfReviveMsg::handle);
    }

    //Custom method used to send data to players
    public static void sendToPlayer(Player player, Object message) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayer) player), message);
    }

    public static void sendMessage(MutableComponent component, boolean isCommand, Entity trackedEntity){
        if (ReviveMeConfig.silenceCommandMessages && isCommand) return;
        if (ReviveMeConfig.silenceRegularMessages && !isCommand) return;

        if (ReviveMeConfig.universalChatMessages) {
            trackedEntity.getServer().getPlayerList().broadcastMessage(component, ChatType.CHAT, Util.NIL_UUID);
        }
        else {
            ((ServerChunkCache) trackedEntity.level.getChunkSource()).broadcastAndSend(trackedEntity, new ClientboundChatPacket(component, ChatType.CHAT, Util.NIL_UUID));
        }

    }
}
