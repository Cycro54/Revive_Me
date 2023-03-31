package invoker54.reviveme.client.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.InstaKillMsg;
import invoker54.reviveme.common.network.message.ReviveChanceMsg;
import invoker54.reviveme.common.network.message.SacrificeItemsMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class FallenPlayerActionsEvent {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Minecraft inst = Minecraft.getInstance();
    public static int timeHeld = 0;

    @SubscribeEvent
    public static void onAttack(InputEvent.ClickInputEvent event){
        if (FallenCapability.GetFallCap(inst.player).isFallen()) {

            event.setCanceled(true);
            if (event.isAttack()){
                event.setSwingHand(false);
            }
        }
    }

    @SubscribeEvent
    public static void forceDeath(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) return;
        FallenCapability cap = FallenCapability.GetFallCap(inst.player);

        if (!cap.isFallen()) return;

        if (event.phase == TickEvent.Phase.END) return;

        boolean flag = (ClientUtil.mC.hasSingleplayerServer() &&
                ClientUtil.mC.getSingleplayerServer().getPlayerList().getPlayers().size() == 1);

        //This will be chance
        if (inst.options.keyAttack.isDown()) {
            timeHeld++;
            if (!ClientUtil.getPlayer().swinging) {
                ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            }

            if (timeHeld == 40) {
                if (flag) NetworkHandler.INSTANCE.sendToServer(new ReviveChanceMsg());
                else NetworkHandler.INSTANCE.sendToServer(new InstaKillMsg(ClientUtil.getPlayer().getUUID()));
            }
        }
        //This will use items
        else if (inst.options.keyUse.isDown() && flag) {
            timeHeld++;

            if (timeHeld == 40) {
                NetworkHandler.INSTANCE.sendToServer(new SacrificeItemsMsg());
            }
        } else if (timeHeld != 0) timeHeld = 0;
    }

    @SubscribeEvent
    public static void modifyFOV(FOVModifierEvent event){
        Player player = event.getEntity();
        if (!FallenCapability.GetFallCap(player).isFallen()) return;

        float f = 1.0F;
        if (player.getAbilities().flying) {
            f *= 1.1F;
        }

        f = (float) ((double) f * ((player.getAttributeValue(Attributes.MOVEMENT_SPEED) / (double) player.getAbilities().getWalkingSpeed() + 1.0D) / 2.0D));
        if (player.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
            f = 1.0F;
        }

        int i = timeHeld;
        float f1 = Math.min ((float) i / 40, 1.0F);
        f1 = f1 * f1;

        f *= 1.0F - f1 * 0.15F;

        event.setNewfov(f);
    }
}
