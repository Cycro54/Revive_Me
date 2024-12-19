package invoker54.reviveme.client.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.InstaKillMsg;
import invoker54.reviveme.common.network.payload.ReviveChanceMsg;
import invoker54.reviveme.common.network.payload.SacrificeItemsMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class FallenPlayerActionsEvent {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Minecraft inst = Minecraft.getInstance();
    public static int timeHeld = 0;

    @SubscribeEvent
    public static void onAttack(InputEvent.InteractionKeyMappingTriggered event){
        if (FallenData.get(inst.player).isFallen()) {

            event.setCanceled(true);
            if (event.isAttack()){
                event.setSwingHand(false);
            }
        }

    }

    @SubscribeEvent
    public static void forceDeath(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide) return;
        if (event.getEntity() != ClientUtil.getPlayer()) return;

        FallenData cap = FallenData.get(inst.player);

        if (!cap.isFallen()) return;

        boolean flag = (ReviveMeConfig.selfReviveMultiplayer || (ClientUtil.getMinecraft().hasSingleplayerServer() &&
                ClientUtil.getMinecraft().getSingleplayerServer().getPlayerList().getPlayers().size() == 1));

        //This will be chance
        if (inst.options.keyAttack.isDown()) {
            timeHeld++;
            if (!ClientUtil.getPlayer().swinging) {
                ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            }

            if (timeHeld == 40) {
                if (flag) PacketDistributor.sendToServer(new ReviveChanceMsg());
                else if (ReviveMeConfig.canGiveUp) PacketDistributor.sendToServer(new InstaKillMsg());
            }
        }
        //This will use items
        else if (VanillaKeybindHandler.useKeyDown && flag && (!cap.usedChance() || !cap.getItemList().isEmpty())) {
            timeHeld++;

            if (timeHeld == 40) {
                PacketDistributor.sendToServer(new SacrificeItemsMsg());
            }
        } else if (timeHeld != 0) timeHeld = 0;
    }

    @SubscribeEvent
    public static void modifyFOV(ComputeFovModifierEvent event){
        Player player = event.getPlayer();
        FallenData cap = FallenData.get(player);
        if (!cap.isFallen()) return;
        boolean isSinglePlayer = (ClientUtil.getMinecraft().hasSingleplayerServer() &&
                ClientUtil.getMinecraft().getSingleplayerServer().getPlayerList().getPlayers().size() == 1);
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
