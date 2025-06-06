package invoker54.reviveme.common.network;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.message.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SChatPacket;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.world.server.ServerChunkProvider;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

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
        INSTANCE.registerMessage(2, InstaKillMsg.class, InstaKillMsg::Encode, InstaKillMsg::Decode, InstaKillMsg::handle);
        INSTANCE.registerMessage(3, SyncConfigMsg.class, SyncConfigMsg::Encode, SyncConfigMsg::Decode, SyncConfigMsg::handle);
        INSTANCE.registerMessage(4, CallForHelpMsg.class, (msg,buf)->{}, it -> new CallForHelpMsg(), CallForHelpMsg::handle);
        INSTANCE.registerMessage(5, SelfReviveMsg.class, SelfReviveMsg::encode, SelfReviveMsg::decode, SelfReviveMsg::handle);
    }

    //Custom method used to send data to players
    public static void sendToPlayer(PlayerEntity player, Object message) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
    }

    public static void sendMessage(IFormattableTextComponent component, boolean isCommand, Entity trackedEntity){
         if (ReviveMeConfig.silenceCommandMessages && isCommand) return;
         if (ReviveMeConfig.silenceRegularMessages && !isCommand) return;

         if (ReviveMeConfig.universalChatMessages) {
             trackedEntity.getServer().getPlayerList().broadcastMessage(component, ChatType.CHAT, Util.NIL_UUID);
         }
         else {
             ((ServerChunkProvider) trackedEntity.level.getChunkSource()).broadcastAndSend(trackedEntity, new SChatPacket(component, ChatType.CHAT, Util.NIL_UUID));
         }

    }
}
