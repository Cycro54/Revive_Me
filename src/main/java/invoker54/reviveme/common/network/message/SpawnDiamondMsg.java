package invoker54.reviveme.common.network.message;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SpawnDiamondMsg {

    //This is how the Network Handler will handle the message
    public static void handle(SpawnDiamondMsg message, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            ServerPlayerEntity player = context.getSender();
            player.inventory.add(new ItemStack(Items.DIAMOND));

            World world = player.getLevel();
            world.setBlockAndUpdate(player.getEntity().blockPosition().below(), Blocks.DIAMOND_BLOCK.defaultBlockState());

            //System.out.println("Diamond block made, your welcome.");
        });
        context.setPacketHandled(true);
    }
}
