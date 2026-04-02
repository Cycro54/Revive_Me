package invoker54.reviveme.client.event;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.RestartDeathTimerMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.UUID;

@EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class RevivePlayerActionsEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(RevivePlayerActionsEvent.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void reviveCheck(PlayerTickEvent.Pre event){
        if (!event.getEntity().level().isClientSide()) return;
        Minecraft mc = ClientUtil.getMinecraft();
        if(event.getEntity() != mc.player) return;

        FallenData myCap = FallenData.get(mc.player);
        UUID myUUID = mc.player.getUUID();

        if (myCap.getOtherPlayer() == null) return;

        if (myCap.isFallen()) return;

        Player targPlayer = mc.level.getPlayerByUUID(myCap.getOtherPlayer());

        boolean cancelEvent;

        //Check if it's a player
        //System.out.println("Player entity instance? : " + (mc.crosshairPickEntity instanceof Player));

        cancelEvent = targPlayer == null;

        //Check if that player is being revived by them
        if (!cancelEvent) {
//            //System.out.println("Someone I'm reviving? : " + (FallenData.get((Player)mc.crosshairPickEntity).
//                    compareUUID(myUUID)));
//            cancelEvent = !(FallenData.get((Player) mc.crosshairPickEntity).
//                    isReviver(myUUID));
            cancelEvent = (mc.crosshairPickEntity != targPlayer && (ReviveMeConfig.reviverMustLook ||
                    ClientUtil.getPlayer().distanceTo(targPlayer) > ClientUtil.getPlayer().getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE)));
        }

        //Check if I'm holding the use button down
        if(!cancelEvent) {
            //System.out.println("Am I holding use down?: " + mc.options.keyUse.isDown());
            cancelEvent = !VanillaKeybindHandler.useHeld;
        }

        if (!cancelEvent){
            cancelEvent = myCap.getReviveStack() != null && !ItemStack.isSameItem(event.getEntity().getMainHandItem(), myCap.getReviveStack());
        }

        if (cancelEvent){
            ClientPacketDistributor.sendToServer(new RestartDeathTimerMsg());
        }
    }

    @SubscribeEvent
    public static void reviveItemUse(PlayerTickEvent.Pre event) {
        Minecraft mc = ClientUtil.getMinecraft();
        if (event.getEntity() != mc.player) return;

        if (!(mc.crosshairPickEntity instanceof Player)) return;

        FallenData cap = FallenData.get((Player) mc.crosshairPickEntity);
        if (!cap.isFallen()) return;
        if (cap.getOtherPlayer() == null) return;
        if (!mc.player.isUsingItem()) return;

        mc.gameMode.releaseUsingItem(event.getEntity());
    }
}