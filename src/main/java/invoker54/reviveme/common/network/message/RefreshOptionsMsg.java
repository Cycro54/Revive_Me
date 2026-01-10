package invoker54.reviveme.common.network.message;

import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class RefreshOptionsMsg {
    //This is how the Network Handler will handle the message
    public static void handle(RefreshOptionsMsg msg, Supplier<NetworkEvent.Context> contextSupplier){
        NetworkEvent.Context context = contextSupplier.get();

        context.enqueueWork(() -> {
            boolean isOptionsDisabled = ReviveMeConfig.selfReviveOptions.isEmpty();
            if (isOptionsDisabled) return;
            if (!FallenItemScreenEvent.isItemScreenActive) return;

            FallenItemScreenEvent.switchReviveScreens();
        });
        context.setPacketHandled(true);
    }
}
