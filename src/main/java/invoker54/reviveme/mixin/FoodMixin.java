package invoker54.reviveme.mixin;

import net.minecraft.util.FoodStats;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodStats.class)
public interface FoodMixin {
    @Accessor("saturationLevel")
    void setSaturationLevel(float newLevel);
}
