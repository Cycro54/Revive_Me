package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.world.GameType;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ForgeHooks.class)
public abstract class ForgeHooksMixin {

    @Inject(
            remap = false,
            method = "onInteractEntity(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResultType;",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onInteractEntity(PlayerEntity player, Entity target, Hand hand, CallbackInfoReturnable<ActionResultType> cir){
        if (target.level.isClientSide) return;

        FallenCapability myCap = FallenCapability.GetFallCap(player);

        //Make sure the player isn't fallen
        if (myCap.isFallen()) return;

        //Make sure they aren't crouching
        if (player.isDiscrete()) return;

        //Make sure target is a player
        if (!(target instanceof PlayerEntity)) return;
        PlayerEntity targPlayer = (PlayerEntity) target;

        //Grab that target entity (player)
        //Grab the targets cap too
        FallenCapability targCap = FallenCapability.GetFallCap(targPlayer);

        //Make sure the target is fallen and isn't being revived already
        if (!targCap.isFallen() || targCap.getOtherPlayer() != null) return;

        //Make sure the player reviving has enough of whatever is required
        if (!targCap.hasEnough(player)) return;

        //Now add the player to the targets fallencapability and vice versa.
        targCap.setProgress(player.level.getGameTime(), ReviveMeConfig.reviveTime);
        targCap.setOtherPlayer(player.getUUID());
        myCap.setProgress(player.level.getGameTime(), ReviveMeConfig.reviveTime);
        myCap.setOtherPlayer(targPlayer.getUUID());

        //Make sure the fallen client has this data too
        CompoundNBT nbt = new CompoundNBT();

        nbt.put(player.getStringUUID(), myCap.writeNBT());
        nbt.put(targPlayer.getStringUUID(), targCap.writeNBT());

        NetworkHandler.sendToPlayer(targPlayer, new SyncClientCapMsg(nbt));
        NetworkHandler.sendToPlayer(player, new SyncClientCapMsg(nbt));

        cir.setReturnValue(ActionResultType.FAIL);
    }

    @Inject(
            remap = false,
            method = "onLivingDeath",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onLivingDeath(LivingEntity entity, DamageSource src, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof ServerPlayerEntity)) return;
        if ((((ServerPlayerEntity) entity).gameMode.getGameModeForPlayer() == GameType.CREATIVE)) return;
        boolean cancelled;

        if (ReviveMeConfig.runDeathEventFirst){
            cancelled = MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
            if (!cancelled) cancelled = FallEvent.cancelEvent((PlayerEntity) entity, src);
        }
        else {
            cancelled = FallEvent.cancelEvent((PlayerEntity) entity, src);
            if (!cancelled) cancelled = MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
        }

        cir.setReturnValue(cancelled);
        cir.cancel();
    }
}
