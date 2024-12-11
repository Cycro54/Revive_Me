package invoker54.reviveme.common.event;

import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class attackFallenEvents {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void playerAttackFallen(LivingAttackEvent event) {
        if (event.isCanceled()) return;

        if (!(event.getEntity() instanceof Player player)) return;

        if (event.getEntity().level.isClientSide) return;

        FallenCapability cap = FallenCapability.GetFallCap(player);

        //If it's a source that goes through invulnerability, let it pass
        if (event.getSource() != null && event.getSource().isBypassInvul()) {
            return;
        }

        //If they aren't fallen, let it pass.
        else if (!cap.isFallen()) {
            return;
        }

        //If the damage source is a player that's crouching, let it pass
        else if (event.getSource() != null && (event.getSource().getEntity() instanceof Player)
                && event.getSource().getEntity().isCrouching() && cap.getKillTime() == 0) {
            player.setInvulnerable(false);
        }

        //Else cancel the attack event
        else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void CancelMobTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob)) return;
        if (!(event.getNewTarget() instanceof Player player)) return;

        FallenCapability cap = FallenCapability.GetFallCap(player);
        if (cap.isFallen()) {
            if (mob instanceof NeutralMob) {
                //Trick the target selector for the mob by making it so the player appears to be dead
                ((NeutralMob) mob).playerDied(player);
            }
            ((Mob) event.getEntity()).setTarget(null);
        }
    }

    @SubscribeEvent
    public static void fallenInvisible(LivingEvent.LivingVisibilityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(FallenCapability.GetFallCap(event.getEntity()).isFallen())) return;

        event.modifyVisibility(0);
    }
}



