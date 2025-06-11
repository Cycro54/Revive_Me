package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.event.FallEvent;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.common.network.message.SyncClientCapMsg;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
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
            method = "onInteractEntity(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onInteractEntity(Player player, Entity target, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir){
        if (target.level.isClientSide) return;

        FallenCapability myCap = FallenCapability.GetFallCap(player);

        //Make sure the player isn't fallen
        if (myCap.isFallen()) return;

        //Make sure they aren't crouching
        if (player.isDiscrete()) return;

        //Make sure target is a player
        if (!(target instanceof Player targPlayer)) return;

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
        CompoundTag nbt = new CompoundTag();

        nbt.put(player.getStringUUID(), myCap.writeNBT());
        nbt.put(targPlayer.getStringUUID(), targCap.writeNBT());

        NetworkHandler.sendToPlayer(targPlayer, new SyncClientCapMsg(nbt));
        NetworkHandler.sendToPlayer(player, new SyncClientCapMsg(nbt));

        cir.setReturnValue(InteractionResult.FAIL);
    }


    @Inject(
            remap = false,
            method = "onLivingDeath",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private static void onLivingDeath(LivingEntity entity, DamageSource src, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof ServerPlayer)) return;
        if ((((ServerPlayer) entity).gameMode.getGameModeForPlayer() == GameType.CREATIVE)) return;
        boolean cancelled;

        if (ReviveMeConfig.runDeathEventFirst){
            cancelled = MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
            if (!cancelled) cancelled = FallEvent.cancelEvent((Player) entity, src);
        }
        else {
            cancelled = FallEvent.cancelEvent((Player) entity, src);
            if (!cancelled) cancelled = MinecraftForge.EVENT_BUS.post(new LivingDeathEvent(entity, src));
        }

        cir.setReturnValue(cancelled);
        cir.cancel();
    }
}
