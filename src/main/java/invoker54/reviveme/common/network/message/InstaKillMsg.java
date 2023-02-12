package invoker54.reviveme.common.network.message;

import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;
import java.util.function.Supplier;

public class InstaKillMsg {

    private final UUID playerID;

    public InstaKillMsg(UUID playerID){
        this.playerID = playerID;
    }

    public static void Encode(InstaKillMsg msg, FriendlyByteBuf buffer){
        buffer.writeUUID(msg.playerID);
    }

    public static InstaKillMsg Decode(FriendlyByteBuf buffer){
        return new InstaKillMsg(buffer.readUUID());
    }

    //This is how the Network Handler will handle the message
    public static void handle(InstaKillMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            //System.out.println("Who's the sender? : " + context.getSender().getName());
            //System.out.println("Does it match send UUID? : " + (context.getSender().getUUID().equals(msg.playerID)));
            PlayerList playerList = ServerLifecycleHooks.getCurrentServer().getPlayerList();
            Player player = playerList.getPlayer(msg.playerID);

            if (player != null){
                FallenCapability cap = FallenCapability.GetFallCap(player);
                //Make them vulnerable
                player.setInvulnerable(false);

                //Then make them take damage from the saved damage source
                player.hurt(cap.getDamageSource().bypassArmor().bypassInvul(), Float.MAX_VALUE);
            }
        });

        context.setPacketHandled(true);
    }
}
