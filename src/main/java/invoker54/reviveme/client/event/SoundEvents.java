package invoker54.reviveme.client.event;

import invoker54.invocore.MathUtil;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.InvoSound;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class SoundEvents{
    private static final Logger LOGGER = LogManager.getLogger();

    private static final InvoSound fallen_state_random_sound = new InvoSound(SoundInit.FALLEN_STATE_HEART_BEAT, SoundCategory.AMBIENT).setVolume(1F).
            setPitch(1.0F).setRepeatDelay(200, true).setGlobal(true).setPreModifySound(
                    (invoSound -> {
                        invoSound.setPitch(MathUtil.randomFloat(0.8F, 1.2F));
                        invoSound.setVolume(MathUtil.randomFloat(1.0F,0.9F));
                        int delay = MathUtil.randomInt(10, 16);
                        invoSound.setRepeatDelay(delay*20, true);
//                        LOGGER.error("Whats the delay: " + (delay));
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
                    invoSound.setVolume(MathUtil.randomFloat(0.7F, 0.9F));
                    invoSound.setRepeatDelay(5*20,false);
                }
                else {
                    invoSound.setPitch(MathUtil.lerp(percentage, 0.7F, 0.4F));
                    invoSound.setVolume(MathUtil.lerp(((1 - Math.pow(2, -10 * percentage))), 0.9F, 0.05F));
                    invoSound.setRepeatDelay(0,false);
                }
            }));

//    private static final InvoSound revive_sound = new InvoSound(SoundInit.REVIVE_BEAT, SoundCategory.PLAYERS).setVolume(0.9F).setPitch(1.0F)
//            .setGlobal(true).setPreModifySound((invoSound -> {
//                PlayerEntity player = ClientUtil.getPlayer();
//                if (player == null) return;
//                FallenCapability cap = FallenCapability.GetFallCap(player);
//                float pitch = MathUtil.lerp(cap.getProgress(), 1.0F, 1.0F);
//                invoSound.setPitch(pitch);
//            }));
    private static final InvoSound revive_background_sound = new InvoSound(SoundInit.REVIVE_BACKGROUND, SoundCategory.PLAYERS)
            .setVolume(1.0F).setPitch(1.0F).setGlobal(true).setPreModifySound(is ->
                    is.setPitch(MathUtil.randomFloat(0.7F, 1.1F)));

    @SubscribeEvent
    public static void fallenNoiseEvent(TickEvent.ClientTickEvent event){
        if (event.phase == TickEvent.Phase.END) return;
        if (ClientUtil.getPlayer() == null) return;

        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
        if (!cap.isFallen() || cap.getOtherPlayer() != null) return;

        fallen_state_random_sound.playWhenStopped();
        if (cap.GetTimeLeft(false) % 1 == 0) fallen_state_ticking_sound.play();
    }

    @SubscribeEvent
    public static void reviveNoiseEvent(TickEvent.ClientTickEvent event){
        if (event.phase == TickEvent.Phase.END) return;
        if (ClientUtil.getPlayer() == null) return;

        FallenCapability cap = FallenCapability.GetFallCap(ClientUtil.getPlayer());
        if (cap.getOtherPlayer() == null){
            if (!revive_background_sound.isDonePlaying()) revive_background_sound.stopIt();
            return;
        }

//        revive_sound.playWhenStopped();
        revive_background_sound.playWhenStopped();
    }

    @SubscribeEvent
    public static void onReviveEvent(PlaySoundEvent event) {
        if (event.getSound() instanceof InvoSound) return;
        if (!SoundInit.REVIVED.getLocation().getPath().equals(event.getName())) return;
        ISound prevSound = event.getResultSound();
        event.setResultSound(new InvoSound(SoundInit.REVIVED, SoundCategory.PLAYERS)
                .setVolume(MathUtil.randomFloat(0.7F, 0.9F)).setPitch(MathUtil.randomFloat(0.8F, 1.0F))
                .setPos(new Vector3d(prevSound.getX(), prevSound.getY(), prevSound.getZ())));
    }
}
