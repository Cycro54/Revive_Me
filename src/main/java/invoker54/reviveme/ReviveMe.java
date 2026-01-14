package invoker54.reviveme;

import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.AttachmentTypesInit;
import invoker54.reviveme.init.CompatibilityInit;
import invoker54.reviveme.init.DamageTypeInit;
import invoker54.reviveme.init.MobEffectInit;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@Mod(ReviveMe.MOD_ID)
public class ReviveMe
{
    public static final String MOD_ID = "revive_me";
    public static IEventBus bus;

    // Directly reference a log4j logger.
    private static final ModLogger LOGGER = ModLogger.getLogger(ReviveMe.class, ReviveMeConfig.debugMode);

    public ReviveMe(IEventBus modEventBus, ModContainer modContainer) {
        DamageTypeInit.init();

        AttachmentTypesInit.registerAttachments(modEventBus);
        MobEffectInit.registerEffects(modEventBus);
        modEventBus.addListener(this::clientSetup);
        //This is for configs
        modContainer.registerConfig(ModConfig.Type.COMMON, ReviveMeConfig.COMMON_SPEC, "reviveme/reviveme-common.toml");
    }

    private void clientSetup(FMLClientSetupEvent event){
        CompatibilityInit.ControllableInit();
    }

    public static ResourceLocation makeResource(String id){
        return ResourceLocation.fromNamespaceAndPath(ReviveMe.MOD_ID, id);
    }
}
