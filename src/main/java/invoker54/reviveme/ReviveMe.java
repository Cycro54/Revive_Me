package invoker54.reviveme;

import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.AttachmentTypesInit;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ReviveMe.MOD_ID)
public class ReviveMe
{
    public static final String MOD_ID = "revive_me";
    public static IEventBus bus;

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final ResourceLocation FALLEN_LOC = makeResource("fallen_data");

    public ReviveMe(IEventBus modEventBus, ModContainer modContainer) {

        AttachmentTypesInit.registerAttachments(modEventBus);
        MobEffectInit.registerEffects(modEventBus);
//        NeoForge.EVENT_BUS.register(this);
        //This is for configs
        modContainer.registerConfig(ModConfig.Type.COMMON, ReviveMeConfig.COMMON_SPEC, "reviveme-common.toml");
        // MinecraftForge.EVENT_BUS.register(new DeathEventHandler());
    }

    public static ResourceLocation makeResource(String id){
        return ResourceLocation.fromNamespaceAndPath(ReviveMe.MOD_ID, id);
    }
//
//    private void setup(final FMLCommonSetupEvent event)
//    {
//        NetworkHandler.init();
//    }
}
