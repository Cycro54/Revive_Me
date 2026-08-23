package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MultiPlayerGameMode.class)
public class PlayerControllerMixin {

    @Inject(
            method = "useItem",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void useItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        if (player == null) return;
        FallenCapability myCap = FallenCapability.get(player);

        boolean lookingAtFallen = false;
        boolean canRevive = false;
        if (ClientUtil.getMinecraft().crosshairPickEntity instanceof Player){
            FallenCapability targetCap = FallenCapability.get((LivingEntity) ClientUtil.getMinecraft().crosshairPickEntity);
            lookingAtFallen = targetCap.isFallen();

            canRevive = targetCap.hasEnough(player);

        }

        if (!canRevive) canRevive = ReviveItemData.getData(player.getMainHandItem(), ReviveItemData.USER.REVIVER) != null;

        if ((myCap.isFallen() && !reviveMe_canUseItems()) || (lookingAtFallen && (canRevive && !player.isShiftKeyDown()))) cir.setReturnValue(InteractionResult.FAIL);
        if (myCap.getOtherPlayer() != null){
            player.stopUsingItem();
            cir.setReturnValue(InteractionResult.FAIL);
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

        if (!myCap.isFallen() || reviveMe_canUseItems()) return;
        cir.setReturnValue(false);
    }

    @Unique
    private static boolean reviveMe_canUseItems(){
        return VanillaKeybindHandler.isAllowedKeybind(ClientUtil.getMinecraft().options.keyUse);
    }
}
