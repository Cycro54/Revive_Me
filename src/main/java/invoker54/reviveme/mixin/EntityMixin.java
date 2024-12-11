package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public Level level;

    @Shadow public abstract int getId();

    @Inject(
            method = "hasPose",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true)
    private void hasPose(Pose pose, CallbackInfoReturnable<Boolean> cir){
        if (!this.level.isClientSide) return;
        Entity entity = this.level.getEntity(this.getId());
        if (!(entity instanceof Player)) return;

        Player player = (Player)entity;
        if (!FallenCapability.GetFallCap(player).isFallen()) return;

        switch (ReviveMeConfig.fallenPose){
            case CROUCH -> cir.setReturnValue(pose == Pose.CROUCHING);
            case PRONE -> cir.setReturnValue(pose == Pose.SWIMMING);
            case SLEEP -> cir.setReturnValue(pose == Pose.SLEEPING);
        }
    }
}
