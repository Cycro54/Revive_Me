package invoker54.reviveme.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.MathUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.InvoTextFormat;
import invoker54.reviveme.common.ReviveMathUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Objects;

import static invoker54.invocore.client.util.ClientUtil.getPlayer;
import static invoker54.reviveme.ReviveMe.makeResource;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = net.neoforged.api.distmarker.Dist.CLIENT)
public class FallScreenEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallScreenEvent.class, ReviveMeConfig.debugMode);

    public static final Identifier Timer_TEXTURE = makeResource("textures/screens/timer_background.png");
    public static final Identifier HEALTH_TEXTURE = makeResource("textures/revive_types/heart.png");
    public static final Identifier FOOD_TEXTURE = makeResource("textures/revive_types/hunger.png");
    public static final Identifier EXPERIENCE_TEXTURE = makeResource("textures/revive_types/experience_bottle.png");
    public static final Identifier MOUSE_TEXTURE = makeResource("textures/mouse_icons.png");
    public static final Identifier REVIVE_HELP_BUTTON_TEXTURE = makeResource("textures/revive_help_button.png");
//    public static final Identifier EFFECT_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/sprites/container/inventory/effect_background.png");
    public static final Identifier EFFECT_BACKGROUND = Identifier.withDefaultNamespace("container/inventory/effect_background");

    //Images
    public static ClientUtil.Image timerIMG = new ClientUtil.Image(Timer_TEXTURE, 0, 64, 0, 64);
    public static ClientUtil.Image heartIMG = new ClientUtil.Image(HEALTH_TEXTURE, 0, 8, 0, 8);
    public static ClientUtil.Image xpIMG = new ClientUtil.Image(EXPERIENCE_TEXTURE, 0, 16, 0, 16);
    public static ClientUtil.Image foodIMG = new ClientUtil.Image(FOOD_TEXTURE, 0, 18, 0, 18);
    public static ClientUtil.Image mouse_idle_IMG = new ClientUtil.Image(MOUSE_TEXTURE, 0, 22, 0, 28, 64, 64);
    public static ClientUtil.Image mouse_left_IMG = new ClientUtil.Image(MOUSE_TEXTURE, 0, 22, 28, 28, 64, 64);
    public static ClientUtil.Image mouse_right_IMG = new ClientUtil.Image(MOUSE_TEXTURE, 22, 22, 28, 28, 64, 64);
    public static ClientUtil.Image revive_help_button_IMG = new ClientUtil.Image(REVIVE_HELP_BUTTON_TEXTURE, 0, 31, 0, 31, 32, 32);

    private static final InvoText titleText = InvoText.translate("fallenScreen.fallen_text");
    private static final InvoText reviveCountSelfText = InvoText.translate("fallenScreen.revive_count.self");
    private static final InvoText reviveCountPlayerText = InvoText.translate("fallenScreen.revive_count.player");
    //    private static final InvoText reviveCountTotalText = InvoText.translate("fallenScreen.revive_count.total");
    public static final InvoText reviveCountSingleText = InvoText.translate("fallenScreen.revive_count.single");
    public static final InvoText reviveCountMultipleText = InvoText.translate("fallenScreen.revive_count.multiple");
    private static final InvoText reviveCountFullText = InvoText.translate("fallenScreen.revive_count.full");
    private static final InvoText reviveCountShortText = InvoText.translate("fallenScreen.revive_count.short");
    private static final InvoText waitText = InvoText.translate("fallenScreen.wait_text");
    private static final InvoText forceDeathText = InvoText.translate("fallenScreen.force_death_text");
    private static final InvoText cantForceDeathText = InvoText.translate("fallenScreen.cant_force_death_text");
    public static final DecimalFormat df = new DecimalFormat("0.#");
    public static final int goldProgCircle = new Color(227, 175, 7,244).getRGB();
    public static final int purpleProgCircle = new Color(132, 31, 205,244).getRGB();
    private static final int greenColor = new Color(39, 235, 86, 255).getRGB();
    private static final int whiteColor = new Color(255, 255, 255, 255).getRGB();
    private static final int blackFadeColor = new Color(0, 0, 0, 71).getRGB();
    private static final int redFadeColor = 1615855616;

    //Translation text for self revive
    private static final String fallenDirectory = "revive_me.fallenScreen.self_revive.";

    private static final InvoText selfDestructTxt1 = InvoText.translate(fallenDirectory+"self_destruct_1");
    private static final InvoText selfDestructTxt2 = InvoText.translate(fallenDirectory+"self_destruct_2").withStyle(true, InvoTextFormat.filter( ChatFormatting.RED));
    public static final InvoText cantGiveUp = InvoText.translate(fallenDirectory+"cant_give_up").withStyle(true, InvoTextFormat.filter( ChatFormatting.RED));
    private static final InvoText chanceTxt = InvoText.translate(fallenDirectory+"chance_1");
    private static final InvoText randomItemFalseTxt = InvoText.translate(fallenDirectory+"random_items.false_1");
    private static final InvoText randomItemHotbarTxt = InvoText.translate(fallenDirectory+"random_items.hotbar_check_disabled");
    private static final InvoText randomItemTxt = InvoText.translate(fallenDirectory+"random_items_1");
    private static final InvoText specificItemFalseTxt = InvoText.translate(fallenDirectory+"specific_item.false_1");
    private static final InvoText specificItemTxt = InvoText.translate(fallenDirectory+"specific_item_1");
    private static final InvoText killTxt1 = InvoText.translate(fallenDirectory+"kill_1");
    private static final InvoText killTxt2 = InvoText.translate(fallenDirectory+"kill_2");
    private static final InvoText killTxt3 = InvoText.translate(fallenDirectory+"kill_3");
    private static final InvoText killTxt4 = InvoText.translate(fallenDirectory+"kill_4");
    private static final InvoText killFalseTxt = InvoText.translate(fallenDirectory+"kill_false");
    private static final InvoText statusEffectTxt = InvoText.translate(fallenDirectory+"status_effects_1");
    private static final InvoText experienceFalseTxt = InvoText.translate(fallenDirectory+"experience.false_1");
    private static final InvoText experienceTxt = InvoText.translate(fallenDirectory+"experience_1");
    private static final InvoText callToggleTip = InvoText.translate(fallenDirectory+"revive_help_toggle");
    private static final InvoText screenToggleTip = InvoText.translate(fallenDirectory+"revive_switch_screen");
    private static final InvoText toggleGUITip = InvoText.translate(fallenDirectory+"revive_toggle_gui");
    private static final InvoText giveUpTip = InvoText.translate(fallenDirectory+"revive_give_up");

    public static boolean guiToggled = false;
    public static double guiToggleTransitionStartTicks = 0;
    public final static float guiToggleTransitionTicks = 8;


    @SubscribeEvent
    public static void registerFallenScreen(RegisterGuiLayersEvent event) {
        Minecraft mC = ClientUtil.getMinecraft();

        event.registerAbove(VanillaGuiLayers.CHAT, makeResource("revive_button"), ((GuiGraphicsExtractor, tracker) ->
        {
            FallenData cap = getCap();
            if (cap == null) return;
            if ((ClientUtil.getMinecraft().screen instanceof ChatScreen)) return;

            InvoZone workZone = new InvoZone(0, GuiGraphicsExtractor.guiWidth(), 0, GuiGraphicsExtractor.guiHeight());

            InvoText callForHelpMsg = InvoText.literal("[" + KeyInit.callForHelpKey.keyBind.getKey().getDisplayName().getString()
                    + "]").withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD));
            if (cap.callForHelpCooldown() < 1)
                callForHelpMsg.withStyle(true, InvoTextFormat.filter(ChatFormatting.BLACK));

            InvoZone reviveButtonZone = revive_help_button_IMG.getRenderZone();
            reviveButtonZone.setDown(workZone.down() - 16).setRight(workZone.right() - 16);

            boolean isSneaking = ClientUtil.getMinecraft().player.isShiftKeyDown();
            //region This is for the call toggle tip
            InvoText toggleText = callToggleTip.setArgs(InvoText.literal(ClientUtil.getMinecraft().options.keyShift.getKey().getDisplayName().getString()).withStyle(false, InvoTextFormat.filter(ChatFormatting.YELLOW)).getText());
            if (isSneaking) toggleText = callToggleTip.setArgs(InvoText.translate(KeyInit.callForHelpKey.keyBind.getKey().getDisplayName().getString())
                    .withStyle(false, InvoTextFormat.filter(ChatFormatting.YELLOW)).getText());
            toggleText.withStyle(true, InvoTextFormat.filter( ChatFormatting.BOLD, ChatFormatting.WHITE));

            InvoZone toggleZone = reviveButtonZone.copy().splitHeight(2, 1).splitWidth(1, 1.5F)
                    .centerX(reviveButtonZone.middleX()).setDown(reviveButtonZone.y() - 4);

            Color toggleBGColor = new Color(0, 0, 0, 200);
            if (cap.isCallToggled()) toggleBGColor = new Color(58, 243, 74, 200);
            Color toggleFGColor = new Color(0, 0, 0, 200);

            ClientUtil.blit2DColor(GuiGraphicsExtractor, toggleZone, toggleBGColor.getRGB());
            ClientUtil.blit2DColor(GuiGraphicsExtractor, toggleZone.copy().inflate(-1, -1), toggleFGColor.getRGB());
            TextUtil.render2DText(GuiGraphicsExtractor, toggleText.getText(), false, 2,
                    toggleZone.copy().inflate(-2, -1.5f), TextUtil.txtAlignment.MIDDLE);
            //endregion

            boolean isOptionsDisabled = ReviveMeConfig.selfReviveOptions.isEmpty();
            boolean isItemsDisabled = ReviveMeConfig.itemUser == ReviveItemData.USER.REVIVER || ReviveMeConfig.itemUser == ReviveItemData.USER.NONE;
            boolean canSwitchScreens = cap.canSelfRevive() && !isOptionsDisabled && !isItemsDisabled;

            //Toggle GUI option
            if (ReviveMeConfig.canToggleGUI) {
                toggleText = toggleGUITip.setArgs(InvoText.literal(mC.options.keyShift.getKey().getDisplayName().getString())
                        .withStyle(false, InvoTextFormat.filter(ChatFormatting.YELLOW)).getText());
                if (isSneaking) toggleText = toggleGUITip.setArgs(InvoText.literal(KeyInit.toggleGUIKey
                        .keyBind.getKey().getDisplayName().getString()).withStyle(false, InvoTextFormat.filter(ChatFormatting.YELLOW)).getText());
                toggleText.withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.WHITE));

                toggleZone.setDown(toggleZone.y() - 4);

                toggleBGColor = guiToggled ? new Color(251, 201, 38, 200) : new Color(0, 0, 0, 200);

                ClientUtil.blit2DColor(GuiGraphicsExtractor, toggleZone, toggleBGColor.getRGB());
                ClientUtil.blit2DColor(GuiGraphicsExtractor, toggleZone.copy().inflate(-1, -1), toggleFGColor.getRGB());
                TextUtil.render2DText(GuiGraphicsExtractor, toggleText.getText(), false, 2,
                        toggleZone.copy().inflate(-2, -1.5f), TextUtil.txtAlignment.MIDDLE);
            }

            if (!guiToggled && canSwitchScreens) {
                //region This is for switching self revive modes (GENERAL or ITEMS)
                toggleText = screenToggleTip.setArgs(InvoText.literal(ClientUtil.getMinecraft().options.keyShift.getKey().getDisplayName().getString())
                        .withStyle(false, InvoTextFormat.filter(ChatFormatting.YELLOW)).getText());
                if (isSneaking) toggleText = screenToggleTip.setArgs(InvoText.literal(KeyInit.leftOption
                        .keyBind.getKey().getDisplayName().getString()).withStyle(false, InvoTextFormat.filter(ChatFormatting.YELLOW)).getText());
                toggleText.withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.WHITE));

                toggleZone.setDown(toggleZone.y() - 4);

                toggleBGColor = new Color(0, 0, 0, 200);

                ClientUtil.blit2DColor(GuiGraphicsExtractor, toggleZone, toggleBGColor.getRGB());
                ClientUtil.blit2DColor(GuiGraphicsExtractor, toggleZone.copy().inflate(-1, -1), toggleFGColor.getRGB());
                TextUtil.render2DText(GuiGraphicsExtractor, toggleText.getText(), false, 2,
                        toggleZone.copy().inflate(-2, -1.5f), TextUtil.txtAlignment.MIDDLE);
                //endregion
            }

            //Give up option
            if (!guiToggled && cap.canSelfRevive() && !FallenItemScreenEvent.isItemScreenActive && ReviveMeConfig.selfReviveOptions.size() > 1) {
                toggleText = giveUpTip.setArgs(InvoText.literal(mC.options.keyShift.getKey().getDisplayName().getString())
                        .withStyle(false, InvoTextFormat.filter(ChatFormatting.YELLOW)).getText());
                if (isSneaking) toggleText = screenToggleTip.setArgs(InvoText.literal(KeyInit.rightOption
                        .keyBind.getKey().getDisplayName().getString()).withStyle(false, InvoTextFormat.filter(ChatFormatting.YELLOW)).getText());
                toggleText.withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.WHITE));

                toggleZone.setDown(toggleZone.y() - 4);

                toggleBGColor = new Color(182, 55, 30, 200);

                ClientUtil.blit2DColor(GuiGraphicsExtractor, toggleZone, toggleBGColor.getRGB());
                ClientUtil.blit2DColor(GuiGraphicsExtractor, toggleZone.copy().inflate(-1, -1), toggleFGColor.getRGB());
                TextUtil.render2DText(GuiGraphicsExtractor, toggleText.getText(), false, 2,
                        toggleZone.copy().inflate(-2, -1.5f), TextUtil.txtAlignment.MIDDLE);
            }

            //Give up popup will be a little red AND only shows on the self revive screen.
            //Toggle GUI will ALWAYS show (unless disabled)

            //Basic GREEN animation to make revive button pop out
            //Every five seconds do a 0.2 second animation
            long tickCount = getPlayer().level().getGameTime() % (6*20);
            long maxTicks = 15;
            long maxInflate = 5;
            if (!cap.isCallingForHelp() && tickCount > (5 * 20)) {
                tickCount -= (5 * 20);
                tickCount = Math.min(maxTicks, tickCount);
                double progress = MathUtil.EaseType.EASEOUTBOUNCE.getEase((double) (tickCount + getPartialTicks()) / maxTicks);
                progress = Math.min(1, progress);
                ClientUtil.blit2DColor(GuiGraphicsExtractor, reviveButtonZone.copy().
                        inflate((float) (maxInflate * progress), (float) (maxInflate * progress)), new Color(58, 243, 74, (int) (255 - (255 * progress))).getRGB());
            }

            ClientUtil.blit2DColor(GuiGraphicsExtractor, reviveButtonZone.copy().setY(reviveButtonZone.down()).
                    setHeight((int) (reviveButtonZone.height() * cap.callForHelpCooldown())).setBound(reviveButtonZone,true), whiteColor);

            revive_help_button_IMG.render(GuiGraphicsExtractor);
            InvoZone txtZone = reviveButtonZone.copy().splitHeight(4,1);
            txtZone.centerY(reviveButtonZone.y() + ((reviveButtonZone.height()/4)*3));
            TextUtil.render2DText(GuiGraphicsExtractor, callForHelpMsg.getText(), false, 1, txtZone, TextUtil.txtAlignment.MIDDLE);

            if (cap.callForHelpCooldown() != 1){
                ClientUtil.blit2DColor(GuiGraphicsExtractor, reviveButtonZone, blackFadeColor);
            }}));

        //if (true) return;
        event.registerAbove(VanillaGuiLayers.CHAT, makeResource("fallen_screen"), (GuiGraphicsExtractor, tracker) -> {
            //region initial checks
            FallenData cap = getCap();
            if (cap == null) return;
            if ((ClientUtil.getMinecraft().screen instanceof ChatScreen)) return;
            if (cap.canSelfRevive()) return;
            //endregion

            InvoZone workZone = new InvoZone(0, GuiGraphicsExtractor.guiWidth(), 0, GuiGraphicsExtractor.guiHeight());
            ClientUtil.blit2DColor(GuiGraphicsExtractor, workZone, redFadeColor);

            //Title text
            renderHeaderAndReviveCount(GuiGraphicsExtractor, cap, workZone);

            //Wait For text
            InvoZone waitTextZone = workZone.copy().setWidth(workZone.width() / 3).setHeight(8).setY((workZone.height() / 4) + 12)
                    .centerX(workZone.middleX());
            InvoZone toggledWaitTextZone = waitTextZone.copy().setDown(workZone.y());
            waitTextZone = ReviveMathUtil.zoneLerp(FallenItemScreenEvent.getTogglePercentage(), waitTextZone, toggledWaitTextZone);
            TextUtil.render2DText(GuiGraphicsExtractor, waitText.getText(), true, 1, waitTextZone, TextUtil.txtAlignment.MIDDLE);

            //Force death text
            InvoText forceDeathTextResult = ReviveMeConfig.canGiveUp ? forceDeathText : cantForceDeathText;

            forceDeathTextResult = forceDeathTextResult.setArgs(
                    InvoText.literal(VanillaKeybindHandler.getKey(KeyInit.leftOption.keyBind).getDisplayName().getString()).getText(),
                    RenderFallPlateEvent.df.format(2 - (FallenPlayerActionsEvent.timeHeld / 20f)));
            InvoZone forceDeathTextZone = waitTextZone.copy().setY(waitTextZone.down() + 17).setWidth(workZone.width()).centerX(waitTextZone.middleX());
            InvoZone toggledForceDeathTextZone = forceDeathTextZone.copy().setDown(workZone.y());

            forceDeathTextZone = ReviveMathUtil.zoneLerp(FallenItemScreenEvent.getTogglePercentage(), forceDeathTextZone, toggledForceDeathTextZone);


            TextUtil.render2DText(GuiGraphicsExtractor, forceDeathTextResult.getText(), true, 1, forceDeathTextZone, TextUtil.txtAlignment.MIDDLE);

            if (!ReviveMeConfig.compactReviveUI) {
                float timerMiddleY = workZone.down() - (workZone.down() / 3);
                renderTimer(GuiGraphicsExtractor, cap, workZone, timerMiddleY, 36, 64);
            } else {
                float timerMiddleY = workZone.down() - (workZone.down() / 4);
                renderTimer(GuiGraphicsExtractor, cap, workZone, timerMiddleY, 18, 32);
            }
        });

        event.registerAbove(VanillaGuiLayers.CHAT, makeResource("fallen_self_revive_screen"), (GuiGraphicsExtractor, tracker) -> {
            FallenData cap = getCap();
            if (cap == null) return;
            if ((ClientUtil.getMinecraft().screen instanceof ChatScreen)) return;
            if (!cap.canSelfRevive()) return;
            if (ReviveMeConfig.selfReviveOptions.isEmpty() && !FallenItemScreenEvent.isItemScreenActive) {
                FallenItemScreenEvent.switchReviveScreens();
            }

            InvoZone workZone = new InvoZone(0, GuiGraphicsExtractor.guiWidth(), 0, GuiGraphicsExtractor.guiHeight());

            ClientUtil.blit2DColor(GuiGraphicsExtractor, workZone, redFadeColor);

            renderHeaderAndReviveCount(GuiGraphicsExtractor, cap, workZone);

            InvoZone leftZone;
            InvoZone rightZone;

            if (!ReviveMeConfig.compactReviveUI) {
                float timerMiddleY = workZone.down() - (workZone.down() / 3);
                leftZone = workZone.copy().splitWidth(4, 1);
                leftZone.splitHeight(5, 3);
                leftZone.center(workZone.copy().setWidth(workZone.width() / 2));
                renderTimer(GuiGraphicsExtractor, cap, workZone, timerMiddleY, 36, 64);
            } else {
                float timerMiddleY = workZone.down() - (workZone.down() / 4);
                leftZone = workZone.copy().splitWidth(5, 1);
                leftZone.splitHeight(3, 1);
                leftZone.centerX(workZone.copy().splitWidth(2, 1).middleX());
                leftZone.setDown(timerMiddleY + 18);
                renderTimer(GuiGraphicsExtractor, cap, workZone, timerMiddleY, 18, 32);
            }
            rightZone = leftZone.copy().mirrorX(workZone.middleX());

            double switchPercentage = FallenItemScreenEvent.getSwitchPercentage();
            boolean isSwitchDone = switchPercentage == 1;
            if (!FallenItemScreenEvent.isItemScreenActive) switchPercentage = 1 - switchPercentage;
            double togglePercent = FallenItemScreenEvent.getTogglePercentage();

            if (FallenItemScreenEvent.isItemScreenActive || !isSwitchDone) {
                FallenItemScreenEvent.renderItemPage(GuiGraphicsExtractor, switchPercentage, workZone, leftZone.copy(), rightZone.copy());
            }

            if (!FallenItemScreenEvent.isItemScreenActive || !isSwitchDone) {
                //region Self Revive Options
                double timeHeldPercentage = FallenPlayerActionsEvent.timeHeld / 40D;
                float sizeChange = MathUtil.lerp(MathUtil.EaseType.EASEOUTQUAD.getEase(timeHeldPercentage), 0, 10);

                timeHeldPercentage = MathUtil.EaseType.EASEOUTQUAD.getEase(Math.min(1, timeHeldPercentage * 2));

                if (VanillaKeybindHandler.attackHeld) {
                    rightZone.setX(MathUtil.lerp(timeHeldPercentage, rightZone.x(), workZone.right()));
                    rightZone.inflate(-sizeChange, -sizeChange);
                    leftZone.inflate(sizeChange, sizeChange);
                } else if (VanillaKeybindHandler.useHeld) {
                    leftZone.setX(MathUtil.lerp(timeHeldPercentage, leftZone.x(), 0 - rightZone.width()));
                    leftZone.inflate(-sizeChange, -sizeChange);
                    rightZone.inflate(sizeChange, sizeChange);
                }

                leftZone.setY(MathUtil.lerp(switchPercentage, leftZone.y(), workZone.down()));
                leftZone.setY(MathUtil.lerp(togglePercent, leftZone.y(), workZone.down()));
                rightZone.setY(MathUtil.lerp(switchPercentage, rightZone.y(), workZone.down()));
                rightZone.setY(MathUtil.lerp(togglePercent, rightZone.y(), workZone.down()));

                renderReviveOption(GLFW.GLFW_MOUSE_BUTTON_1, GuiGraphicsExtractor, leftZone, cap, VanillaKeybindHandler.attackHeld);
                int secondOption = ClientUtil.getPlayer().isDiscrete() || ReviveMeConfig.selfReviveOptions.size() == 1 ? -1 : GLFW.GLFW_MOUSE_BUTTON_2;
                renderReviveOption(secondOption, GuiGraphicsExtractor, rightZone, cap, VanillaKeybindHandler.useHeld);
                //endregion
            }
        });

        event.registerBelowAll(makeResource("chat_fallen_timer_screen"), (GuiGraphicsExtractor, tracker) -> {
            FallenData cap = getCap();
            if (cap == null) return;
            if (!(ClientUtil.getMinecraft().screen instanceof ChatScreen)) return;

            InvoZone workZone = new InvoZone(0, GuiGraphicsExtractor.guiWidth(), 0, GuiGraphicsExtractor.guiHeight());

            renderTimer(GuiGraphicsExtractor, cap, workZone, workZone.down() - (workZone.down() / 3), 36, 64);
        });
    }
    public static void renderHeaderAndReviveCount(GuiGraphicsExtractor GuiGraphicsExtractor, FallenData cap, InvoZone workZone){
        //Title text
        InvoZone titleTextZone = workZone.copy().setWidth(workZone.width() / 3).setHeight(workZone.height() / 5).inflate(0, -12)
                .centerX(workZone.middleX());
        InvoZone toggledTitleTextZone = titleTextZone.copy().setDown(workZone.y() - (titleTextZone.height() * 2));
        titleTextZone = ReviveMathUtil.zoneLerp(FallenItemScreenEvent.getTogglePercentage(), titleTextZone, toggledTitleTextZone);

        TextUtil.render2DText(GuiGraphicsExtractor, titleText.getText(), true, 1, titleTextZone, TextUtil.txtAlignment.MIDDLE);

        //All different renders
        /*
        You have 3 Player Revives
        You have 3 Total Revives
        You have 5 Self Revives

        You have 3 revives left
        2 self         1 player

          max is 6
          5 and 6

          max is now 4
          3 and 4
          Then nothing...
         */
        int totalRevives = cap.getTotalReviveCount(true);
        int selfRevives = cap.getSelfReviveCount(true);
        int playerRevives = cap.getPlayerReviveCount(true);
        boolean allTheSame = (Objects.equals(ReviveMeConfig.maxTotalRevives, ReviveMeConfig.maxSelfRevives)) &&
                (Objects.equals(ReviveMeConfig.maxTotalRevives, ReviveMeConfig.maxPlayerRevives));
        if (allTheSame && ReviveMeConfig.maxTotalRevives == -1) return;

        int mainCount;
        InvoText mainTypeText;

        //Main one happens if
        //They are all the same
        //Self and Player aren't infinite
        if ((selfRevives > 0 && playerRevives > 0)){
            mainCount = totalRevives;
            mainTypeText = InvoText.literal("");
        }
        else if (selfRevives > 0 && cap.canSelfRevive()){
            mainCount = selfRevives;
            mainTypeText = reviveCountSelfText;
        }
        //In this case, player revives can't be infinite
        else if (playerRevives > 0 && cap.canPlayerRevive()){
            mainCount = playerRevives;
            mainTypeText = reviveCountPlayerText;
        }
        else return;

        InvoZone reviveTextZone = titleTextZone.copy().setWidthConstraint(titleTextZone.width() * 0.7F).centerX(titleTextZone.middleX())
                .setY(titleTextZone.down()).splitHeight(3, 2);
        renderReviveCount(GuiGraphicsExtractor, reviveTextZone, mainCount, mainTypeText, reviveCountFullText);

        if ((selfRevives <= 0 || playerRevives <= 0) || (totalRevives == selfRevives && totalRevives == playerRevives)) return;
        if (!cap.canPlayerRevive() || !cap.canSelfRevive()) return;

        InvoZone reviveLeftZone = reviveTextZone.copy().splitWidth(5, 2).shiftXY(0, reviveTextZone.height());
        renderReviveCount(GuiGraphicsExtractor, reviveLeftZone, selfRevives, reviveCountSelfText, reviveCountShortText);

        InvoZone reviveRightZone = reviveLeftZone.copy().mirrorX(reviveTextZone.middleX());
        renderReviveCount(GuiGraphicsExtractor, reviveRightZone, playerRevives, reviveCountPlayerText, reviveCountShortText);
    }

    public static void  renderReviveCount(GuiGraphicsExtractor GuiGraphicsExtractor, InvoZone renderZone, int reviveCount, InvoText typeText, InvoText mainText) {
        boolean turnRed = reviveCount <= 1;
        boolean isSingle = reviveCount == 1;

        InvoText countText = InvoText.literal(reviveCount + "")
                .withStyle(true, InvoTextFormat.filter((turnRed ? ChatFormatting.RED : ChatFormatting.YELLOW), ChatFormatting.BOLD));
        InvoText pluralText = isSingle ? reviveCountSingleText : reviveCountMultipleText;

        mainText = mainText.setArgs(countText.getText(), typeText.getText(), pluralText.getText());

        ClientUtil.blit2DColor(GuiGraphicsExtractor, renderZone, new Color(0, 0, 0, 150).getRGB());
        TextUtil.render2DText(GuiGraphicsExtractor, mainText.getText(), true, 1,
                renderZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);
    }

    public static void renderReviveOption(int mouseButton, GuiGraphicsExtractor GuiGraphicsExtractor, InvoZone workZone, FallenData cap, boolean beingHeld) {
        FallenData.SELFREVIVETYPE selfReviveType = cap.getSelfReviveOption(mouseButton);
        Minecraft inst = ClientUtil.getMinecraft();

        boolean shouldPass = true;
        ClientUtil.blit2DColor(GuiGraphicsExtractor, workZone, blackFadeColor);
        InvoZone headerZone = workZone.copy().splitHeight(4, 1);
        InvoZone mainZone = workZone.copy().setY(headerZone.down()).setHeight(workZone.height()-headerZone.height());
        InvoZone progressZone = mainZone.copy();
        InvoText chosenTxt = null;
        ClientUtil.blit2DColor(GuiGraphicsExtractor, mainZone, new Color(0, 0, 0, 128).getRGB());

        switch (selfReviveType) {
            case CHANCE: {
                chosenTxt = chanceTxt;
                int reviveChance = (int) Math.round(100 * (Math.max(0, ReviveMeConfig.reviveChance*(1 - cap.getSelfPenaltyPercentage()))));
                shouldPass = reviveChance > 0;
                InvoText chanceNumberTxt =
                        InvoText.literal(reviveChance+"%")
                                .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, (shouldPass ? ChatFormatting.GOLD : ChatFormatting.RED)));

                TextUtil.render2DText(GuiGraphicsExtractor, chanceNumberTxt.getText(), true, 1, mainZone.copy()
                        .splitHeight(6,4f).centerY(mainZone.middleY())
                        .inflate(-12,0), TextUtil.txtAlignment.MIDDLE);
                break;
            }
            case RANDOM_ITEMS: {
                chosenTxt = randomItemTxt;
                if (!ReviveMeConfig.sacrificialItemTakesHotbar){
                    chosenTxt = randomItemTxt.deepCopy().append(InvoText.literal("\n"))
                            .append(randomItemHotbarTxt.deepCopy().withStyle(true, InvoTextFormat.filter( ChatFormatting.RED)));
                }
                float randomItemPadding = 2;

                ArrayList<ItemStack> itemArrayList = cap.getItemList();
                if (itemArrayList.isEmpty()){
                    shouldPass = false;
                    TextUtil.render2DText(GuiGraphicsExtractor, randomItemFalseTxt.deepCopy().withStyle(true,
                                            InvoTextFormat.filter(ChatFormatting.RED)).append(InvoText.literal("\n\n"))
                                    .append(randomItemHotbarTxt.deepCopy().withStyle(true, InvoTextFormat.filter(ChatFormatting.RED))).getText(),
                            true, 0, mainZone.inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);
                    break;
                }

                //Remove the padding space
                mainZone.setHeight(mainZone.height() - (randomItemPadding * 3));
                mainZone.splitHeight(4, 1);
                float randomItemSize = mainZone.height() - (randomItemPadding * 2);

                for (ItemStack sacrificeStack : itemArrayList) {
                    //Draw the background
                    ClientUtil.blit2DColor(GuiGraphicsExtractor, mainZone, new Color(0, 0, 0, 255).getRGB());

                    InvoZone randomItemFullZone = mainZone.copy().inflate(-randomItemPadding, -randomItemPadding);
                    InvoZone randomItemImageZone = randomItemFullZone.copy().setWidth(randomItemSize);
                    InvoZone randomItemTxtZone = randomItemFullZone.copy().setWidth(randomItemFullZone.width() - randomItemImageZone.width())
                            .setX(randomItemImageZone.right()).splitWidth(3, 1);

                    //Draw the item
                    ClientUtil.blit2DItem(GuiGraphicsExtractor, randomItemImageZone, sacrificeStack);

                    //Draw the amount they have, then the amount they will have after reduction
                    int count = FallenData.getSacrificeItems(inst.player.getInventory(), sacrificeStack).getRight();
                    TextUtil.render2DText(GuiGraphicsExtractor, InvoText.literal("" + count).withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.GREEN)).getText(),
                            true, 1, randomItemTxtZone, TextUtil.txtAlignment.MIDDLE);
                    randomItemTxtZone.shiftXY(randomItemTxtZone.width(), 0);

                    TextUtil.render2DText(GuiGraphicsExtractor, InvoText.literal(" -> ").withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD)).getText(),
                            true, 1, randomItemTxtZone, TextUtil.txtAlignment.MIDDLE);
                    randomItemTxtZone.shiftXY(randomItemTxtZone.width(), 0);

                    TextUtil.render2DText(GuiGraphicsExtractor, InvoText.literal("" + (count - (Math.round(Math.max(1,
                                            count * ReviveMeConfig.sacrificialItemPercent*(1+cap.getSelfPenaltyPercentage()))))))
                                    .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.RED)).getText(),
                            true, 1, randomItemTxtZone, TextUtil.txtAlignment.MIDDLE);

                    mainZone.shiftXY(0, mainZone.height() + randomItemPadding);
                }
                break;
            }
            case KILL: {
                chosenTxt = killTxt1;
                int seconds = (int) ((ReviveMeConfig.reviveKillTime * 20 * (1 - cap.getSelfPenaltyPercentage()))/20);
                shouldPass = seconds > 0;

                if (shouldPass) {
                    mainZone.splitHeight(4, 1);
                    mainZone.setY(mainZone.middleY());
                    TextUtil.render2DText(GuiGraphicsExtractor, killTxt2.withStyle(true, InvoTextFormat.filter(ChatFormatting.GOLD)).setArgs(
                                    InvoText.literal("" + ReviveMeConfig.reviveKillAmount)
                                            .withStyle(true, InvoTextFormat.filter(ChatFormatting.RED)).getText()).getText(),
                            true, 1, mainZone.copy()
                                    .inflate(-3, -3), TextUtil.txtAlignment.MIDDLE);

                    TextUtil.render2DText(GuiGraphicsExtractor, killTxt3.withStyle(true, InvoTextFormat.filter(ChatFormatting.GOLD, ChatFormatting.BOLD)).getText(),
                            true, 1, mainZone.shiftXY(0, mainZone.height()).copy()
                                    .inflate(-3, -3), TextUtil.txtAlignment.MIDDLE);

                    TextUtil.render2DText(GuiGraphicsExtractor, killTxt4.withStyle(true, InvoTextFormat.filter(ChatFormatting.GOLD)).setArgs(InvoText.literal("" + seconds)
                                    .withStyle(true, InvoTextFormat.filter(ChatFormatting.RED)).getText()).getText(),
                            true, 1, mainZone.shiftXY(0, mainZone.height()).copy()
                                    .inflate(-3, -3), TextUtil.txtAlignment.MIDDLE);
                }
                else {
                    TextUtil.render2DText(GuiGraphicsExtractor, killFalseTxt.withStyle(true, InvoTextFormat.filter(ChatFormatting.RED)).getText(),
                            true, 0, mainZone.inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);
                }

                break;
            }
            case STATUS_EFFECTS: {
                chosenTxt = statusEffectTxt;
//                MobEffectTextureManager potionspriteuploader = ClientUtil.getMinecraft().getMobEffectTextures();

//                ClientUtil.Image backgroundImg = new ClientUtil.Image(EFFECT_BACKGROUND, 0, 120, 0, 32);
                InvoZone backgroundZone = new InvoZone(0, 120, 0, 32);

                backgroundZone.setY(headerZone.down()).setWidthConstraint(mainZone.width()-2);

                mainZone.setHeight(mainZone.height()/cap.getNegativeStatusEffects().size());
                for (MobEffect effect : cap.getNegativeStatusEffects()){
                    if (effect == null) continue;
//                    TextureAtlasSprite sprite = potionspriteuploader.get(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
//                    ClientUtil.Image effectIMG = new ClientUtil.Image(sprite.atlasLocation(), sprite.getU0(),
//                            (sprite.getU1() - sprite.getU0()), sprite.getV0(),
//                            (sprite.getV1() - sprite.getV0()), 1, 1);

                    //private void renderEffects(GuiGraphicsExtractor GuiGraphicsExtractor, DeltaTracker deltaTracker) {
                    Identifier effectId = Gui.getMobEffectSprite(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect));
//                    LOGGER.warn("What's the id? " + effectId);
                    TextureAtlasSprite sprite = GuiGraphicsExtractor.guiSprites.getSprite(effectId);
                    ClientUtil.Image effectIMG = new ClientUtil.Image(sprite.atlasLocation(), sprite.getU0(),
                            (sprite.getU1() - sprite.getU0()), sprite.getV0(),
                            (sprite.getV1() - sprite.getV0()), 1, 1);
                    InvoZone effectZone = effectIMG.getRenderZone();

                    backgroundZone.center(mainZone);
//                    GuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, ambient ? EFFECT_BACKGROUND_AMBIENT_SPRITE : EFFECT_BACKGROUND_SPRITE, x, y, k, 32);
                    GuiGraphicsExtractor.blitSprite(RenderPipelines.GUI_TEXTURED, EFFECT_BACKGROUND, (int) backgroundZone.x(), (int) backgroundZone.y(), (int) backgroundZone.width(), (int) backgroundZone.height());
//                    backgroundImg.render(GuiGraphicsExtractor);
                    effectZone.copy(backgroundZone);

                    //Divide the background zone width by 4 and that will be the effect img zone
                    effectZone.splitWidth(4,1).inflate(-4,-4);
                    effectIMG.render(GuiGraphicsExtractor);

                    InvoZone textZone = backgroundZone.copy().splitWidth(4,1);
                    textZone.setX(textZone.right()).setWidth(textZone.width()*3).inflate(-2,-6)
                            .splitHeight(2,1);

                    int amp = (cap.getNegativeStatusEffects().size() > 1 ? 0 : 1);
                    int duration = (int) (20 * ReviveMeConfig.negativeEffectsTime * (1 + cap.getSelfPenaltyPercentage()));
                    MobEffectInstance instance = new MobEffectInstance(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(effect), duration, amp);

                    String s = I18n.get(effect.getDescriptionId());
                    if (instance.getAmplifier() >= 1 && instance.getAmplifier() <= 9) {
                        s = s + ' ' + I18n.get("enchantment.level." + (instance.getAmplifier() + 1));
                    }
                    String s1 = MobEffectUtil.formatDuration(instance, 1.0F, 20).getString();

                    TextUtil.render2DText(GuiGraphicsExtractor, InvoText.literal(s).getText(), true, 1, textZone,
                            TextUtil.txtAlignment.LEFT);
                    textZone.setY(textZone.down()+2);
                    TextUtil.render2DText(GuiGraphicsExtractor, InvoText.literal(s1).withStyle(true, InvoTextFormat.filter(ChatFormatting.DARK_GRAY)).getText(),
                            true, 1, textZone, TextUtil.txtAlignment.LEFT);

                    mainZone.setY(mainZone.down());
                }
                break;
            }
            case EXPERIENCE: {
                chosenTxt = experienceTxt;
                int currentAmount = ReviveMathUtil.getExperienceFromLevel(inst.player);
                int levelsToTake = (int) (inst.player.experienceLevel * ReviveMeConfig.reviveXPLossPercentage * (1 + cap.getSelfPenaltyPercentage()));
                float newLevel = ReviveMathUtil.getLevelFromExperience(currentAmount - ReviveMathUtil.getExperienceFromLevel(levelsToTake, 0));

                InvoZone itemZone = mainZone.copy().splitHeight(4, 3);
                itemZone.inflate(Math.min((itemZone.height()-itemZone.width())/2, 0), Math.min((itemZone.width()-itemZone.height())/2,0));
                ClientUtil.blit2DItem(GuiGraphicsExtractor, itemZone.inflate(-5,-5),
                        new ItemStack(Items.EXPERIENCE_BOTTLE));

                if (ReviveMeConfig.minReviveXPLevel <= inst.player.experienceLevel) {
                    mainZone.splitWidth(3, 1).splitHeight(4, 1).shiftXY(0, mainZone.height() * 3);
                    TextUtil.render2DText(GuiGraphicsExtractor, InvoText.literal(inst.player.experienceLevel + "").withStyle(true, InvoTextFormat.filter(ChatFormatting.GREEN, ChatFormatting.BOLD))
                            .getText(), true, 1, mainZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);

                    mainZone.shiftXY(mainZone.width(), 0);
                    TextUtil.render2DText(GuiGraphicsExtractor, InvoText.literal("->")
                            .getText(), true, 1, mainZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);

                    mainZone.shiftXY(mainZone.width(), 0);
                    TextUtil.render2DText(GuiGraphicsExtractor, InvoText.literal(df.format(newLevel)).withStyle(true, InvoTextFormat.filter(ChatFormatting.RED, ChatFormatting.BOLD))
                            .getText(), true, 1, mainZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);
                }
                else {
                    shouldPass = false;
                    ClientUtil.blit2DColor(GuiGraphicsExtractor, mainZone, new Color(0,0,0,230).getRGB());
                    TextUtil.render2DText(GuiGraphicsExtractor, experienceFalseTxt.setArgs(InvoText.literal(""+ReviveMeConfig.minReviveXPLevel)
                                            .withStyle(true, InvoTextFormat.filter(ChatFormatting.GREEN, ChatFormatting.BOLD)).getText())
                                    .withStyle(true, InvoTextFormat.filter(ChatFormatting.RED)).getText(), true, 0,
                            mainZone.inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);
                }
                break;
            }
            case NONE: {
                shouldPass = false;
                chosenTxt = chanceTxt;
                ClientUtil.blit2DItem(GuiGraphicsExtractor, mainZone.copy().setWidth(Math.min(mainZone.width(), mainZone.height()))
                        .setHeight(Math.min(mainZone.width(), mainZone.height())).center(mainZone), Items.BARRIER.asItem().getDefaultInstance());
                break;

            }
        }

        if (!shouldPass) ClientUtil.blit2DColor(GuiGraphicsExtractor, progressZone, blackFadeColor);

        if (beingHeld) {
            int progressColor = shouldPass ? new Color(117, 243, 54, 216).getRGB() : new Color(243, 60, 54, 216).getRGB();
            float fillPercent = MathUtil.lerp((FallenPlayerActionsEvent.timeHeld / 40F), 0, progressZone.height());
            ClientUtil.blit2DColor(GuiGraphicsExtractor, progressZone.copy().setHeight(fillPercent).mirrorY(progressZone.middleY()), progressColor);
        }

        if (chosenTxt != null) {
            InputConstants.Key key = VanillaKeybindHandler.getKey(mouseButton == 0 ?
                    KeyInit.leftOption.keyBind : KeyInit.rightOption.keyBind);
            if (shouldPass){
                chosenTxt = chosenTxt.setArgs(InvoText.literal(key.
                        getDisplayName().getString()).withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.YELLOW)).getText());
            }
            else {
                if (ReviveMeConfig.canGiveUp) {
                    chosenTxt = selfDestructTxt1;
                    chosenTxt = chosenTxt.setArgs(InvoText.literal(key.
                                    getDisplayName().getString()).withStyle(true, InvoTextFormat.filter( ChatFormatting.BOLD, ChatFormatting.YELLOW)).getText(),
                            selfDestructTxt2.getText());
                }
                else {
                    chosenTxt = cantGiveUp.deepCopy();
                }
            }
            TextUtil.render2DText(GuiGraphicsExtractor, chosenTxt.withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD)).getText(), true,3,
                    headerZone.inflate(-2,-3), TextUtil.txtAlignment.MIDDLE);
        }
    }
    public static void renderTimer(GuiGraphicsExtractor GuiGraphicsExtractor, FallenData cap, InvoZone workZone, float y, float progressCircleRadius, int imageSize){
        //Where the timer will be placed.
        float progressCircleMultiplier = (70/36f);
        InvoZone timerZone = timerIMG.getRenderZone().copy().setWidth(imageSize).setHeight(imageSize).setY(y).centerX(workZone.middleX());
        InvoZone timerZoneAdjusted = timerZone.copy().inflate(timerZone.width()/-4f, timerZone.height()/-4f).setDown(timerZone.down());

        timerZone = ReviveMathUtil.zoneLerp(FallenItemScreenEvent.getTogglePercentage(), timerZone, timerZoneAdjusted);
        progressCircleRadius = MathUtil.lerp(FallenItemScreenEvent.getTogglePercentage(), progressCircleRadius, progressCircleRadius/2F);
        //Increase seconds by 1 if seconds isn't at 0
        float seconds = cap.getTimeLeft(false);
        seconds += (seconds <= 0 ? 0 : 1);
        InvoText timeLeftTxt = InvoText.literal(Integer.toString((int) seconds));
        if (ReviveMeConfig.timeLeft == -1) timeLeftTxt = InvoText.literal("INF");
        else if (seconds <= 0) timeLeftTxt = InvoText.literal("RIP");

        timeLeftTxt.withStyle(true, InvoTextFormat.filter(ChatFormatting.RED, ChatFormatting.BOLD));

        //This is the timer background
        timerZone.shiftXY(0, -timerZone.height()/2);

        //This is overkill
        CircleRender.draw2DArc(GuiGraphicsExtractor, timerZone.middleX(), timerZone.middleY(), (progressCircleRadius + 4) * progressCircleMultiplier,
                0, Math.max(0.001D,cap.getOverkillPercentage() * 360), purpleProgCircle);

        //This is overheal
        CircleRender.draw2DArc(GuiGraphicsExtractor, timerZone.middleX(), timerZone.middleY(), (progressCircleRadius + 2) * progressCircleMultiplier,
                0, Math.max(0.001D,cap.getOverhealPercentage() * 360), goldProgCircle);
//        GuiGraphicsExtractor.guiRenderState.forEachElement(state -> LOGGER.error(state.getClass().getName()), GuiRenderState.TraverseRange.ALL);

        //green color: 2616150
        seconds = cap.getTimeLeft(true);
        float endAngle = seconds <= 0 ? 360 : seconds * 360;
//        LOGGER.error("ZoneX:"+(timerZone.middleX() - 90)+", ZoneY:"+(timerZone.middleY()-90)+
//                ", W:"+(90*2)+", H:"+(90*2));
//        ClientUtil.blit2DColor(GuiGraphicsExtractor, InvoZone.fromPoint(timerZone.middle()).inflate(45), Color.BLACK.getRGB());
        CircleRender.draw2DArc(GuiGraphicsExtractor, timerZone.middleX(), timerZone.middleY(), progressCircleRadius * progressCircleMultiplier, 0, endAngle, greenColor);
//        ClientUtil.getMinecraft().renderBuffers().bufferSource().endBatch();

        InvoZone originalZone = timerIMG.getRenderZone().copy();
        timerIMG.getRenderZone().copy(timerZone);
        timerIMG.render(GuiGraphicsExtractor);
        timerIMG.getRenderZone().copy(originalZone);

        TextUtil.render2DText(GuiGraphicsExtractor, timeLeftTxt.getText(), false, 1,
                timerZone.inflate(-imageSize/4F, -imageSize/4F), TextUtil.txtAlignment.MIDDLE);
    }

    public static double getPartialTicks(){
        return ClientUtil.getMinecraft().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }
    public static FallenData getCap() {
        FallenData cap = FallenData.get(ClientUtil.getMinecraft().player);
        if (!cap.isFallen()) return null;
        if (cap.getOtherPlayer() != null) return null;

        return cap;
    }
}


