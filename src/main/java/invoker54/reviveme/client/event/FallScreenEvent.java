package invoker54.reviveme.client.event;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.MathUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.OverlayRegistry;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static invoker54.invocore.client.util.ClientUtil.getPlayer;
import static invoker54.invocore.client.util.ClientUtil.mC;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FallScreenEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallScreenEvent.class, ReviveMeConfig.debugMode);

    private static final Minecraft inst = Minecraft.getInstance();

    public static final ResourceLocation Timer_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID,"textures/screens/timer_background.png");
    public static final ResourceLocation HEALTH_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID,"textures/revive_types/heart.png");
    public static final ResourceLocation FOOD_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID,"textures/revive_types/hunger.png");
    public static final ResourceLocation EXPERIENCE_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID,"textures/revive_types/experience_bottle.png");
    public static final ResourceLocation MOUSE_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID, "textures/mouse_icons.png");
    public static final ResourceLocation REVIVE_HELP_BUTTON_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID, "textures/revive_help_button.png");

    //Images
    public static ClientUtil.Image timerIMG = new ClientUtil.Image(Timer_TEXTURE, 0, 64, 0, 64, 64);
    public static ClientUtil.Image heartIMG = new ClientUtil.Image(HEALTH_TEXTURE, 0, 8, 0, 8, 8);
    public static ClientUtil.Image xpIMG = new ClientUtil.Image(EXPERIENCE_TEXTURE, 0, 16, 0, 16, 16);
    public static ClientUtil.Image foodIMG = new ClientUtil.Image(FOOD_TEXTURE, 0, 18, 0, 18,18);
    public static ClientUtil.Image mouse_idle_IMG = new ClientUtil.Image(MOUSE_TEXTURE, 0, 22, 0, 28,64);
    public static ClientUtil.Image mouse_left_IMG = new ClientUtil.Image(MOUSE_TEXTURE, 0, 22, 28, 28,64);
    public static ClientUtil.Image mouse_right_IMG = new ClientUtil.Image(MOUSE_TEXTURE, 22, 22, 28, 28,64);
    public static ClientUtil.Image revive_help_button_IMG = new ClientUtil.Image(REVIVE_HELP_BUTTON_TEXTURE, 0, 31, 0, 31,32);

    private static final InvoText titleText = InvoText.translate("fallenScreen.fallen_text");
    private static final InvoText reviveCountMultipleText = InvoText.translate("fallenScreen.revive_count_multiple");
    private static final InvoText reviveCountSingleText = InvoText.translate("fallenScreen.revive_count_single");
    private static final InvoText waitText = InvoText.translate("fallenScreen.wait_text");
    private static final InvoText forceDeathText = InvoText.translate("fallenScreen.force_death_text");
    private static final InvoText cantForceDeathText = InvoText.translate("fallenScreen.cant_force_death_text");
    private static final DecimalFormat df = new DecimalFormat("0.0");
    private static final int greenColor = new Color(39, 235, 86, 255).getRGB();
    private static final int whiteColor = new Color(255, 255, 255, 255).getRGB();
    private static final int blackFadeColor = new Color(0, 0, 0, 71).getRGB();
    private static final int redFadeColor = 1615855616;

    //Translation text for self revive
    private static final String fallenDirectory = "revive-me.fallenScreen.self_revive.";
    private static final InvoText chanceTxt = InvoText.translate(fallenDirectory+"chance_1");
    private static final InvoText randomItemFalseTxt = InvoText.translate(fallenDirectory+"random_items.false_1");
    private static final InvoText randomItemTxt = InvoText.translate(fallenDirectory+"random_items_1");
    private static final InvoText specificItemFalseTxt = InvoText.translate(fallenDirectory+"specific_item.false_1");
    private static final InvoText specificItemTxt = InvoText.translate(fallenDirectory+"specific_item_1");
    private static final InvoText killTxt1 = InvoText.translate(fallenDirectory+"kill_1");
    private static final InvoText killTxt2 = InvoText.translate(fallenDirectory+"kill_2");
    private static final InvoText killTxt3 = InvoText.translate(fallenDirectory+"kill_3");
    private static final InvoText killTxt4 = InvoText.translate(fallenDirectory+"kill_4");
    private static final InvoText statusEffectTxt = InvoText.translate(fallenDirectory+"status_effects_1");
    private static final InvoText experienceFalseTxt = InvoText.translate(fallenDirectory+"experience.false_1");
    private static final InvoText experienceTxt = InvoText.translate(fallenDirectory+"experience_1");

    public static void registerFallenScreen(){
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CHAT_PANEL_ELEMENT, "revive_button", (gui, stack, partialTicks, width, height) ->{
            FallenCapability cap = getCap();
            if (cap == null) return;
            if ((mC.screen instanceof ChatScreen)) return;

            InvoZone workZone = new InvoZone(0, width, 0, height);

            InvoText message = InvoText.literal("[" + KeyInit.callForHelpKey.keyBind.getKey().getDisplayName().getString()
                    + "]").withStyle(true,ChatFormatting.BOLD);
            if (cap.callForHelpCooldown() < 1) message.withStyle(true,ChatFormatting.BLACK);

            InvoZone reviveButtonZone = revive_help_button_IMG.getRenderZone();
            reviveButtonZone.setDown(workZone.down() - 16).setRight(workZone.right() - 16);
            ClientUtil.blitColor(stack, reviveButtonZone.copy().setY(reviveButtonZone.down()).
                    setHeight((int) (reviveButtonZone.height() * cap.callForHelpCooldown())).setBound(reviveButtonZone,true), whiteColor);

            revive_help_button_IMG.render(stack);
            InvoZone txtZone = reviveButtonZone.copy().splitHeight(4,1);
            txtZone.centerY(reviveButtonZone.y() + ((reviveButtonZone.height()/4)*3));
            TextUtil.renderText(stack, message.getText(), false, 1, txtZone, TextUtil.txtAlignment.MIDDLE);

            if (cap.callForHelpCooldown() != 1){
                ClientUtil.blitColor(stack, reviveButtonZone, blackFadeColor);
            }
        });
        //if (true) return;
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CHAT_PANEL_ELEMENT, "fallen_screen", (gui, stack, partialTicks, width, height) ->
        {
            //region initial checks
            FallenCapability cap = getCap();
            if (cap == null) return;
            if ((mC.screen instanceof ChatScreen)) return;
            if (cap.canSelfRevive()) return;
            //endregion

            InvoZone workZone = new InvoZone(0, width, 0, height);

            ClientUtil.blitColor(stack, workZone, redFadeColor);

            //Title text
            InvoZone titleTextZone = workZone.copy().setWidth(workZone.width()/3).setHeight(workZone.height()/5).inflate(0,2)
                    .centerX(workZone.middleX());
            TextUtil.renderText(stack, titleText.getText(), true, 1, titleTextZone, TextUtil.txtAlignment.MIDDLE);

            //Wait For text
            InvoZone waitTextZone  = workZone.copy().setWidth(workZone.width()/3).setHeight(8).setY((workZone.height()/4) + 12)
                    .centerX(workZone.middleX());
            TextUtil.renderText(stack, waitText.getText(), true, 1, waitTextZone, TextUtil.txtAlignment.MIDDLE);

            //Force death text
            InvoText forceDeathTextResult = ReviveMeConfig.canGiveUp ? forceDeathText : cantForceDeathText;

            forceDeathTextResult = forceDeathTextResult.setArgs(
                    InvoText.literal(VanillaKeybindHandler.getKey(inst.options.keyAttack).getDisplayName().getString()).getText(),
                    df.format(2 - (FallenPlayerActionsEvent.timeHeld / 20f)));
            InvoZone forceDeathTextZone = waitTextZone.copy().setY(waitTextZone.down() + 17).setWidth(workZone.width()).centerX(waitTextZone.middleX());
            TextUtil.renderText(stack, forceDeathTextResult.getText(), true, 1, forceDeathTextZone, TextUtil.txtAlignment.MIDDLE);

            renderTimer(stack, cap, workZone, workZone.down() - (workZone.down()/3), 36, 64);
        });
        OverlayRegistry.registerOverlayAbove(ForgeIngameGui.CHAT_PANEL_ELEMENT, "fallen_self_revive_screen", (gui, stack, partialTicks, width, height) ->
        {
            FallenCapability cap = getCap();
            if (cap == null) return;
            if ((mC.screen instanceof ChatScreen)) return;
            if (!cap.canSelfRevive()) return;

            InvoZone workZone = new InvoZone(0, width, 0, height);

            InvoZone leftZone;
            InvoZone rightZone;

            ClientUtil.blitColor(stack, workZone, redFadeColor);

            if(!ReviveMeConfig.compactReviveUI) {
                float timerMiddleY = workZone.down() - (workZone.down()/3);
                leftZone = workZone.copy().splitWidth(4,1);
                leftZone.splitHeight(5,3);
                leftZone.center(workZone.copy().setWidth(workZone.width() / 2));
                renderTimer(stack, cap, workZone, timerMiddleY, 36, 64);
            }
            else {
                float timerMiddleY = workZone.down() - (workZone.down()/4);
                leftZone = workZone.copy().splitWidth(5,1);
                leftZone.splitHeight(3,1);
                leftZone.centerX(workZone.copy().splitWidth(2,1).middleX());
                leftZone.setDown(timerMiddleY + 18);
                renderTimer(stack, cap, workZone, timerMiddleY, 18, 32);
            }
            rightZone = leftZone.copy().mirrorX(workZone.middleX());

            //Title text
            InvoZone titleTextZone = workZone.copy().setWidth(workZone.width()/3).setHeight(workZone.height()/5).inflate(0,-12)
                    .centerX(workZone.middleX());
            TextUtil.renderText(stack, titleText.getText(), true, 1, titleTextZone, TextUtil.txtAlignment.MIDDLE);

            int revivesLeft = ReviveMeConfig.maxSelfRevives - cap.getSelfReviveCount();
            if (revivesLeft > 0) {
                InvoZone reviveTextZone = titleTextZone.copy().setWidthConstraint(titleTextZone.width() * 0.7F).centerX(titleTextZone.middleX())
                        .setY(titleTextZone.down()).splitHeight(3,2);
                InvoText chosenText = revivesLeft == 1 ? reviveCountSingleText : reviveCountMultipleText;
                chosenText = chosenText.setArgs(InvoText.literal(revivesLeft + "")
                        .withStyle(true,(revivesLeft == 1 ? ChatFormatting.RED : ChatFormatting.YELLOW), ChatFormatting.BOLD).getText());
                ClientUtil.blitColor(stack, reviveTextZone, new Color(0,0,0,150).getRGB());
                TextUtil.renderText(stack, chosenText.getText(), true, 1,
                        reviveTextZone.inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);
            }



            double timeHeldPercentage = FallenPlayerActionsEvent.timeHeld/40D;
            float sizeChange = MathUtil.lerp(MathUtil.EaseType.EASEOUTQUAD.getEase(timeHeldPercentage), 0, 10);

            timeHeldPercentage = MathUtil.EaseType.EASEOUTQUAD.getEase(Math.min(1, timeHeldPercentage*2));

            if (VanillaKeybindHandler.attackHeld){
                rightZone.setX(MathUtil.lerp(timeHeldPercentage, rightZone.x(), workZone.right()));
                rightZone.inflate(-sizeChange, -sizeChange);
                leftZone.inflate(sizeChange, sizeChange);
            }
            else if (VanillaKeybindHandler.useHeld){
                leftZone.setX(MathUtil.lerp(timeHeldPercentage, leftZone.x(), 0 - rightZone.width()));
                leftZone.inflate(-sizeChange, -sizeChange);
                rightZone.inflate(sizeChange, sizeChange);
            }

            renderReviveOption(GLFW.GLFW_MOUSE_BUTTON_1, stack, leftZone, cap, VanillaKeybindHandler.attackHeld);
            renderReviveOption(GLFW.GLFW_MOUSE_BUTTON_2, stack, rightZone, cap, VanillaKeybindHandler.useHeld);
        });


        OverlayRegistry.registerOverlayBottom("chat_fallen_timer_screen", (gui, stack, partialTicks, width, height) ->
        {
            FallenCapability cap = getCap();
            if (cap == null) return;
            if (!(mC.screen instanceof ChatScreen)) return;

            InvoZone workZone = new InvoZone(0, width, 0, height);

            renderTimer(stack, cap, workZone, workZone.down() - (workZone.down()/3), 36, 64);
        });
    }

    public static void renderReviveOption(int mouseButton, PoseStack stack, InvoZone workZone, FallenCapability cap, boolean beingHeld) {
        ClientUtil.blitColor(stack, workZone, blackFadeColor);
        InvoZone headerZone = workZone.copy().splitHeight(4, 1);
        InvoZone mainZone = workZone.copy().setY(headerZone.down()).setHeight(workZone.height()-headerZone.height());
        InvoZone greenZone = mainZone.copy();
        InvoText chosenTxt = null;
        FallenCapability.SELFREVIVETYPE selfReviveType = cap.getSelfReviveOption(mouseButton);
        ClientUtil.blitColor(stack, mainZone, new Color(0, 0, 0, 128).getRGB());

        switch (selfReviveType) {
            case CHANCE: {
                chosenTxt = chanceTxt;
                int reviveChance = (int) (100*Float.parseFloat(df.format(Math.max(0, ReviveMeConfig.reviveChance*(1 - cap.getSelfPenaltyPercentage())))));
                InvoText chanceNumberTxt =
                        InvoText.literal(reviveChance+"%")
                                .withStyle(true,ChatFormatting.BOLD, (reviveChance <= 0 ? ChatFormatting.RED : ChatFormatting.GOLD));

                TextUtil.renderText(stack, chanceNumberTxt.getText(), true, 1, mainZone.copy().inflate(-4,0), TextUtil.txtAlignment.MIDDLE);
                break;
            }
            case RANDOM_ITEMS: {
                chosenTxt = randomItemTxt;
                float randomItemPadding = 2;

                ArrayList<ItemStack> itemArrayList = cap.getItemList();
                if (itemArrayList.isEmpty()){
                    TextUtil.renderText(stack, randomItemFalseTxt.withStyle(true,ChatFormatting.RED).getText(),
                            true, 0, mainZone.inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);
                    break;
                }

                //Remove the padding space
                mainZone.setHeight(mainZone.height() - (randomItemPadding * 3));
                mainZone.splitHeight(4, 1);
                float randomItemSize = mainZone.height() - (randomItemPadding * 2);

                for (ItemStack sacrificeStack : itemArrayList) {
                    //Draw the background
                    ClientUtil.blitColor(stack, mainZone, new Color(0, 0, 0, 255).getRGB());

                    InvoZone randomItemFullZone = mainZone.copy().inflate(-randomItemPadding, -randomItemPadding);
                    InvoZone randomItemImageZone = randomItemFullZone.copy().setWidth(randomItemSize);
                    InvoZone randomItemTxtZone = randomItemFullZone.copy().setWidth(randomItemFullZone.width() - randomItemImageZone.width())
                            .setX(randomItemImageZone.right()).splitWidth(3, 1);

                    //Draw the item
                    ClientUtil.blitItem(stack, randomItemImageZone, sacrificeStack);

                    //Draw the amount they have, then the amount they will have after reduction
                    int count = FallenCapability.countItem(inst.player.getInventory(), sacrificeStack);
                    TextUtil.renderText(stack, InvoText.literal("" + count).withStyle(true,ChatFormatting.BOLD, ChatFormatting.GREEN).getText(),
                            true, 1, randomItemTxtZone, TextUtil.txtAlignment.MIDDLE);
                    randomItemTxtZone.shift(randomItemTxtZone.width(), 0);

                    TextUtil.renderText(stack, InvoText.literal(" -> ").withStyle(true,ChatFormatting.BOLD).getText(),
                            true, 1, randomItemTxtZone, TextUtil.txtAlignment.MIDDLE);
                    randomItemTxtZone.shift(randomItemTxtZone.width(), 0);

                    TextUtil.renderText(stack, InvoText.literal("" + (count - (Math.round(Math.max(1,
                                            count * ReviveMeConfig.sacrificialItemPercent*(1+cap.getSelfPenaltyPercentage()))))))
                                    .withStyle(true,ChatFormatting.BOLD, ChatFormatting.RED).getText(),
                            true, 1, randomItemTxtZone, TextUtil.txtAlignment.MIDDLE);

                    mainZone.shift(0, mainZone.height() + randomItemPadding);
                }
                break;
            }
            case SPECIFIC_ITEM: {
                chosenTxt = specificItemTxt;
                Pair<Integer, List<ItemStack>> itemPair = cap.getSpecificItem(getPlayer());
                InvoZone itemZone = mainZone.copy().splitHeight(3,2);
                itemZone.setHeight(Math.min(itemZone.height(), itemZone.width())).setWidth(Math.min(itemZone.width(), itemZone.height()));
                itemZone.centerX(mainZone.middleX());
                InvoZone countZone = itemZone.copy().setY(itemZone.down()).setHeight(mainZone.down()-itemZone.down())
                        .setWidth(mainZone.width()).setX(mainZone.x());

                InvoText badText = InvoText.translate("revive-me.fallenScreen.self_revive.specific_item.false_1")
                        .withStyle(true,ChatFormatting.RED, ChatFormatting.BOLD);

                if (itemPair.getKey() >= ReviveMeConfig.specificItemCount) {
                    Inventory inventory = getPlayer().getInventory();
                    int itemCount = 0;
                    ItemStack specificStack = itemPair.getRight().get(0);
                    for (int a = 0; a < inventory.getContainerSize(); a++) {
                        ItemStack containerStack = inventory.getItem(a);
                        if (!containerStack.sameItem(specificStack)) continue;
                        if (!ItemStack.tagMatches(specificStack, containerStack)) continue;
                        itemCount += containerStack.getCount();
                    }

                    ClientUtil.blitColor(stack, mainZone, new Color(0, 0, 0, 100).getRGB());
                    ClientUtil.blitItem(stack, itemZone, specificStack);
                    ClientUtil.blitColor(stack, countZone, new Color(0, 0, 0, 100).getRGB());
                    countZone.splitWidth(3,1);
                    TextUtil.renderText(stack, InvoText.literal(itemCount+"").withStyle(true,ChatFormatting.GREEN).getText(),
                            true, 1, countZone.copy().inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);
                    TextUtil.renderText(stack, InvoText.literal("->").getText(),
                            true, 1, countZone.setX(countZone.right()), TextUtil.txtAlignment.MIDDLE);
                    TextUtil.renderText(stack, InvoText.literal((itemCount-itemPair.getKey())+"").withStyle(true,ChatFormatting.RED).getText(),
                            true, 1, countZone.setX(countZone.right()).copy().inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);
                }
                else {
//                    ClientUtil.blitItem(stack, itemZone, itemPair.getRight().get(0));
//                    ClientUtil.blitColor(stack, mainZone, new Color(0,0,0,230).getRGB());
                    mainZone.splitHeight(2,1);
                    InvoText itemCountTxt = InvoText.literal(""+Math.abs(itemPair.getKey() - ReviveMeConfig.specificItemCount)).withStyle(true,ChatFormatting.GREEN);
                    TextUtil.renderText(stack, badText.setArgs(itemCountTxt.getText()).getText(),true, 0,
                            mainZone.copy().inflate(-4,-2), TextUtil.txtAlignment.MIDDLE);

                    mainZone.setY(mainZone.down());
                    mainZone.splitHeight(2,1);

                    ClientUtil.blitItem(stack, mainZone.copy().setWidth(mainZone.height()).centerX(mainZone.middleX()), itemPair.getRight().get(0));
                    InvoText itemNameTxt = InvoText.component((MutableComponent) itemPair.getRight().get(0)
                            .getDisplayName()).withStyle(false, ChatFormatting.BOLD);
                    TextUtil.renderText(stack, itemNameTxt.getText(), true, 1, mainZone.setY(mainZone.down())
                            .inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);
                }
                break;
            }
            case KILL: {
                chosenTxt = killTxt1;
                int seconds = (int) ((ReviveMeConfig.reviveKillTime * 20 * (1 - cap.getSelfPenaltyPercentage()))/20);
                mainZone.splitHeight(4, 1);
                mainZone.setY(mainZone.middleY());

                TextUtil.renderText(stack, killTxt2.withStyle(true,ChatFormatting.GOLD).setArgs(
                                InvoText.literal(""+ReviveMeConfig.reviveKillAmount)
                                        .withStyle(true,ChatFormatting.RED).getText()).getText(),
                        true, 1, mainZone.copy()
                                .inflate(-3,-3), TextUtil.txtAlignment.MIDDLE);

                TextUtil.renderText(stack, killTxt3.withStyle(true,ChatFormatting.GOLD, ChatFormatting.BOLD).getText(),
                        true, 1, mainZone.shift(0, mainZone.height()).copy()
                                .inflate(-3,-3), TextUtil.txtAlignment.MIDDLE);

                TextUtil.renderText(stack, killTxt4.withStyle(true,ChatFormatting.GOLD).setArgs(InvoText.literal(""+seconds)
                                .withStyle(true,ChatFormatting.RED).getText()).getText(),
                        true, 1, mainZone.shift(0, mainZone.height()).copy()
                                .inflate(-3,-3), TextUtil.txtAlignment.MIDDLE);

                break;
            }
            case STATUS_EFFECTS: {
                chosenTxt = statusEffectTxt;
                MobEffectTextureManager potionspriteuploader = mC.getMobEffectTextures();

                ClientUtil.Image backgroundImg = new ClientUtil.Image(ContainerScreen.INVENTORY_LOCATION, 0,120,166,31,256);
                InvoZone backgroundZone = backgroundImg.getRenderZone();

                backgroundZone.setY(headerZone.down()).setWidthConstraint(mainZone.width()-2);

                mainZone.setHeight(mainZone.height()/cap.getNegativeStatusEffects().size());
                for (MobEffect effect : cap.getNegativeStatusEffects()){
                    TextureAtlasSprite sprite = potionspriteuploader.get(effect);
                    ClientUtil.Image effectIMG = new ClientUtil.Image(sprite.atlas().location(), sprite.getU0(),
                            (sprite.getU1()-sprite.getU0()),  sprite.getV0(),
                            (sprite.getV1()-sprite.getV0()), 1);
                    InvoZone effectZone = effectIMG.getRenderZone();

                    backgroundZone.center(mainZone);
                    backgroundImg.render(stack);
                    effectZone.copy(backgroundZone);

                    //Divide the background zone width by 4 and that will be the effect img zone
                    effectZone.splitWidth(4,1).inflate(-4,-4);
                    effectIMG.render(stack);

                    InvoZone textZone = backgroundZone.copy().splitWidth(4,1);
                    textZone.setX(textZone.right()).setWidth(textZone.width()*3).inflate(-2,-4)
                            .splitHeight(2,1);

                    int amp = (cap.getNegativeStatusEffects().size() > 1 ? 0 : 1);
                    int duration = (int) (20 * ReviveMeConfig.negativeEffectsTime * (1 + cap.getSelfPenaltyPercentage()));
                    MobEffectInstance instance = new MobEffectInstance(effect, duration, amp);

                    String s = I18n.get(effect.getDescriptionId());
                    if (instance.getAmplifier() >= 1 && instance.getAmplifier() <= 9) {
                        s = s + ' ' + I18n.get("enchantment.level." + (instance.getAmplifier() + 1));
                    }
                    String s1 = MobEffectUtil.formatDuration(instance, 1.0F);

                    TextUtil.renderText(stack, InvoText.literal(s).getText(), true, 1, textZone,
                            TextUtil.txtAlignment.LEFT);
                    textZone.setY(textZone.down()+2);
                    TextUtil.renderText(stack, InvoText.literal(s1).withStyle(true,ChatFormatting.DARK_GRAY).getText(),
                            true, 1, textZone, TextUtil.txtAlignment.LEFT);

                    mainZone.setY(mainZone.down());
                }
                break;
            }
            case EXPERIENCE: {
                chosenTxt = experienceTxt;
                int newLevel =  (inst.player.experienceLevel - (int)(inst.player.experienceLevel * ReviveMeConfig.reviveXPLossPercentage
                        * (1 + cap.getSelfPenaltyPercentage())));

                InvoZone itemZone = mainZone.copy().splitHeight(4, 3);
                itemZone.inflate(Math.min((itemZone.height()-itemZone.width())/2, 0), Math.min((itemZone.width()-itemZone.height())/2,0));

                ClientUtil.blitItem(stack, itemZone.inflate(-5,-5),
                        new ItemStack(Items.EXPERIENCE_BOTTLE));

                if (ReviveMeConfig.minReviveXPLevel <= inst.player.experienceLevel) {
                    mainZone.splitWidth(3, 1).splitHeight(4, 1).shift(0, mainZone.height() * 3);
                    TextUtil.renderText(stack, InvoText.literal(inst.player.experienceLevel + "").withStyle(true,ChatFormatting.GREEN, ChatFormatting.BOLD)
                            .getText(), true, 1, mainZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);

                    mainZone.shift(mainZone.width(), 0);
                    TextUtil.renderText(stack, InvoText.literal("->")
                            .getText(), true, 1, mainZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);

                    mainZone.shift(mainZone.width(), 0);
                    TextUtil.renderText(stack, InvoText.literal("" + newLevel).withStyle(true,ChatFormatting.RED, ChatFormatting.BOLD)
                            .getText(), true, 1, mainZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);
                }
                else {
                    RenderSystem.defaultBlendFunc();
                    ClientUtil.blitColor(stack, mainZone, new Color(0,0,0,230).getRGB());
                    TextUtil.renderText(stack, experienceFalseTxt.setArgs(InvoText.literal(""+ReviveMeConfig.minReviveXPLevel)
                                            .withStyle(true,ChatFormatting.GREEN, ChatFormatting.BOLD).getText())
                                    .withStyle(true,ChatFormatting.RED).getText(), true, 0,
                            mainZone.inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);
                }
                break;
            }
        }

        if (beingHeld) {
            float fillPercent = MathUtil.lerp((FallenPlayerActionsEvent.timeHeld / 40F), 0, greenZone.height());
            ClientUtil.blitColor(stack, greenZone.copy().setHeight(fillPercent).mirrorY(greenZone.middleY()),
                    new Color(117, 243, 54, 216).getRGB());
        }

        if (chosenTxt != null) {
            InputConstants.Key key = VanillaKeybindHandler.getKey(mouseButton == 0 ?
                    inst.options.keyAttack : inst.options.keyUse);
            chosenTxt = chosenTxt.setArgs(InvoText.literal(key.getDisplayName().getString()).withStyle(true,ChatFormatting.BOLD, ChatFormatting.YELLOW).getText());
            TextUtil.renderText(stack, chosenTxt.withStyle(true,ChatFormatting.BOLD).getText(), true,3,
                    headerZone.inflate(-2,-3), TextUtil.txtAlignment.MIDDLE);
        }
    }
    public static void renderTimer(PoseStack stack, FallenCapability cap, InvoZone workZone, float y, int progressCircleRadius, int imageSize){
        //Where the timer will be placed.
        InvoZone timerZone = timerIMG.getRenderZone().setWidth(imageSize).setHeight(imageSize).setY(y).centerX(workZone.middleX());

        //Increase seconds by 1 if seconds isn't at 0
        float seconds = cap.GetTimeLeft(false);
        seconds += (seconds <= 0 ? 0 : 1);

        InvoText timeLeftTxt = InvoText.literal((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                .withStyle(true,ChatFormatting.RED, ChatFormatting.BOLD);

        //This is the timer background
        timerZone.shift(0, -timerZone.height()/2);

        //green color: 2616150
        seconds = cap.GetTimeLeft(true);
        float endAngle = seconds <= 0 ? 360 : seconds * 360;
        CircleRender.drawArc(stack, timerZone.middleX(),
                timerZone.middleY(), progressCircleRadius, 0, endAngle, greenColor);

        timerIMG.render(stack);

        TextUtil.renderText(stack, timeLeftTxt.getText(), false, 1,
                timerZone.inflate(-imageSize/4F, -imageSize/4F), TextUtil.txtAlignment.MIDDLE);
    }

    public static FallenCapability getCap(){
        FallenCapability cap = FallenCapability.GetFallCap(inst.player);
        if (!cap.isFallen()) return null;
        if (cap.getOtherPlayer() != null) return null;

        return cap;
    }
}


