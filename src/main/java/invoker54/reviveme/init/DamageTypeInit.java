package invoker54.reviveme.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static invoker54.reviveme.ReviveMe.MOD_ID;

public class DamageTypeInit {
    private static final Logger LOGGER = LogManager.getLogger();

    public static ResourceKey<DamageType> KILL_REVIVE;

    public static void init(){
        KILL_REVIVE =
                ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(MOD_ID, "kill_revive_damage_type"));
    }
}
