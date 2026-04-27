package invoker54.reviveme.common.event;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;


@EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallenTimerEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void changeGamemode(PlayerEvent.PlayerChangeGameModeEvent event){
        Player player = event.getEntity();
        if (!(FallenData.get(player).isFallen())) return;
        if (event.getNewGameMode() != GameType.CREATIVE && event.getNewGameMode() != GameType.SPECTATOR) return;
        ReviveMeConfig.configReviveData.revivePlayer(player,false, player, FallenData.SELFREVIVETYPE.CREATIVE);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelFallenHeal(LivingHealEvent event){
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        FallenData cap = FallenData.get(player);
        if (!cap.isFallen()) return;

        boolean canRevive = cap.canPlayerRevive() && cap.addOverheal(event.getAmount());
        if (canRevive){
            ReviveMeConfig.configReviveData.revivePlayer(player, false, null, "overheal");
            return;
        }
        if (ReviveMeConfig.overhealAmount != 0) {
            cap.syncClient(false);
        }

        float maxHealth = (float) ReviveMeConfig.fallenHealth;
        if (maxHealth <= 0) maxHealth = player.getMaxHealth();
        else if (maxHealth < 1) maxHealth = player.getMaxHealth() * maxHealth;
        maxHealth = Math.max(maxHealth, 1);

        float maxHeal = maxHealth - player.getHealth();

        event.setAmount(Math.min(event.getAmount(), maxHeal));
    }

    @SubscribeEvent
    public static void TickDownTimer(PlayerTickEvent.Pre event) {
        //System.out.println("Game time is: " + event.getEntity().level.getGameTime());
        if (event.getEntity().isDeadOrDying()) return;

        FallenData cap = FallenData.get(event.getEntity());

        if (!cap.isFallen() || cap.getOtherPlayer() != null) return;

//        LOGGER.warn("Whats my revive options? "+ cap.getSelfReviveOption(0) + ":"+cap.getSelfReviveOption(1));

        event.getEntity().setForcedPose(null); //Mixin will assign the correct pose (PlayerMixin)

        if (event.getEntity().level().isClientSide()) return;
//        LOGGER.warn("What's pose: " + event.getEntity().getForcedPose());

        if (!ReviveMeConfig.reviveMeEnabled){
            event.getEntity().sendSystemMessage(InvoText.translate("revive_me.disabled").getText());
            cap.forceDeath();
        }

        //Make sure they aren't sprinting.
        if(event.getEntity().isSprinting()) event.getEntity().setSprinting(false);

        double maxHealth = ReviveMeConfig.fallenHealth;
        if (maxHealth <= 0) maxHealth = event.getEntity().getMaxHealth();
        else if (maxHealth < 1) maxHealth = event.getEntity().getMaxHealth() * maxHealth;
        maxHealth = Math.max(maxHealth, 1);
        //Make sure they aren't healing
        if (event.getEntity().getHealth() > maxHealth) {
            event.getEntity().setHealth((float) maxHealth);
        }

        //Make sure they have no food either
        event.getEntity().getFoodData().setFoodLevel(1);

        //Check if the original effects were removed
        cap.removeOriginalEffects(false);

        //Finally make sure they have all the required effects.
        FallEvent.modifyPotionEffects(event.getEntity());

        if (!ReviveMeConfig.dieWhenTimerEnds) return;
        if (!cap.timeRanOut()) return;

        cap.forceDeath();
        //System.out.println("Who's about to die: " + event.player.getDisplayName());
    }

    //Make sure this only runs for the person being revived
    @SubscribeEvent
    public static void TickProgress(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;

        FallenData cap = FallenData.get(event.getEntity());

        //make sure other player isn't null
        if (cap.getOtherPlayer() == null) return;

        Player otherPlayer = event.getEntity().level().getServer().getPlayerList().getPlayer(cap.getOtherPlayer());

        if (otherPlayer != null && cap.getReviveStack() != null) otherPlayer.getCooldowns().addCooldown(cap.getReviveStack(), 30);

        //If tick progress finishes, revive the fallen player and take whatever you need to take from the otherPlayer
        if (cap.getProgress(true) < 1) return;

        //Make sure this person is fallen.
        if (!cap.isFallen()) return;

        Player fellPlayer = event.getEntity();
        if (otherPlayer == null) return;
        cap.incrementReviveCount(otherPlayer);

        ReviveItemData reviveData = ReviveItemData.getData(cap.getReviveStack(), ReviveItemData.USER.REVIVER);
        if (reviveData != null){
            if (!otherPlayer.isCreative()) reviveData.takeItemCount(otherPlayer, cap.getReviveStack());
            reviveData.revivePlayer(fellPlayer, false, otherPlayer, "item");
        }
        else
        {
            ReviveMeConfig.configReviveData.takeFromReviver(otherPlayer, fellPlayer);
            ReviveMeConfig.configReviveData.revivePlayer(fellPlayer, false, otherPlayer, ReviveMeConfig.penaltyType);
        }
    }
}
