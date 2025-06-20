package invoker54.reviveme.client.event;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.payload.SelfReviveMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
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
    public static void forceDeath(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide) return;
        if (event.getEntity() != ClientUtil.getPlayer()) return;

        FallenData cap = FallenData.get(inst.player);

        if (!cap.isFallen()) return;
        if (!VanillaKeybindHandler.useHeld && !VanillaKeybindHandler.attackHeld) {
            timeHeld = 0;
            return;
        }

        //This will be chance
        if (VanillaKeybindHandler.attackHeld) {
            timeHeld++;
            if (!ClientUtil.getPlayer().swinging) ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            if (timeHeld == 40) PacketDistributor.sendToServer(new SelfReviveMsg(0));
        }
        //This will use items
        else if (VanillaKeybindHandler.useHeld && cap.canSelfRevive()) {
            timeHeld++;
            ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            if (timeHeld == 40) PacketDistributor.sendToServer(new SelfReviveMsg(1));
        }


    }

    @SubscribeEvent
    public static void modifyFOV(ComputeFovModifierEvent event){
        Player player = event.getPlayer();
        FallenData cap = FallenData.get(player);
        if (!cap.isFallen()) return;

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

    @SubscribeEvent
    public static void openInventory(ScreenEvent.Opening event){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenData.get(ClientUtil.getPlayer()).isFallen()) return;
        if (!(event.getScreen() instanceof InventoryScreen)) return;
        if (ReviveMeConfig.interactWithInventory != ReviveMeConfig.INTERACT_WITH_INVENTORY.NO) return;
        event.setCanceled(true);
    }
}
