package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.SyncClientCapMsg;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class ReviverEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHitWhileReviving(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof Player reviverPlayer)) return;
        FallenData reviveCap = FallenData.get(reviverPlayer);
        if (reviveCap.isFallen()) return;
        if (!reviveCap.isReviver(reviveCap.getOtherPlayer())) return;
        Player fallenEntity = reviverPlayer.level().getPlayerByUUID(reviveCap.getOtherPlayer());
        if (fallenEntity == null) return;
        FallenData fallCap = FallenData.get(fallenEntity);

        reviveCap.setProgress(reviverPlayer.level().getGameTime(), ReviveMeConfig.reviveTime);
        fallCap.setProgress(reviverPlayer.level().getGameTime(), ReviveMeConfig.reviveTime);

        CompoundTag nbt = new CompoundTag();
        nbt.put(reviverPlayer.getStringUUID(), reviveCap.writeNBT());
        nbt.put(fallenEntity.getStringUUID(), fallCap.writeNBT());

        PacketDistributor.sendToPlayer((ServerPlayer) fallenEntity, new SyncClientCapMsg(reviverPlayer.getStringUUID(), reviveCap.writeNBT()));
        PacketDistributor.sendToPlayer((ServerPlayer) reviverPlayer, new SyncClientCapMsg(fallenEntity.getStringUUID(), fallCap.writeNBT()));
    }
}
