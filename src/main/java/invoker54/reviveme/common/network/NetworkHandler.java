package invoker54.reviveme.common.network;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.network.message.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {
    //Increment the first number if you add new stuff to NetworkHandler class
    //Increment the middle number each time you make a new Message
    //Increment the last number each time you fix a bug
    private static final String PROTOCOL_VERSION = "1.3.0";

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
        INSTANCE.registerMessage(1, SyncServerCapMsg.class, SyncServerCapMsg::Encode, SyncServerCapMsg::Decode, SyncServerCapMsg::handle);
        INSTANCE.registerMessage(2, InstaKillMsg.class, InstaKillMsg::Encode, InstaKillMsg::Decode, InstaKillMsg::handle);
    }

    //Custom method used to send data to players
    public static void sendToPlayer(PlayerEntity player, Object message) {
        NetworkHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player), message);
    }
}
