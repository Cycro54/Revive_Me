package invoker54.reviveme.common.api;

import net.minecraft.entity.player.ServerPlayerEntity;

public interface IFallen {
    float progress = 0;
    float timeLeft = 0;
    boolean isFallen = false;
    ServerPlayerEntity revivingPlayer = null;

    float GetProgress();

    void SetProgress(float a);

    float GetTimeLeft();

    void SetTimeLeft(float a);

    void ForceDeath();

    boolean isFallen();

    void setFallen(boolean fallen);

    ServerPlayerEntity revivingPlayer();
}
