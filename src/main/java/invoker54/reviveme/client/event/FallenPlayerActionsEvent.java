package invoker54.reviveme.client.event;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.ReviveItemMsg;
import invoker54.reviveme.common.network.message.SelfReviveMsg;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.FOVModifierEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class FallenPlayerActionsEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallenPlayerActionsEvent.class, ReviveMeConfig.debugMode);
    private static final Minecraft inst = Minecraft.getInstance();
    public static int timeHeld = 0;

    @SubscribeEvent
    public static void doReviveAction(TickEvent.PlayerTickEvent event) {
        if (event.side == LogicalSide.SERVER) return;
        if (event.type != TickEvent.Type.PLAYER) return;
        if (event.phase == TickEvent.Phase.END) return;
        if (event.player != ClientUtil.getPlayer()) return;

        FallenCapability cap = FallenCapability.get(inst.player);
        boolean canSelfRevive = cap.canSelfRevive();

        if (!cap.isFallen()) return;
        if ((!VanillaKeybindHandler.useHeld && !VanillaKeybindHandler.attackHeld) || cap.getOtherPlayer() != null){
            timeHeld = 0;
            return;
        }

        if (canSelfRevive && FallenItemScreenEvent.isItemScreenActive) useReviveItem();
        else doReviveAction(canSelfRevive);
    }

    public static void doReviveAction(boolean canSelfRevive){
        //This will be chance
        if (VanillaKeybindHandler.attackHeld) {
            timeHeld++;
            if (!ClientUtil.getPlayer().swinging) ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            if (timeHeld == 40) NetworkHandler.INSTANCE.sendToServer(new SelfReviveMsg(0));
        }
        //This will use items
        else if (VanillaKeybindHandler.useHeld && canSelfRevive) {
            timeHeld++;
            ClientUtil.getPlayer().swing(InteractionHand.MAIN_HAND);
            if (timeHeld == 40) NetworkHandler.INSTANCE.sendToServer(new SelfReviveMsg(1));
        }

        timeHeld = Math.min(timeHeld, 41);
    }

    public static void useReviveItem(){
        if (VanillaKeybindHandler.useHeld){
            timeHeld++;
            List<Pair<ItemStack, ReviveItemData>> dataStackList = FallenItemScreenEvent.dataStackList;
            ReviveItemData data = dataStackList.get(FallenItemScreenEvent.selectedItem).getRight();

            int maxTicks = data == null ? 40 : (int) (data.getReviveSeconds() * 20);
            if (maxTicks == 0) maxTicks = 1;

            if (timeHeld == maxTicks){
                NetworkHandler.INSTANCE.sendToServer(new ReviveItemMsg(dataStackList.get(FallenItemScreenEvent.selectedItem).getKey().serializeNBT()));

            }
            timeHeld = Math.min(timeHeld, maxTicks + 1);
        }
        else {
            timeHeld = 0;
        }
    }

    @SubscribeEvent
    public static void modifyFOV(FOVModifierEvent event){
        Player player = event.getEntity();
        FallenCapability cap = FallenCapability.get(player);
        if (!cap.isFallen()) return;

        List<Pair<ItemStack, ReviveItemData>> dataStackList = FallenItemScreenEvent.dataStackList;
        ReviveItemData data = dataStackList.isEmpty() ? null : dataStackList.get(FallenItemScreenEvent.selectedItem).getRight();
        int maxTicks = data == null ? 40 : (int) (data.getReviveSeconds() * 20);
        if (maxTicks == 0) maxTicks = 1;

        float f = 1.0F;
        if (player.getAbilities().flying) {
            f *= 1.1F;
        }

        f = (float) ((double) f * ((player.getAttributeValue(Attributes.MOVEMENT_SPEED) / (double) player.getAbilities().getWalkingSpeed() + 1.0D) / 2.0D));
        if (player.getAbilities().getWalkingSpeed() == 0.0F || Float.isNaN(f) || Float.isInfinite(f)) {
            f = 1.0F;
        }

        int i = timeHeld;
        float f1 = Math.min ((float) i / maxTicks, 1.0F);
        f1 = f1 * f1;

        f *= 1.0F - f1 * 0.15F;

        event.setNewfov(f);
    }

    @SubscribeEvent
    public static void openInventory(ScreenOpenEvent event){
        if (ClientUtil.getWorld() == null) return;
        if (ClientUtil.getPlayer() == null) return;
        if (!FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;
        if (!(event.getScreen() instanceof InventoryScreen)) return;
        if (ReviveMeConfig.interactWithInventory != ReviveMeConfig.INTERACT_WITH_INVENTORY.NO) return;
        event.setScreen(null);
    }
}
