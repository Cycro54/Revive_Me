package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class ReviverEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHitWhileReviving(LivingDamageEvent.Pre event) {
        if (!ReviveMeConfig.resetReviveOnHit) return;
        if (!(event.getEntity() instanceof Player reviverPlayer)) return;
        FallenData reviveCap = FallenData.get(reviverPlayer);
        if (reviveCap.isFallen()) return;
        if (!reviveCap.isReviver(reviveCap.getOtherPlayer())) return;
        Player fallenEntity = reviverPlayer.level().getPlayerByUUID(reviveCap.getOtherPlayer());
        if (fallenEntity == null) return;
        FallenData fallCap = FallenData.get(fallenEntity);

        ReviveItemData itemData = ReviveItemData.getData(reviverPlayer.getMainHandItem(), ReviveItemData.USER.REVIVER);
        if (itemData != null){
            reviveCap.setProgress(reviverPlayer.level().getGameTime(), itemData.getReviveSeconds()/20d);
            fallCap.setProgress(reviverPlayer.level().getGameTime(), itemData.getReviveSeconds()/20d);
        }
        else {
            reviveCap.setProgress(reviverPlayer.level().getGameTime(), ReviveMeConfig.reviveTime);
            fallCap.setProgress(reviverPlayer.level().getGameTime(), ReviveMeConfig.reviveTime);
        }

        reviveCap.syncClient(true);
        fallCap.syncClient(true);
    }
}
