package invoker54.reviveme.mixin;

import com.mojang.authlib.GameProfile;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.ProfilePublicKey;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player {

    @Shadow @Final public ServerPlayerGameMode gameMode;

    public ServerPlayerMixin(Level p_219727_, BlockPos p_219728_, float p_219729_, GameProfile p_219730_, @Nullable ProfilePublicKey p_219731_) {
        super(p_219727_, p_219728_, p_219729_, p_219730_, p_219731_);
    }


    @Override
    public boolean canEat(boolean p_71043_1_) {
        FallenCapability cap = FallenCapability.GetFallCap(this);
        if (!cap.isFallen()) return super.canEat(p_71043_1_);

        return false;
    }

    @Inject(

            method = "isCreative",
            at = {
                    @At(value = "HEAD")
            },
            cancellable = true)
    private void isCreative(CallbackInfoReturnable<Boolean> cir){
        if (this.gameMode.getGameModeForPlayer() == GameType.CREATIVE) return;
        FallenCapability cap = FallenCapability.GetFallCap(this);
        if (!cap.isFallen()) return;

        cir.setReturnValue(true);
    }
}
