package invoker54.reviveme.mixin;

import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

//    @Inject(
//            method = "getMainHandItem",
//            at = {
//                    @At(value = "HEAD")
//            }, cancellable = true)
//    private void getMainHandItem(CallbackInfoReturnable<ItemStack> cir){
//        if (((LivingEntity) (Object)this) != ClientUtil.getPlayer()) return;
//        FallenCapability cap = FallenCapability.get(((LivingEntity) (Object)this));
//        if (!cap.isFallen()) return;
//        if (!FallenItemScreenEvent.isItemScreenActive) return;
//        Pair<ItemStack, ReviveItemData> pair = FallenItemScreenEvent.getSelectedPair();
//        if (pair == null) return;
//
//        cir.setReturnValue(pair.getKey());
//    }
}
