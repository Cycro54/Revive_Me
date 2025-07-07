package invoker54.reviveme.mixin;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static invoker54.invocore.client.util.ClientUtil.mC;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Inject(
            method = "shouldEntityAppearGlowing",
            at = {
                    @At(value = "HEAD")
            }, cancellable = true
    )
    private void shouldBeGlowing(Entity entity, CallbackInfoReturnable<Boolean> cir){
        if (!(entity instanceof PlayerEntity)) return;
        float distance = entity.distanceTo(ClientUtil.getPlayer());
        if (distance < 10 || distance > ReviveMeConfig.reviveGlowMaxDistance) return;
        FallenCapability cap = FallenCapability.GetFallCap((LivingEntity) entity);
        if (!cap.isFallen()) return;

        BlockRayTraceResult rayResult = mC.player.level.clip(
                new RayTraceContext(mC.player.getEyePosition(1.0F), entity.getEyePosition(1.0F)
                        , RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
        boolean targetSeen = rayResult.getType() == RayTraceResult.Type.MISS;
        if (!targetSeen && !cap.isCallingForHelp()) return;


        cir.setReturnValue(true);
    }
}
