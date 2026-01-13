package invoker54.reviveme.common.network.message;

import invoker54.invocore.common.MathUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.SoundInit;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CallForHelpMsg {
    private static final ModLogger LOGGER = ModLogger.getLogger(CallForHelpMsg.class, ReviveMeConfig.debugMode);
    public final boolean isSneaking;

    public CallForHelpMsg(boolean isSneaking){
        this.isSneaking = isSneaking;
    }

    public static void encode(CallForHelpMsg msg, PacketBuffer buffer){
        buffer.writeBoolean(msg.isSneaking);
    }

    public static CallForHelpMsg decode(PacketBuffer buffer){
        return new CallForHelpMsg(buffer.readBoolean());
    }

    public static void handle(CallForHelpMsg msg, Supplier<NetworkEvent.Context> contextSupplier) {
        contextSupplier.get().enqueueWork(() -> {
            PlayerEntity player = contextSupplier.get().getSender();
            if (player == null) return;
            if (player.isDeadOrDying()) return;
            FallenCapability cap = FallenCapability.get(player);

            cap.callForHelp(msg.isSneaking);

            cap.syncClient(false);

            float pitch = MathUtil.randomFloat(0.7f, 1.4F);
            float volume = MathUtil.randomFloat(1.0f, 1.5F);

            player.playSound(SoundInit.CALL_FOR_HELP, volume, pitch);
        });
    }

}