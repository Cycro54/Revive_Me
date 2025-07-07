package invoker54.reviveme.client.event;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoSound;
import invoker54.invocore.common.MathUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.SoundInit;
import net.minecraft.client.audio.ISound;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class SoundEvents{
    private static final ModLogger LOGGER = ModLogger.getLogger(SoundEvents.class, ReviveMeConfig.debugMode);

    private static final InvoSound fallen_state_random_sound = new InvoSound(SoundInit.FALLEN_STATE_HEART_BEAT, SoundCategory.AMBIENT).setVolume((float) (1F * ReviveMeConfig.soundLevel)).
            setPitch(1.0F).setRepeatDelay(200, true).setGlobal(true).setPreModifySound(
                    (invoSound -> {
                        invoSound.setPitch(MathUtil.randomFloat(0.8F, 1.2F));
                        invoSound.setVolume((float) (MathUtil.randomFloat(1.3F,1.1F) * ReviveMeConfig.soundLevel));
                        int delay = MathUtil.randomInt(10, 16);
                        invoSound.setRepeatDelay(delay*20, true);
                    })
            );
    private static final InvoSound fallen_state_ticking_sound = new InvoSound(SoundInit.FALLEN_STATE_TICK_TOCK, SoundCategory.AMBIENT)
            .setGlobal(true).setPreModifySound((invoSound -> {
                PlayerEntity player = ClientUtil.getPlayer();
                if (player == null) return;
                FallenCapability cap = FallenCapability.GetFallCap(player);
                float percentage = cap.GetTimeLeft(true);
                if (ReviveMeConfig.timeLeft == 0){
                    invoSound.setPitch(MathUtil.randomFloat(0.5F, 0.75F));
                    invoSound.setVolume((float) (MathUtil.randomFloat(0.1F, 0.3F) * ReviveMeConfig.soundLevel));
                    invoSound.setRepeatDelay(5*20,false);
                }
                else {
                    invoSound.setPitch(MathUtil.lerp(percentage, 0.7F, 0.4F));
                    invoSound.setVolume((float) (MathUtil.lerp(((1 - Math.pow(2, -10 * percentage))), 0.3F, 0.01F) * ReviveMeConfig.soundLevel));
                    invoSound.setRepeatDelay(0,false);
                }
            }));

    private static final InvoSound revive_sound = new InvoSound(SoundInit.REVIVE_BEAT, SoundCategory.PLAYERS).setVolume(0.9F).setPitch(1.0F)
            .setGlobal(true).setPreModifySound((invoSound -> {
                PlayerEntity player = ClientUtil.getPlayer();
                if (player == null) return;
                FallenCapability cap = FallenCapability.GetFallCap(player);
                float pitch = MathUtil.lerp(cap.getProgress(true), 0.8F, 1.2F);
                invoSound.setVolume((float) (1.0f * ReviveMeConfig.soundLevel));
                invoSound.setPitch(pitch);
            }));
    private static final InvoSound revive_background_sound = new InvoSound(SoundInit.REVIVE_BACKGROUND, SoundCategory.PLAYERS)
            .setVolume((float) (0.4F * ReviveMeConfig.soundLevel)).setPitch(1.0F).setGlobal(true).setPreModifySound(is -> {
                is.setVolume((float) (0.3F * ReviveMeConfig.soundLevel));
                is.setPitch(MathUtil.randomFloat(0.7F, 1.1F));
            });

    @SubscribeEvent
    public static void fallenNoiseEvent(TickEvent.ClientTickEvent event){
        if (event.phase == TickEvent.Phase.END) return;
        if (ClientUtil.getPlayer() == null) return;

        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
        if (!cap.isFallen() || cap.getOtherPlayer() != null) return;

        if (cap.GetTimeLeft(false) > 5) fallen_state_random_sound.playWhenStopped();
        if (cap.GetTimeLeft(false) % 1 == 0 && cap.GetTimeLeft(false) > 0  || ReviveMeConfig.timeLeft == 0) fallen_state_ticking_sound.play();
    }

    @SubscribeEvent
    public static void reviveNoiseEvent(TickEvent.ClientTickEvent event){
        if (event.phase == TickEvent.Phase.END) return;
        if (ClientUtil.getPlayer() == null) return;

        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
        if (cap.getOtherPlayer() == null){
            if (!revive_background_sound.isDonePlaying()) revive_background_sound.stopIt();
//            if (!revive_sound.isDonePlaying()) revive_sound.stopIt();
            return;
        }
        if ((cap.getProgress(false)/20f) % 0.5F == 0 && cap.GetTimeLeft(true) != 1) revive_sound.play();
        revive_background_sound.playWhenStopped();
    }

    @SubscribeEvent
    public static void onReviveEvent(PlaySoundEvent event) {
        if (event.getSound() instanceof InvoSound) return;
        if (!SoundInit.REVIVED.getLocation().getPath().equals(event.getName())) return;
        ISound prevSound = event.getResultSound();
        event.setResultSound(new InvoSound(SoundInit.REVIVED, SoundCategory.PLAYERS)
                .setVolume((float) (MathUtil.randomFloat(0.7F, 0.9F) * ReviveMeConfig.soundLevel)).setPitch(MathUtil.randomFloat(0.8F, 1.0F))
                .setPos(new Vector3d(prevSound.getX(), prevSound.getY(), prevSound.getZ())));
    }
}
