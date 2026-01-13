package invoker54.reviveme.common.event;


import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class ReviverEvents {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onHitWhileReviving(LivingDamageEvent event) {
        if (event.isCanceled()) return;
        if (!(event.getEntityLiving() instanceof PlayerEntity)) return;
        PlayerEntity reviverPlayer = (PlayerEntity) event.getEntityLiving();
        FallenCapability reviveCap = FallenCapability.get(reviverPlayer);
        if (reviveCap.isFallen()) return;
        if (!reviveCap.isReviver(reviveCap.getOtherPlayer())) return;
        PlayerEntity fallenEntity = reviverPlayer.level.getPlayerByUUID(reviveCap.getOtherPlayer());
        if (fallenEntity == null) return;
        FallenCapability fallCap = FallenCapability.get(fallenEntity);

        ReviveItemData itemData = ReviveItemData.getData(reviverPlayer.getMainHandItem(), ReviveItemData.USER.REVIVER);
        if (itemData != null){
            reviveCap.setProgress(reviverPlayer.level.getGameTime(), itemData.getReviveSeconds()/20d);
            fallCap.setProgress(reviverPlayer.level.getGameTime(), itemData.getReviveSeconds()/20d);
        }
        else {
            reviveCap.setProgress(reviverPlayer.level.getGameTime(), ReviveMeConfig.reviveTime);
            fallCap.setProgress(reviverPlayer.level.getGameTime(), ReviveMeConfig.reviveTime);
        }

        reviveCap.syncClient(true);
        fallCap.syncClient(true);
    }
}
