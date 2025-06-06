package invoker54.reviveme;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.network.NetworkHandler;
import invoker54.reviveme.init.CapabilityTypesInit;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

// The value here should match an entry in the META-INF/mods.toml file
//@Mod("examplemod")
@Mod(ReviveMe.MOD_ID)
public class ReviveMe
{
    public static final String MOD_ID = "reviveme";
    public static IEventBus bus;
    public static ModLogger LOGGER = ModLogger.getLogger(ReviveMe.class, ReviveMeConfig.debugMode);

    // Directly reference a log4j logger.
    public static final ResourceLocation FALLEN_LOC = new ResourceLocation(MOD_ID, "fallen");

    public ReviveMe() {
        bus = FMLJavaModLoadingContext.get().getModEventBus();
        // Register the setup method for modloading
        bus.addListener(this::commonSetup);
        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //This is for configs
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ReviveMeConfig.COMMON_SPEC, "reviveme-common.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        CapabilityTypesInit.registerCaps();
        NetworkHandler.init();
    }
}