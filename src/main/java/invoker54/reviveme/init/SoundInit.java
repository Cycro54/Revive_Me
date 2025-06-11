package invoker54.reviveme.init;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundInit {
    private static final ModLogger LOGGER = ModLogger.getLogger(SoundInit.class, ReviveMeConfig.debugMode);

    private static final List<Pair<ResourceLocation, SoundEvent>> sounds = new ArrayList<>();

    //FALLEN STATE SOUNDS
    public static SoundEvent FALLEN_STATE_HEART_BEAT = addSound("fallen_state_heart_beat");
    public static SoundEvent FALLEN_STATE_TICK_TOCK = addSound("fallen_state_tick_tock");

    //REVIVE SOUNDS
    public static SoundEvent REVIVE_BEAT = addSound("revive_main");
    public static SoundEvent REVIVE_BACKGROUND = addSound("revive_background");
    public static SoundEvent REVIVED = addSound("revived");

    //MISC SOUNDS
    public static SoundEvent CALL_FOR_HELP = addSound("call_for_help");

    public static SoundEvent addSound(String name) {
        ResourceLocation soundSource = new ResourceLocation(ReviveMe.MOD_ID, name);
        SoundEvent event = new SoundEvent(soundSource);
        sounds.add(Pair.of(soundSource, event));
        return event;
    }

    @SubscribeEvent
    public static void registerSounds(final RegisterEvent event) {

        event.register(ForgeRegistries.Keys.SOUND_EVENTS,
                helper -> {
                    for (var pair : sounds) {
                        helper.register(pair.getKey(), pair.getValue());
                    }
                });
//       SOUND_EVENTS.register(soundEventRegister.getRegistry());
    }

}