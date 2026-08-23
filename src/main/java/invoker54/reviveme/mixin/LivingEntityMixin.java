package invoker54.reviveme.mixin;

import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {



    @ModifyVariable(
            method = "hurt",
            at = @At(value = "HEAD"),
            argsOnly = true,
            index = 2)
    private float amount(float amount, DamageSource source) {
        if (source.getEntity() instanceof Player){
            FallenCapability cap = FallenCapability.get((LivingEntity) source.getEntity());
            if (cap.isFallen()) amount *= ReviveMeConfig.fallenDamageScaleOut;
        }
        else if (((LivingEntity)(Object)this) instanceof Player){
            FallenCapability cap = FallenCapability.get(((LivingEntity)(Object)this));
            if (cap.isFallen()) amount *= ReviveMeConfig.fallenDamageScaleIn;
        }

        return amount;
    }

}