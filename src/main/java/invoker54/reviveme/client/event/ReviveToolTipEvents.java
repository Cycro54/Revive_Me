package invoker54.reviveme.client.event;

import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import invoker54.reviveme.init.EffectInit;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class ReviveToolTipEvents {
    public static final ModLogger LOGGER = ModLogger.getLogger(ItemTooltipEvent.class, ReviveMeConfig.debugMode);
    private static final String tooltipLangDirectory = "reviveme.item.tooltip.";

    public static final InvoText combinationText = InvoText.translate(tooltipLangDirectory +"combination");
    public static final InvoText idText = InvoText.translate(tooltipLangDirectory +"id");
    public static final InvoText displayText = InvoText.translate(tooltipLangDirectory +"name");
    public static final InvoText descriptionText = InvoText.translate(tooltipLangDirectory +"description");
    public static final InvoText userText = InvoText.translate(tooltipLangDirectory +"user");
    public static final InvoText reviveSecondsText = InvoText.translate(tooltipLangDirectory +"seconds");
    public static final InvoText reviveHealthText = InvoText.translate(tooltipLangDirectory +"health");
    public static final InvoText reviveFoodText = InvoText.translate(tooltipLangDirectory +"food");
    public static final InvoText revivePenaltyText = InvoText.translate(tooltipLangDirectory +"penalty");
    public static final InvoText countText = InvoText.translate(tooltipLangDirectory +"count");
    public static final InvoText reviveChanceText = InvoText.translate(tooltipLangDirectory +"chance");
    public static final InvoText fallenTimerChangeText = InvoText.translate(tooltipLangDirectory +"fallen_timer_change");
    public static final InvoText refreshOptionsText = InvoText.translate(tooltipLangDirectory +"refresh_options");
//    public static final InvoText maxUsesText = InvoText.translate(tooltipLangDirectory +"uses");
    public static final InvoText useReviveOnFailText = InvoText.translate(tooltipLangDirectory +"use_revive");
    public static final InvoText effectsText = InvoText.translate(tooltipLangDirectory +"effects");
    public static final InvoText unlimitedText = InvoText.translate(tooltipLangDirectory +"unlimited");
    public static boolean isKeybindDown = false;

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void beforeToolTip(ItemTooltipEvent event) {
        if (!ReviveMeConfig.showToolTip) return;
        List<ITextComponent> list = event.getToolTip();
        ReviveItemData itemData = ReviveItemData.getData(event.getItemStack(), ReviveItemData.USER.BOTH);
        if (itemData == null) return;

        list.add(InvoText.translate("reviveme.item.tooltip.combination",
                KeyInit.tooltip.keyBind.getTranslatedKeyMessage()).getText());
        //Display
        list.add(displayText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatString(itemData.getDisplayName().getString()).getText()).getText());
        //ID
        list.add(idText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatString(itemData.getIdName()).getText()).getText());
        //Item User
        list.add(userText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatString(itemData.getItemUser().name()).getText()).getText());
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void afterToolTip(ItemTooltipEvent event) {
        if (!ReviveMeConfig.showToolTip) return;

        ReviveItemData itemData = ReviveItemData.getData(event.getItemStack(), ReviveItemData.USER.BOTH);
        if (itemData == null) return;
        if (!isKeybindDown) return;
        List<ITextComponent> list = event.getToolTip();
        list.clear();

        //Display
        list.add(displayText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatString(itemData.getDisplayName().getString()).getText()).getText());
        //ID
        list.add(idText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatString(itemData.getIdName()).getText()).getText());
        //Item User
        list.add(userText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatString(itemData.getItemUser().name()).getText()).getText());

        //Description
        list.add(itemData.getDescription().getText());

        //Revive Seconds
        list.add(reviveSecondsText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatNumber(itemData.getReviveSeconds(), true, false).getText()).getText());

        //Revive Health
        list.add(reviveHealthText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatNumber(itemData.getRevivedHealth(), true, itemData.getRevivedHealth() < 1).getText()).getText());

        //Revive Food
        list.add(reviveFoodText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatNumber(itemData.getRevivedFood(), true, itemData.getRevivedFood() < 1).getText()).getText());

        //Count Required
        list.add(countText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatNumber(itemData.getCountRequired(), true, false).getText()).getText());
//
//        if (itemData.getMaxUses() != 0) {
//            //Max Uses
//            list.add(maxUsesText.withStyle(true).setArgs(FallenItemScreenEvent
//                    .formatNumber(itemData.getMaxUses(), true, false).getText()).getText());
//        }
//        else {
//            //Max Uses
//            list.add(maxUsesText.withStyle(true).setArgs(unlimitedText.getText()).getText());
//        }

        //Revive Chance
        list.add(reviveChanceText.withStyle(true).setArgs(FallenItemScreenEvent
                .formatNumber(itemData.getReviveChance(), true, true).getText()).getText());

        if (itemData.getReviveChance() != 1) {
            //Fallen Timer Change
            list.add(fallenTimerChangeText.withStyle(true).setArgs(FallenItemScreenEvent
                    .formatNumber(itemData.getFallenTimerChange(), false, false).getText()).getText());

            //Refresh Options
            list.add(refreshOptionsText.withStyle(true).setArgs(FallenItemScreenEvent
                    .formatBoolean(itemData.isRefreshOptions()).getText()).getText());

            //Use Revive On Fail
            list.add(useReviveOnFailText.withStyle(true).setArgs(FallenItemScreenEvent
                    .formatBoolean(itemData.useReviveOnFail()).getText()).getText());
        }

        //Effects
        list.add(effectsText.withStyle(true).getText());
        List<EffectInstance> instanceList = itemData.getReviveEffects();

        int fallenAmp = FallenCapability.get(ClientUtil.getPlayer()).getPenaltyMultiplier();
        instanceList.add(new EffectInstance(EffectInit.FALLEN_EFFECT, (int)(itemData.getFallenPenaltyTimer() * 20), fallenAmp));

        for (EffectInstance instance : itemData.getReviveEffects()){
            InvoText duration = FallenItemScreenEvent.formatNumber(instance.getDuration(), true, false);
            InvoText amp = FallenItemScreenEvent.formatNumber(instance.getAmplifier(), true, false);
            InvoText name = InvoText.component((IFormattableTextComponent) instance.getEffect().getDisplayName());
            InvoText separator = InvoText.literal(":");
            list.add(name.append(separator).append(amp).append(separator).append(duration).getText());
        }
    }
}
