package invoker54.reviveme;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.compatibility.controllable.client.events.ControllableModEvents;
import invoker54.reviveme.init.DamageTypeInit;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ReviveMe.MOD_ID)
public class ReviveMe
{
    public static final String MOD_ID = "revive_me";
    public static IEventBus bus;

    // Directly reference a log4j logger.
    private static final ModLogger LOGGER = ModLogger.getLogger(ReviveMe.class, ReviveMeConfig.debugMode);
    public static final ResourceLocation FALLEN_LOC = new ResourceLocation(MOD_ID, "fallen");

    public ReviveMe() {
        bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::setup);
        // Register clientSetup
        bus.addListener(this::clientSetup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        DamageTypeInit.init();

        //This is for configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ReviveMeConfig.COMMON_SPEC, "reviveme/reviveme-common.toml");
        // MinecraftForge.EVENT_BUS.register(new DeathEventHandler());
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        NetworkHandler.init();
    }
    private void clientSetup(final FMLClientSetupEvent event){
        if (ModList.get().isLoaded("controllable")){
            LOGGER.error("CONTROLLABLE WAS FOUND");
            ControllableModEvents.init();
//            MinecraftForge.EVENT_BUS.register(new ControllableModEvents());
        }
        else {
            LOGGER.error("CONTROLLABLE WAS NOT FOUND");
        }
    }
}