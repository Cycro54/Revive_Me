package invoker54.reviveme.init;

import invoker54.reviveme.ReviveMe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class SoundInit {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final List<SoundEvent> sounds = new ArrayList<>();

    //FALLEN STATE SOUNDS
    public static SoundEvent FALLEN_STATE_HEART_BEAT = addSound("fallen_state_heart_beat");
    public static SoundEvent FALLEN_STATE_TICK_TOCK = addSound("fallen_state_tick_tock");

    //REVIVE SOUNDS
    public static SoundEvent REVIVE_BEAT = addSound("revive_main");
    public static SoundEvent REVIVE_BACKGROUND = addSound("revive_background");
    public static SoundEvent REVIVED = addSound("revived");

    //MISC SOUNDS
    public static SoundEvent CALL_FOR_HELP = addSound("call_for_help");

    public static SoundEvent addSound(String name){
        ResourceLocation soundSource = new ResourceLocation(ReviveMe.MOD_ID, name);
        SoundEvent event = new SoundEvent(soundSource);
        event.setRegistryName(soundSource);
        sounds.add(event);
        return event;
    }

    @SubscribeEvent
    public static void registerSounds(final RegistryEvent.Register<SoundEvent> soundEventRegister){
        IForgeRegistry<SoundEvent> registry = soundEventRegister.getRegistry();
        for (SoundEvent soundEvent : sounds){
            registry.register(soundEvent);
        }
//       SOUND_EVENTS.register(soundEventRegister.getRegistry());
    }

}