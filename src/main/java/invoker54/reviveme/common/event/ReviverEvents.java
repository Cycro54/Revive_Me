package invoker54.reviveme.common.event;


import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class ReviverEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHitWhileReviving(LivingDamageEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getEntity() instanceof Player reviverPlayer)) return;
        FallenCapability reviveCap = FallenCapability.GetFallCap(reviverPlayer);
        if (reviveCap.isFallen()) return;
        if (!reviveCap.isReviver(reviveCap.getOtherPlayer())) return;
        Player fallenEntity = reviverPlayer.level().getPlayerByUUID(reviveCap.getOtherPlayer());
        if (fallenEntity == null) return;
        FallenCapability fallCap = FallenCapability.GetFallCap(fallenEntity);

        reviveCap.setProgress(reviverPlayer.level().getGameTime(), ReviveMeConfig.reviveTime);
        fallCap.setProgress(reviverPlayer.level().getGameTime(), ReviveMeConfig.reviveTime);

        CompoundTag nbt = new CompoundTag();
        nbt.put(reviverPlayer.getStringUUID(), reviveCap.writeNBT());
        nbt.put(fallenEntity.getStringUUID(), fallCap.writeNBT());

        NetworkHandler.sendToPlayer(fallenEntity, new SyncClientCapMsg(nbt));
        NetworkHandler.sendToPlayer(reviverPlayer, new SyncClientCapMsg(nbt));
    }
}
