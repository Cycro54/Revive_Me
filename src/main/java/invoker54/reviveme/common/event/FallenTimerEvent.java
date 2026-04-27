package invoker54.reviveme.common.event;

import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHealEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID)
public class FallenTimerEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallenTimerEvent.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void changeGamemode(PlayerEvent.PlayerChangeGameModeEvent event){
        Player player = event.getPlayer();
        if (!(FallenCapability.get(player).isFallen())) return;
        if (event.getNewGameMode() != GameType.CREATIVE && event.getNewGameMode() != GameType.SPECTATOR) return;
        ReviveMeConfig.configReviveData.revivePlayer(player,false, player, FallenCapability.SELFREVIVETYPE.CREATIVE);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void cancelFallenHeal(LivingHealEvent event){
        if (!(event.getEntityLiving() instanceof Player)) return;
        Player player = (Player) event.getEntityLiving();
        FallenCapability cap = FallenCapability.get(player);
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
    public static void TickDownTimer(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;

        //System.out.println("Game time is: " + event.player.level.getGameTime());
        if (event.player.isDeadOrDying()) return;

        FallenCapability cap = FallenCapability.get(event.player);

        if (!cap.isFallen() || cap.getOtherPlayer() != null) return;

        event.player.setForcedPose(null); //Mixin will assign the correct pose (PlayerMixin)

        if (event.player.getLevel().isClientSide) return;
//        LOGGER.warn("What's pose: " + event.player.getForcedPose());

        if (!ReviveMeConfig.reviveMeEnabled){
            event.player.displayClientMessage(InvoText.translate("revive_me.disabled").getText(), false);
            cap.forceDeath();
        }

        //Make sure they aren't sprinting.
        if (event.player.isSprinting()) event.player.setSprinting(false);

        double maxHealth = ReviveMeConfig.fallenHealth;
        if (maxHealth <= 0) maxHealth = event.player.getMaxHealth();
        else if (maxHealth < 1) maxHealth = event.player.getMaxHealth() * maxHealth;
        maxHealth = Math.max(maxHealth, 1);
        //Make sure they aren't healing
        if (event.player.getHealth() > maxHealth) {
            event.player.setHealth((float) maxHealth);
        }

        //Make sure they have no food either
        event.player.getFoodData().setFoodLevel(1);

        //Check if the original effects were removed
        cap.removeOriginalEffects(false);

        //Finally make sure they have all the required effects.
        FallEvent.modifyPotionEffects(event.player);

        if (!ReviveMeConfig.dieWhenTimerEnds) return;
        if (!cap.timeRanOut()) return;

        cap.forceDeath();
        //System.out.println("Who's about to die: " + event.player.getDisplayName());
    }

    //Make sure this only runs for the person being revived
    @SubscribeEvent
    public static void TickProgress(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) return;
        if (event.side != LogicalSide.SERVER) return;

        FallenCapability cap = FallenCapability.get(event.player);

        //make sure other player isn't null
        if (cap.getOtherPlayer() == null) return;

        Player otherPlayer = event.player.getServer().getPlayerList().getPlayer(cap.getOtherPlayer());

        if (otherPlayer != null && cap.getReviveStack() != null) otherPlayer.getCooldowns().addCooldown(cap.getReviveStack().getItem(), 30);

        //If tick progress finishes, revive the fallen player and take whatever you need to take from the otherPlayer
        if (cap.getProgress(true) < 1) return;

        //Make sure this person is fallen.
        if (!cap.isFallen()) return;

        Player fellPlayer = event.player;
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
