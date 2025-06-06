package invoker54.reviveme.common.network.message;

import invoker54.invocore.common.MathUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.init.SoundInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.Supplier;

public class CallForHelpMsg {
    private static final ModLogger LOGGER = ModLogger.getLogger(CallForHelpMsg.class, ReviveMeConfig.debugMode);

    public static void handle(CallForHelpMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            PlayerEntity player = contextSupplier.get().getSender();
            if (player == null) return;
            if (player.isDeadOrDying()) return;
            FallenCapability cap = FallenCapability.GetFallCap(player);
            cap.callForHelp();
            CompoundNBT nbt = new CompoundNBT();
            nbt.put(player.getStringUUID(), cap.writeNBT());

            NetworkHandler.INSTANCE.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new SyncClientCapMsg(nbt));

            float pitch = MathUtil.randomFloat(0.7f, 1.4F);
            float volume = MathUtil.randomFloat(1.0f, 1.5F);

            player.playSound(SoundInit.CALL_FOR_HELP, volume, pitch);
        });
    }

}