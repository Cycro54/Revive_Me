package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import invoker54.reviveme.common.network.payload.SyncClientCapMsg;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CommonHooks.class)
public class CommonHooksMixin {

    @Inject(
            remap = false,
            method = "onInteractEntity(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onInteractEntity(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        if (target.level().isClientSide) return;

        FallenData reviverCap = FallenData.get(player);

        //Make sure the player isn't fallen
        if (reviverCap.isFallen()) return;

        //Make sure they aren't crouching
        if (player.isDiscrete()) return;

        //Make sure target is a player
        if (!(target instanceof Player targPlayer)) return;

        //Grab that target entity (player)
        //Grab the targets cap too
        FallenData targCap = FallenData.get(targPlayer);

        //Make sure the target is fallen and isn't being revived already
        if (!targCap.isFallen() || targCap.getOtherPlayer() != null) return;

        //Make sure the player reviving has enough of whatever is required
        if (!targCap.hasEnough(player)) return;

        //Now add the player to the targets FallenData and vice versa.
        targCap.setProgress(player.level().getGameTime(), ReviveMeConfig.reviveTime);
        targCap.setOtherPlayer(player.getUUID());
        reviverCap.setProgress(player.level().getGameTime(), ReviveMeConfig.reviveTime);
        reviverCap.setOtherPlayer(targPlayer.getUUID());

        //Make sure the fallen client has this data too
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(player, new SyncClientCapMsg(player.getUUID(), reviverCap.writeNBT()));
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(targPlayer, new SyncClientCapMsg(targPlayer.getUUID(), targCap.writeNBT()));

        cir.setReturnValue(InteractionResult.FAIL);
    }

    @Inject(
            remap = false,
            method = "onLivingDeath",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onLivingDeath(LivingEntity entity, DamageSource src, CallbackInfoReturnable<Boolean> cir) {
        if (!(entity instanceof ServerPlayer)) return;
        if ((((ServerPlayer) entity).gameMode.getGameModeForPlayer() == GameType.CREATIVE)) return;
        boolean cancelled;

        if (ReviveMeConfig.runDeathEventFirst) {
            cancelled = NeoForge.EVENT_BUS.post(new LivingDeathEvent(entity, src)).isCanceled();
            if (!cancelled) cancelled = FallEvent.cancelEvent((Player) entity, src);
        } else {
            cancelled = FallEvent.cancelEvent((Player) entity, src);
            if (!cancelled) cancelled = NeoForge.EVENT_BUS.post(new LivingDeathEvent(entity, src)).isCanceled();
        }

        cir.setReturnValue(cancelled);
        cir.cancel();
    }
}
