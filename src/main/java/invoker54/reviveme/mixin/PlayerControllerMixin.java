package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.client.multiplayer.PlayerController;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerController.class)
public class PlayerControllerMixin {

    @Inject(
            method = "useItem",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void useItem(PlayerEntity player, World level, Hand hand, CallbackInfoReturnable<ActionResultType> cir){
        if (player == null) return;
        FallenCapability myCap = FallenCapability.get(player);

        boolean lookingAtFallen = false;
        boolean canRevive = false;
        if (ClientUtil.mC.crosshairPickEntity instanceof PlayerEntity){
            FallenCapability targetCap = FallenCapability.get((LivingEntity) ClientUtil.mC.crosshairPickEntity);
            lookingAtFallen = targetCap.isFallen();

            canRevive = targetCap.hasEnough(player);

//            if (lookingAtFallen && player.getMainHandItem() != ItemStack.EMPTY){
//                player.getCooldowns().addCooldown(player.getMainHandItem().getItem(), 30);
//            }
        }

        if (!canRevive) canRevive = ReviveItemData.getData(player.getMainHandItem(), ReviveItemData.USER.REVIVER) != null;

        if (myCap.isFallen() || (lookingAtFallen && (canRevive && !player.isCrouching()))) cir.setReturnValue(ActionResultType.FAIL);
        if (myCap.getOtherPlayer() != null){
            player.stopUsingItem();
            cir.setReturnValue(ActionResultType.FAIL);
        }
    }

    @Inject(
            method = "destroyBlock",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void destroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir){
        if (ClientUtil.getPlayer() == null) return;
        FallenCapability myCap = FallenCapability.get(ClientUtil.getPlayer());

        if (!myCap.isFallen()) return;
        cir.setReturnValue(false);
    }
}
