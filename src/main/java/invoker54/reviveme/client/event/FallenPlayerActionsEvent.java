package invoker54.reviveme.client.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.InstaKillMsg;
import invoker54.reviveme.common.network.message.ReviveChanceMsg;
import invoker54.reviveme.common.network.message.SacrificeItemsMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
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
    public static void forceDeath(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) return;
        if (event.type != TickEvent.Type.PLAYER) return;
        if (event.phase == TickEvent.Phase.END) return;
        if (event.player != ClientUtil.getPlayer()) return;

        FallenCapability cap = FallenCapability.GetFallCap(inst.player);

        if (!cap.isFallen()) {
            if (timeHeld != 0) timeHeld = 0;
            return;
        }

        boolean flag = (ReviveMeConfig.selfReviveMultiplayer || (ClientUtil.mC.hasSingleplayerServer() &&
                ClientUtil.mC.getSingleplayerServer().getPlayerList().getPlayers().size() == 1));

        //This will be chance
        if (VanillaKeybindHandler.attackHeld) {
            timeHeld++;
            if (!ClientUtil.getPlayer().swinging) {
                ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            }

            if (timeHeld == 40) {
                if (flag) NetworkHandler.INSTANCE.sendToServer(new ReviveChanceMsg());
                else if (ReviveMeConfig.canGiveUp) NetworkHandler.INSTANCE.sendToServer(new InstaKillMsg(ClientUtil.getPlayer().getUUID()));
            }
        }
        //This will use items
        else if (VanillaKeybindHandler.useHeld && flag && (!cap.usedChance() || !cap.getItemList().isEmpty())) {
            timeHeld++;
            ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);

            if (timeHeld == 40) {
                NetworkHandler.INSTANCE.sendToServer(new SacrificeItemsMsg());
            }
        } else if (timeHeld != 0) timeHeld = 0;
    }

    @SubscribeEvent
    public static void modifyFOV(ComputeFovModifierEvent event){
        Player player = event.getPlayer();
        FallenCapability cap = FallenCapability.GetFallCap(player);
        if (!cap.isFallen()) return;
        boolean isSinglePlayer = (ClientUtil.mC.hasSingleplayerServer() &&
                ClientUtil.mC.getSingleplayerServer().getPlayerList().getPlayers().size() == 1);
        if (!ReviveMeConfig.canGiveUp && !isSinglePlayer &&
                (!ReviveMeConfig.selfReviveMultiplayer || cap.usedSacrificedItems() && cap.usedChance())) return;

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

        event.setNewFovModifier(f);
    }
}
