package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.client.event.FallenItemScreenEvent;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(
            method = "getMainHandItem",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void getMainHandItem(CallbackInfoReturnable<ItemStack> cir){
        if (((LivingEntity) (Object)this) != ClientUtil.getPlayer()) return;
        FallenCapability cap = FallenCapability.get(((LivingEntity) (Object)this));
        if (!cap.isFallen()) return;
        if (!FallenItemScreenEvent.isItemScreenActive) return;
        Pair<ItemStack, ReviveItemData> pair = FallenItemScreenEvent.getSelectedPair();
        if (pair == null) return;
        cir.setReturnValue(pair.getKey());
    }
}
