package invoker54.reviveme.client.event;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SelfReviveMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class FallenPlayerActionsEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallenPlayerActionsEvent.class, ReviveMeConfig.debugMode);
    private static final Minecraft inst = Minecraft.getInstance();
    public static int timeHeld = 0;

    @SubscribeEvent
    public static void forceDeath(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) return;
        if (event.type != TickEvent.Type.PLAYER) return;
        if (event.phase == TickEvent.Phase.END) return;
        if (event.player != ClientUtil.getPlayer()) return;

        FallenCapability cap = FallenCapability.GetFallCap(inst.player);

        if (!cap.isFallen()) return;
        if (!VanillaKeybindHandler.useHeld && !VanillaKeybindHandler.attackHeld){
            timeHeld = 0;
            return;
        }

        //This will be chance
        if (VanillaKeybindHandler.attackHeld) {
            timeHeld++;
            if (!ClientUtil.getPlayer().swinging) ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            if (timeHeld == 40) NetworkHandler.INSTANCE.sendToServer(new SelfReviveMsg(0));
        }
        //This will use items
        else if (VanillaKeybindHandler.useHeld && cap.canSelfRevive()) {
            timeHeld++;
            ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            if (timeHeld == 40) NetworkHandler.INSTANCE.sendToServer(new SelfReviveMsg(1));
        }


    }

    @SubscribeEvent
    public static void modifyFOV(FOVModifierEvent event){
        Player player = event.getEntity();
        FallenCapability cap = FallenCapability.GetFallCap(player);
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

        event.setNewfov(f);
    }

    @SubscribeEvent
    public static void openInventory(ScreenOpenEvent event){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.GetFallCap(ClientUtil.getPlayer()).isFallen()) return;
        if (!(event.getScreen() instanceof InventoryScreen)) return;
        if (ReviveMeConfig.interactWithInventory != ReviveMeConfig.INTERACT_WITH_INVENTORY.NO) return;
        event.setScreen(null);
    }
}
