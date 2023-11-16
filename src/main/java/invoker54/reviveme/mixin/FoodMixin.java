package invoker54.reviveme.mixin;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface FoodMixin {
    @Accessor("saturationLevel")
    void setSaturationLevel(float newLevel);
}
