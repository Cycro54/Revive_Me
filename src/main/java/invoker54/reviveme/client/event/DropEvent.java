package invoker54.reviveme.client.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class DropEvent {

    @SubscribeEvent
    public static void onDrop(ItemTossEvent event){
        FallenCapability cap = FallenCapability.GetFallCap(event.getPlayer());
        if (event.isCancelable() && cap.isFallen()){
            ItemStack itemStack = event.getEntityItem().getItem();
            event.setCanceled(true);
            event.getPlayer().getInventory().placeItemBackInInventory(itemStack);
        }
    }
}
