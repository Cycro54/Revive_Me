package invoker54.reviveme.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.invocore.common.util.MathUtil;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.common.InvoTextFormat;
import invoker54.reviveme.common.ReviveMathUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static invoker54.invocore.client.util.ClientUtil.mC;

public class FallenItemScreenEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(FallenItemScreenEvent.class, ReviveMeConfig.debugMode);

    private static final String itemLangDirectory = "revive-me.fallenScreen.items.";
    private static final InvoText changePageText = InvoText.translate(itemLangDirectory +"change_page");
    private static final InvoText useItemText = InvoText.translate(itemLangDirectory +"use_item");
    private static final InvoText changeItemText = InvoText.translate(itemLangDirectory +"change_item");
    private static final InvoText giveUpDescriptionText = InvoText.translate(itemLangDirectory +"give_up_description");
    private static final InvoText noRefreshDescriptionText = InvoText.translate(itemLangDirectory +"no_refresh_description");
    private static final InvoText cantGiveUpDescriptionText = InvoText.translate(itemLangDirectory +"cant_give_up_description");
    private static final InvoText giveUpText = InvoText.translate(itemLangDirectory +"give_up");
    private static final InvoText notEnoughText = InvoText.translate(itemLangDirectory +"not_enough");
    private static final int blackFadeColor = new Color(0, 0, 0, 130).getRGB();

    //I need a transition variables
    //Bool for if it's item or regular fallen screen
    //A start double and end double for the transition animation
    //I need to know the transition type

    //I need everything that's in the fallen screen too
    public static boolean isItemScreenActive = false;
    public static double startTransition = 0;
    public static double endTransition = 1;
    public static double transitionTicks = 8;

    public static MathUtil.EaseType easeType = MathUtil.EaseType.EASEOUTQUAD;
    public static double moveAmount = 0;
    public static double previousTick = 0;
    public static double passedTicks = 0;

    public static InvoZone prevLeftZone = new InvoZone(0,0,0,0);

    public static DecimalFormat df = new DecimalFormat("0.0");

    //Right side item animation
    public static int selectedItem = 0;
    public static int selectedPage = 0;
    public static List<BiConsumer<PoseStack, InvoZone>> pageList = new ArrayList<>();
    public static List<Pair<ItemStack, ReviveItemData>> dataStackList = new ArrayList<>();

    public static void renderItemPage(PoseStack stack, double switchPercentage, InvoZone workZone, InvoZone leftZone, InvoZone rightZone){
        passedTicks = (mC.player.level.getGameTime() + FallScreenEvent.getPartialTicks()) - previousTick;

        if (mC.level.getGameTime() % 10 == 0) refreshItemData();

        leftZone.setRight(MathUtil.lerp(1 - switchPercentage, leftZone.right(), workZone.x()));
        rightZone.setX(MathUtil.lerp(1 - switchPercentage, rightZone.x(), workZone.right()));

        if ((prevLeftZone.width() * prevLeftZone.height()) != (leftZone.width() * leftZone.height())) pageList.clear();
        prevLeftZone = leftZone;

        //Start with left
        if (pageList.isEmpty()) generatePages(dataStackList.get(selectedItem).getRight(), leftZone);
        if (ReviveMeConfig.showItemPage) {
            InvoZone topLeftZone = leftZone.copy().splitHeight(5, 1);
            ClientUtil.blitColor(stack, leftZone, blackFadeColor);
            ClientUtil.blitColor(stack, topLeftZone.copy().shift(0, topLeftZone.height()).splitHeight(1, 4), blackFadeColor);
            InvoText changeText = changePageText.deepCopy().setArgs(InvoText.component(VanillaKeybindHandler.getKey(KeyInit.leftOption.keyBind).getDisplayName().copy())
                    .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.YELLOW)).getText());

            TextUtil.renderText(stack, changeText.getText(), true, 2,
                    topLeftZone.inflate(-1, -1f), TextUtil.txtAlignment.MIDDLE);

            InvoZone numberZone = topLeftZone.copy().setHeight(topLeftZone.height() / 2).setY(topLeftZone.down() + 1);
            numberZone.setWidth(numberZone.height() * 2).centerX(leftZone.middleX());
            ClientUtil.blitColor(stack, numberZone, blackFadeColor);

            InvoText pageText = InvoText.literal((selectedPage + 1) + "/" + pageList.size());
            TextUtil.renderText(stack, pageText.getText(), true, 1,
                    numberZone.inflate(-1, -1f), TextUtil.txtAlignment.MIDDLE);


            InvoZone bodyZone = leftZone.copy().setY(numberZone.down()).setHeight(leftZone.height() - (topLeftZone.height() + numberZone.height()));

            pageList.get(selectedPage).accept(stack, bodyZone.copy().inflate(-1, -4));
        }
//        topLeftZone.setHeight(Math.min(topLeftZone.height(), 12));
//        topLeftZone.mirrorY(leftZone.middleY());
//        ClientUtil.blitColor(stack, topLeftZone, new Color(1,1,1, 190).getRGB());
//        TextUtil.renderText(stack, InvoText.literal((selectedPage+1) + "/" + pageList.size()).getText(), false, 2,
//                topLeftZone.copy().inflate(-1, -0.5F), TextUtil.txtAlignment.MIDDLE);

        //region right side
        //Now with right
        ClientUtil.blitColor(stack, rightZone, blackFadeColor);
        InvoZone textZone = rightZone.copy().splitHeight(5,1);
        InvoZone middleZone = textZone.copy().setY(textZone.down()).splitHeight(1, 4);

        //Top

        InvoText itemText = useItemText.deepCopy().setArgs(InvoText.translate(KeyInit.rightOption.keyBind.getKey().getName())
                .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.YELLOW)).getText());
        ClientUtil.blitColor(stack, middleZone, blackFadeColor);
        TextUtil.renderText(stack, itemText.getText(), true, 2, textZone.copy()
                .inflate(-1, -1F), TextUtil.txtAlignment.MIDDLE);


        //Middle
        ClientUtil.beginCrop(middleZone.x(), middleZone.width(), middleZone.y(), middleZone.height(), true);
//        ClientUtil.blitColor(stack, rightZone, Color.red.getRGB());
        renderItems(stack, middleZone);
        ClientUtil.endCrop();

        //Bottom
//        textZone.mirrorY(rightZone.middleY());
//        ClientUtil.blitColor(stack, textZone, blackFadeColor);
//        TextUtil.renderText(stack, changeItemText.getText(), false, 2, textZone.copy()
//                .inflate(-1, -0.5F), TextUtil.txtAlignment.MIDDLE);

        //endregion

        previousTick = mC.player.level.getGameTime() + FallScreenEvent.getPartialTicks();
    }

    public static void refreshItemData(){
        dataStackList.clear();
        dataStackList.addAll(FallenCapability.get(ClientUtil.getPlayer()).getReviveItemList(false));
        dataStackList.add(Pair.of(new ItemStack(Items.BARRIER), null));
        changeSelectedItem(0);
    }
    public static void changeSelectedItem(int scrollAmount) {
        scrollAmount = -scrollAmount;
        pageList.clear();
        int maxIndex = (dataStackList.size() - 1);
        int resultAmount = (scrollAmount + selectedItem);

        if (scrollAmount == 0) resultAmount = Math.min(maxIndex, resultAmount);
        resultAmount = (int) Math.round(ReviveMathUtil.clampLoop(resultAmount, 0, maxIndex));
        selectedItem = resultAmount;
        moveAmount += scrollAmount;

        if (scrollAmount != 0) FallenPlayerActionsEvent.timeHeld = 0;
    }

    public static Pair<ItemStack, ReviveItemData> getSelectedPair(){
        return dataStackList.get(selectedItem);
    }

    public static void generatePages(ReviveItemData data, InvoZone leftZone){
        InvoZone topZone = leftZone.copy().splitHeight(5,1);
        InvoZone bodyZone = leftZone.copy().splitHeight(5,4).setY(topZone.down());

        if (data != null) {
            if (!data.getDescription().getString().isEmpty()) pageList.addAll(getDescriptionPages(data, bodyZone));
            pageList.addAll(getPropertyPages(data));
            pageList.addAll(getEffectPages(data, bodyZone));
        }
        else {
            pageList.addAll(getDescriptionPages(null, bodyZone));
        }

        selectedPage = (int) ReviveMathUtil.clampLoop(selectedPage, 0, pageList.size()-1);
    }

    public static void renderItems(PoseStack stack, InvoZone bodyZone){
        int maxCount = (int) Math.floor(bodyZone.height()/20);
        if (maxCount % 2 == 0) maxCount--;
        maxCount = Math.max(3, maxCount);
        int middleCount = (int) Math.ceil(maxCount/2f);

        InvoZone itemZone = bodyZone.copy().splitHeight(maxCount,1);
        //Moves zone to middle
        itemZone.shift(0, itemZone.height() * (middleCount - 1));

        //Now I need to figure out which way to go
        int multiplier = 1;
        if (moveAmount != 0) multiplier = (int) -(Math.abs(moveAmount)/moveAmount);

        //Get the count I have to render
        int count = (int) (maxCount + (Math.ceil(Math.abs(moveAmount))));
        //The starting index
        int index = (int) ReviveMathUtil.clampLoop(selectedItem - (multiplier * ( middleCount - 1)), 0, dataStackList.size()-1);
        //Now move starting zone to the first spot
        itemZone.shift(0, (float) (itemZone.height() * moveAmount));
        //Move the opposite way of the multiplier
        itemZone.shift(0, itemZone.height() * -multiplier * (middleCount-1));
        for (int a = 0; a < count; a++){
            renderItem(itemZone.copy(), dataStackList.get(index), stack, a == (middleCount-1));
            itemZone.shift(0, itemZone.height() * multiplier);
            index = (int) ReviveMathUtil.clampLoop(index + multiplier, 0, dataStackList.size()-1);
        }

        double passedSeconds = passedTicks/20d;
        double speedPerSecond = Math.max(8.0 * Math.abs(moveAmount), 0.5F);
        double changeAmount = Math.min(Math.abs(moveAmount), (passedSeconds * speedPerSecond) * multiplier);
        moveAmount = moveAmount + changeAmount;

    }

    public static void renderItem(InvoZone myZone, Pair<ItemStack, ReviveItemData> pair, PoseStack stack, boolean isMain){
        myZone.inflate(-2,-2);

        InvoZone nameZone = myZone.copy().setWidth(myZone.width() - myZone.height());
        InvoZone itemZone = nameZone.copy().setX(nameZone.right()).setWidth(nameZone.height());
        InvoZone countZone = itemZone.copy().splitHeight(5,2).splitWidth(5,2).setDown(itemZone.down());
        ReviveItemData data = pair.getRight();
        ItemStack chosenStack = pair.getKey();
        int currentCount = data == null ? 0 : Math.min(pair.getKey().getCount(), data.getItemCount(ClientUtil.getPlayer(), chosenStack));
        int countNeeded = data == null ? 0 : data.getCountRequired() - currentCount;
        boolean hasEnough = data == null || currentCount >= data.getCountRequired();

        ClientUtil.blitColor(stack, itemZone, blackFadeColor);
        if (!hasEnough){
            ClientUtil.blitColor(stack, itemZone.inflate(-1,-1),
                    new Color(223, 17, 17, 176).getRGB());
        }
        ClientUtil.blitItem(stack, itemZone.inflate(-1,-1), chosenStack);

        if (data != null) {
            int endCount = currentCount - data.getCountRequired();

            ClientUtil.blitColor(stack, countZone, Color.black.getRGB());
            TextUtil.renderText(stack, InvoText.literal(String.valueOf(currentCount)).withStyle(false, ChatFormatting.GREEN).getText(), false, 1,
                    countZone.copy().inflate(-0.2f, -0.2f), TextUtil.txtAlignment.MIDDLE);

            ClientUtil.blitColor(stack, countZone.setRight(itemZone.right()), Color.black.getRGB());
            TextUtil.renderText(stack, InvoText.literal(String.valueOf(endCount)).withStyle(false, ChatFormatting.RED).getText(), false, 1,
                    countZone.copy().inflate(-0.2f, -0.2f), TextUtil.txtAlignment.MIDDLE);
        }

        nameZone.inflate(-2, -((nameZone.height()/2)/2));

        if (isMain){
            ClientUtil.blitColor(stack, nameZone.copy().inflate(1,1),
                    new Color(74, 218, 64, 181).getRGB());
        }
        ClientUtil.blitColor(stack, nameZone, blackFadeColor);

        if (isMain && VanillaKeybindHandler.useHeld) {
            double reviveSeconds = (data == null ? 2 : data.getReviveSeconds());
            double revivePercent = ReviveMathUtil.percentageLerp(FallenPlayerActionsEvent.timeHeld, 0, reviveSeconds * 20);
            revivePercent = Math.min(1,revivePercent);
            double colorWidth = (nameZone.width()*revivePercent);
            InvoText nameText;

            if (hasEnough) {
                ClientUtil.blitColor(stack, nameZone.copy().setWidth((float) colorWidth)
                        .inflate(-1, -1), new Color(74, 218, 64, 181).getRGB());
                nameText = InvoText.literal(df.format(reviveSeconds - (reviveSeconds * revivePercent)) + "");
            }
            else {
                ClientUtil.blitColor(stack, nameZone.copy()
                        .inflate(-1, -1), new Color(108, 1, 1, 181).getRGB());
                nameText = notEnoughText.deepCopy().setArgs(InvoText.literal(""+countNeeded).withStyle(false, ChatFormatting.GOLD).getText());
            }

            TextUtil.renderText(stack, nameText.getText(), false, 0,
                    nameZone.copy().inflate(-1, -0.5f), TextUtil.txtAlignment.RIGHT);

        }
        else {
            InvoText nameText = InvoText.component(pair.getKey().getDisplayName().copy());
            if (!pair.getKey().hasCustomHoverName() && data != null && !data.getDisplayName().getString().isEmpty()) nameText = data.getDisplayName();
            if (data == null){
                if (ReviveMeConfig.canGiveUp) nameText = giveUpText.deepCopy();
                else nameText = FallScreenEvent.cantGiveUp.deepCopy();
            }

            TextUtil.renderText(stack, nameText.getText(), false, 0,
                    nameZone.copy().inflate(-1, -0.5f), TextUtil.txtAlignment.RIGHT);
        }
    }

    public static List<BiConsumer<PoseStack, InvoZone>> getDescriptionPages(ReviveItemData data, InvoZone textZone){
        List<BiConsumer<PoseStack, InvoZone>> consumerList = new ArrayList<>();

        int cutOffPoint = (int) (textZone.width() * (textZone.height()/8));
        InvoText description = ReviveMeConfig.canGiveUp ? giveUpDescriptionText : cantGiveUpDescriptionText;
        if (!ReviveMeConfig.refreshItems) description = description.deepCopy().append(noRefreshDescriptionText);
        if (data != null) description = data.getDescription();

        int textWidth = mC.font.width(description.getText());
        cutOffPoint = Math.max(cutOffPoint, textWidth/4);
        
        List<MutableComponent> textList = new ArrayList<>();
        new TextUtil.CharacterManagerMixer().splitLines(description.getText(), cutOffPoint,
                Style.EMPTY, (A, B) ->{
                textList.add(InvoText.literal(A.getString()).getText());
                });

        if (textList.size() > 4){
            textList.get(textList.size()-2).append(textList.get(textList.size()-1));
            textList.remove(textList.size()-1);
        }

        for (MutableComponent comp : textList){
            consumerList.add((stack, bodyZone) -> {
                int spacePerLine = (int) Math.ceil(bodyZone.width() * 1D);
                int heightPerLine = 10;
                int lineCount = (int) Math.ceil(mC.font.width(comp)/(float)spacePerLine);
                float maxHeight = lineCount * heightPerLine;
                maxHeight = Math.min(maxHeight + (bodyZone.height()/4), bodyZone.height());
                bodyZone.setHeight(maxHeight);

//                ClientUtil.blitColor(stack, bodyZone, blackFadeColor);
                TextUtil.renderText(stack, comp, true, 0,
                        bodyZone.copy().inflate(-1,-1f), TextUtil.txtAlignment.MIDDLE);

            });
        }

        return consumerList;
    }

    public static List<BiConsumer<PoseStack, InvoZone>> getPropertyPages(ReviveItemData data){
        List<BiConsumer<PoseStack, InvoZone>> consumerList = new ArrayList<>();

        consumerList.add((stack, bodyZone) -> {

            List<InvoText> textList = new ArrayList<>();
            //Health
            textList.add(ReviveToolTipEvents.reviveHealthText.withStyle(true, ChatFormatting.AQUA)
                    .setArgs(formatNumber(data.getRevivedHealth(), true, data.getRevivedHealth() < 1).getText()));
            //Food
            textList.add(ReviveToolTipEvents.reviveFoodText.withStyle(true, ChatFormatting.AQUA)
                    .setArgs(formatNumber(data.getRevivedFood(), true, data.getRevivedFood() < 1).getText()));
            //Count required
            textList.add(ReviveToolTipEvents.countText.withStyle(true, ChatFormatting.AQUA)
                    .setArgs(formatNumber(data.getCountRequired(), true, false).getText()));
            //Revive Time
            textList.add(ReviveToolTipEvents.reviveSecondsText.withStyle(true, ChatFormatting.AQUA)
                    .setArgs(formatNumber(data.getReviveSeconds(), true, false).getText()));

            if (data.getReviveChance() != 1) {
                //Revive Chance
                textList.add(ReviveToolTipEvents.reviveChanceText.withStyle(true, ChatFormatting.AQUA)
                        .setArgs(formatNumber(data.getReviveChance(), true, true).getText()));

                //Fallen Timer Change
                textList.add(ReviveToolTipEvents.fallenTimerChangeText.withStyle(true, ChatFormatting.AQUA)
                        .setArgs(formatNumber(data.getFallenTimerChange(), false, false).getText()));

                //Refresh Options
                textList.add(ReviveToolTipEvents.refreshOptionsText.withStyle(true, ChatFormatting.AQUA)
                        .setArgs(formatBoolean(data.isRefreshOptions()).getText()));
            }

            TextUtil.renderText(stack, textList.stream()
                    .map(InvoText::getText).collect(Collectors.toList()), true,
                    bodyZone.inflate(-2,-1), TextUtil.txtAlignment.LEFT);
        });

        return consumerList;
    }

    public static List<BiConsumer<PoseStack, InvoZone>> getEffectPages(ReviveItemData data, InvoZone initialZone){
        List<BiConsumer<PoseStack, InvoZone>> consumerList = new ArrayList<>();

        MobEffectTextureManager potionspriteuploader = mC.getMobEffectTextures();
        ClientUtil.Image backgroundImg = new ClientUtil.Image(ContainerScreen.INVENTORY_LOCATION, 0,120,166,31,256);
        InvoZone backgroundZone = backgroundImg.getRenderZone();

        List<MobEffectInstance> effectList = data.getReviveEffects();
        InvoZone subBodyZone = initialZone.copy().splitHeight(6,5).setDown(initialZone.down());
        int maxHeight = (int) backgroundZone.setWidthConstraint(subBodyZone.width()).height();
        int maxCountPerPage = (int) Math.ceil(subBodyZone.height()/maxHeight);
        int maxPages = (int) Math.ceil(effectList.size()/(float)maxCountPerPage);

        for (int a = 0; a < maxPages; a++) {
            int pageCount = a;
            consumerList.add((stack, bodyZone) -> {
                InvoZone subZone = bodyZone.copy().splitHeight(maxCountPerPage, 1);
                backgroundZone.setWidthConstraint(subZone.width()).setHeightConstraint
                        (Math.min(backgroundZone.height(),maxHeight)).center(subZone).setY(bodyZone.y());
                for (int b = 0; b < (maxCountPerPage - 1); b++) {
                    int index = ((pageCount*maxCountPerPage)+b);
                    if (index == effectList.size()) break;
                    MobEffectInstance effectInstance = effectList.get(index);

                    TextureAtlasSprite sprite = potionspriteuploader.get(effectInstance.getEffect());
                    ClientUtil.Image effectIMG = new ClientUtil.Image(sprite.atlas().location(), sprite.getU0(),
                            (sprite.getU1()-sprite.getU0()),  sprite.getV0(),
                            (sprite.getV1()-sprite.getV0()), 1);
                    InvoZone effectZone = effectIMG.getRenderZone();

                    backgroundImg.render(stack);
                    effectZone.copy(backgroundZone);

                    //Divide the background zone width by 4 and that will be the effect img zone
                    effectZone.splitWidth(4,1).inflate(-4,-4);
                    effectIMG.render(stack);

                    InvoZone textZone = backgroundZone.copy().splitWidth(4,1);
                    textZone.setX(textZone.right()).setWidth(textZone.width()*3).inflate(-2,-4)
                            .splitHeight(2,1);

                    int amp = effectInstance.getAmplifier();

                    String s = I18n.get(effectInstance.getDescriptionId());
                    if (amp >= 1 && amp <= 9) {
                        s = s + ' ' + I18n.get("enchantment.level." + (amp + 1));
                    }
                    String s1 = MobEffectUtil.formatDuration(effectInstance, 1.0F);

                    TextUtil.renderText(stack, InvoText.literal(s).getText(), true, 1, textZone,
                            TextUtil.txtAlignment.LEFT);
                    textZone.setY(textZone.down()+2);
                    TextUtil.renderText(stack, InvoText.literal(s1).withStyle(true, InvoTextFormat.filter(ChatFormatting.DARK_GRAY)).getText(),
                            true, 1, textZone, TextUtil.txtAlignment.LEFT);

                    backgroundZone.shift(0, backgroundZone.height() + 2);
                }
            });
        }

        return consumerList;
    }

    public static InvoText formatNumber(double number, boolean isAbsolute, boolean isPercentage){
        if (isAbsolute) number = Math.abs(number);
        if (isPercentage) number = number * 100;
        String s = String.valueOf(number);
        if (number % 1 == 0) s = String.valueOf((long) number);

        InvoText customText = InvoText.literal(s);
        customText.withStyle(false, ChatFormatting.LIGHT_PURPLE);
        if (isPercentage) customText.append(InvoText.literal("%").withStyle(false, ChatFormatting.WHITE));
        return customText;
    }

    public static InvoText formatString(String s){
        return InvoText.literal(s).withStyle(false, ChatFormatting.GREEN);
    }

    public static InvoText formatBoolean(boolean bool){
        InvoText customText = InvoText.literal(String.valueOf(bool));
        if (bool) customText.withStyle(false, ChatFormatting.GREEN);
        else customText.withStyle(false, ChatFormatting.RED);
        return customText;
    }

    public static void switchReviveScreens(){
        if (isItemScreenActive && ReviveMeConfig.selfReviveOptions.isEmpty()) return;
        if (!isItemScreenActive && ReviveItemData.reviveItemMap.isEmpty()) return;

        isItemScreenActive = !isItemScreenActive;
        startTransition = mC.level.getGameTime();
        endTransition = startTransition + transitionTicks;

        VanillaKeybindHandler.attackHeld = false;
        VanillaKeybindHandler.useHeld = false;

        if (isItemScreenActive) refreshItemData();
    }

    public static double getSwitchPercentage() {
        double range = endTransition - startTransition;
        double percentage = ((mC.level.getGameTime() + FallScreenEvent.getPartialTicks()) - startTransition) / range;
        percentage = Math.max(0, Math.min(1, percentage));
        return easeType.getEase(percentage);
    }
}
