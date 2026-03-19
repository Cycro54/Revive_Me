package invoker54.reviveme.mixin;

import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LocalPlayer.class)
public interface LocalPlayerMixin {
    @Accessor("crouching")
    void setCrouching(boolean crouching);
}
