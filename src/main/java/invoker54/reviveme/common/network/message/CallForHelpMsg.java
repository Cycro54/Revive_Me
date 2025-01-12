package invoker54.reviveme.common.network.message;

import invoker54.invocore.common.MathUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.init.SoundInit;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

public class CallForHelpMsg {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void handle(CallForHelpMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            Player player = contextSupplier.get().getSender();
            if (player == null) return;
            if (player.isDeadOrDying()) return;
            FallenCapability cap = FallenCapability.GetFallCap(player);
            cap.callForHelp();
            CompoundTag nbt = new CompoundTag();
            nbt.put(player.getStringUUID(), cap.writeNBT());

            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncClientCapMsg(nbt));

            float pitch = MathUtil.randomFloat(0.7f, 1.4F);
            float volume = MathUtil.randomFloat(1.0f, 1.5F);

            player.playSound(SoundInit.CALL_FOR_HELP, volume, pitch);
        });
    }

}