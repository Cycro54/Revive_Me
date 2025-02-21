package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static invoker54.invocore.client.ClientUtil.mC;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class FallScreenEvent {
    private static final Logger LOGGER = LogManager.getLogger();

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


    private static final net.minecraft.util.text.TextComponent titleText = new TranslationTextComponent("fallenScreen.fallen_text");
    private static final net.minecraft.util.text.TextComponent waitText = new TranslationTextComponent("fallenScreen.wait_text");
    private static final net.minecraft.util.text.TextComponent forceDeathText = new TranslationTextComponent("fallenScreen.force_death_text");
    private static final net.minecraft.util.text.TextComponent cantForceDeathText = new TranslationTextComponent("fallenScreen.cant_force_death_text");
    private static final DecimalFormat df = new DecimalFormat("0.0");
    private static final int greenColor = new Color(39, 235, 86, 255).getRGB();
    private static final int whiteColor = new Color(255, 255, 255, 255).getRGB();
    private static final int blackFadeColor = new Color(0, 0, 0, 71).getRGB();

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void renderReviveButton(RenderGameOverlayEvent.Post event){
        if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
        if (mC.screen instanceof ChatScreen) return;
        FallenCapability cap = FallenCapability.GetFallCap(inst.player);
        if (!cap.isFallen()) return;

        MatrixStack stack = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        IFormattableTextComponent message = new StringTextComponent("[").append(KeyInit.callForHelpKey.keyBind.getKey().getDisplayName())
                .append(new StringTextComponent("]")).withStyle(TextFormatting.BOLD);
        if (cap.callForHelpCooldown() < 1) message.withStyle(TextFormatting.BLACK);

        revive_help_button_IMG.moveTo(width - (revive_help_button_IMG.getWidth() + 16), height - (revive_help_button_IMG.getHeight() + 16));
        ClientUtil.blitColor(stack, revive_help_button_IMG.x0 + 1, revive_help_button_IMG.getWidth()-2,
                revive_help_button_IMG.y0 + 1 + (30 - (30 * (float)cap.callForHelpCooldown())),  30 * (float)cap.callForHelpCooldown(), whiteColor);

        revive_help_button_IMG.RenderImage(stack);
        TextUtil.renderText(stack, message, 1, false, revive_help_button_IMG.x0 + 3, revive_help_button_IMG.getWidth() - 6,
                revive_help_button_IMG.y0 + 19, 8, 0, TextUtil.txtAlignment.MIDDLE);

        if (cap.callForHelpCooldown() != 1){
            ClientUtil.blitColor(stack, revive_help_button_IMG.x0, revive_help_button_IMG.getWidth(),
                    revive_help_button_IMG.y0, revive_help_button_IMG.getHeight(), blackFadeColor);
        }
    }

    @SubscribeEvent
    public static void renderFallenScreen(RenderGameOverlayEvent.Post event){
        if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
        if (mC.screen instanceof ChatScreen) return;
        FallenCapability cap = FallenCapability.GetFallCap(inst.player);
        if (!cap.isFallen()) return;

        if (cap.getOtherPlayer() != null) return;

        if (ReviveMeConfig.selfReviveMultiplayer && (!cap.usedChance() || !cap.getItemList().isEmpty())) return;
        //If self revive multiplayer is true, I don't want this to show if we have not used chance, and we have sacrificial items

        if ((mC.hasSingleplayerServer() && mC.getSingleplayerServer().getPlayerList().getPlayers().size() == 1)) return;
        MatrixStack stack = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        int startTextHeight = (height / 5);
        FontRenderer font = mC.font;

        AbstractGui.fill(stack, 0, 0, width, height, 1615855616);

        //Title text
        stack.pushPose();
        stack.scale(2, 2, 2);
        AbstractGui.drawCenteredString(stack, font, titleText, (width / 2) / 2, (startTextHeight - 10) / 2, 16777215);
        stack.popPose();

        //Wait For text
        AbstractGui.drawCenteredString(stack, font, waitText, width / 2, (int) (startTextHeight * 1.5f), 16777215);
        //Force death text
        String editText = ReviveMeConfig.canGiveUp ? forceDeathText.getString() : cantForceDeathText.getString();
        editText = editText.replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString());
        editText = editText.replace("{seconds}", df.format(2 - (FallenPlayerActionsEvent.timeHeld / 20f)));
        AbstractGui.drawCenteredString(stack, font, editText, width / 2, (startTextHeight * 2), 16777215);

        //Where the timer will be placed.
        float x = (width / 2F);
        float y = height - (height / 3F);
        float seconds = cap.GetTimeLeft(true);

        //System.out.println(seconds);

        //green color: 2616150
        float endAngle = seconds <= 0 ? 360 : seconds * 360;
        float radius = 36;
        CircleRender.drawArc(stack, x, y, radius, 0, endAngle, greenColor);

        //Increase seconds by 1 if seconds isn't at 0
        seconds = cap.GetTimeLeft(false);
        seconds += (seconds <= 0 ? 0 : 1);

        IFormattableTextComponent timeLeftString =
                new StringTextComponent((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                        .withStyle(TextFormatting.RED, TextFormatting.BOLD);

        //This is the timer background
        timerIMG.resetScale();
        timerIMG.setActualSize(64, 64);
        timerIMG.moveTo(0, 0);
        timerIMG.centerImageX(0, width);
        timerIMG.centerImageY((int) (y - radius), (int) (radius * 2));
        timerIMG.RenderImage(stack);

        TextUtil.renderText(stack, timeLeftString, false, timerIMG.x0 + 17, 30, timerIMG.y0 + 17, 30,
                0, TextUtil.txtAlignment.MIDDLE);
    }

    @SubscribeEvent
    public static void renderSelfReviveScreen(RenderGameOverlayEvent.Post event){
        if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
        if (mC.screen instanceof ChatScreen) return;
        if (ReviveMeConfig.compactReviveUI) return;
        if (!ReviveMeConfig.selfReviveMultiplayer &&
                (
                        (mC.hasSingleplayerServer() && mC.getSingleplayerServer().getPlayerList().getPlayers().size() > 1) ||
                                !mC.isLocalServer()
                )
        ) return;

        FallenCapability cap = FallenCapability.GetFallCap(inst.player);
        if (!cap.isFallen()) return;
        if (cap.getOtherPlayer() != null) return;
        if (cap.usedChance() && cap.getItemList().isEmpty()) return;

        MatrixStack stack = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        int fifthHeight = (height / 5);
        float halfWidth = (width / 2F);
        float thirdHeight = height - (height / 3F);
        FontRenderer font = mC.font;

        AbstractGui.fill(stack, 0, 0, width, height, 1615855616);

        //Title text
        stack.pushPose();
        stack.scale(2, 2, 2);
        AbstractGui.drawCenteredString(stack, font, titleText, (width / 2) / 2, ((fifthHeight - 18)/2)/2, 16777215);
        stack.popPose();

        //Other revive items top
        float startHeight = fifthHeight + ((fifthHeight - 9)/2F);
        IFormattableTextComponent stringToRender;

        //region Left mouse to give chance
        if (!cap.usedChance()) {
            //Top portion
            ClientUtil.blitColor(stack, halfWidth/4F, (halfWidth/2F), startHeight,
                    20, new Color(0,0,0, 182).getRGB());
            stringToRender = new TranslationTextComponent("fallenScreen.self_revive.give_chance_1");
            stringToRender = new StringTextComponent(stringToRender.getString().replace("{attack}",inst.options.keyAttack.getKey().getDisplayName().getString()));
            TextUtil.renderText(stack, stringToRender.withStyle(TextFormatting.BOLD), true, halfWidth / 4, (halfWidth / 2F),
                    startHeight, 20, 2, TextUtil.txtAlignment.MIDDLE);

            //Middle potion
            ClientUtil.blitColor(stack, halfWidth / 4F, (halfWidth / 2F), startHeight + 40,
                    (halfWidth / 2F) - 60, new Color(0, 0, 0, 255).getRGB());
            stringToRender = new StringTextComponent(Math.round(ReviveMeConfig.reviveChance * 100) + "%");
            TextUtil.renderText(stack, stringToRender.withStyle(TextFormatting.BOLD).withStyle(TextFormatting.GOLD),
                    true, halfWidth / 4, (halfWidth / 2F), startHeight,
                    (halfWidth / 2F) + 20, 2, TextUtil.txtAlignment.MIDDLE);

            //Bottom portion
            ClientUtil.blitColor(stack, halfWidth / 4F, (halfWidth / 2F), startHeight + (halfWidth / 2F),
                    20, new Color(0, 0, 0, 182).getRGB());
            stringToRender = new TranslationTextComponent("fallenScreen.self_revive.give_chance_2");
            TextUtil.renderText(stack, stringToRender.withStyle(TextFormatting.BOLD), true, halfWidth / 4, (halfWidth / 2F),
                    startHeight + (halfWidth / 2F), 20, 2, TextUtil.txtAlignment.MIDDLE);
        }
        //endregion

        //Right mouse to give items
        ClientUtil.blitColor(stack, (halfWidth + halfWidth/4), (halfWidth/2F),
                startHeight, 20, new Color(0,0,0, 182).getRGB());
        stringToRender = new TranslationTextComponent("fallenScreen.self_revive.give_items");
        stringToRender = new StringTextComponent(stringToRender.getString().replace("{use}",inst.options.keyUse.getKey().getDisplayName().getString()));
        TextUtil.renderText(stack, stringToRender.withStyle(TextFormatting.BOLD), true, (halfWidth + halfWidth/4), (halfWidth/2F),
                startHeight, 20, 1, TextUtil.txtAlignment.MIDDLE);

        //region this will be for sacrificial items
        float offset = 0;
        int padding = 2;
        int itemSize = 16;

        if (cap.getItemList().size() != 0){
            ArrayList<Item> itemArrayList = cap.getItemList();
            for (Item item : itemArrayList) {
                //Draw the background
                ClientUtil.blitColor(stack, (halfWidth / 4) + halfWidth, halfWidth / 2, (fifthHeight * 2) + offset,
                        itemSize + (padding * 2), new Color(0, 0, 0, 255).getRGB());

                //Draw the item
                mC.getItemRenderer().renderAndDecorateItem(new ItemStack(item),
                        (int) ((halfWidth / 4) + halfWidth + padding), (int) ((fifthHeight * 2) + offset + padding));

                //Draw the amount they have, then the amount they will have after reduction
                int count = inst.player.inventory.countItem(item);
                IFormattableTextComponent countComp = new StringTextComponent("" + count).withStyle(TextFormatting.BOLD).withStyle(TextFormatting.GREEN);
                IFormattableTextComponent arrowComp = new StringTextComponent(" -> ").withStyle(TextFormatting.BOLD);
                IFormattableTextComponent newCountComp =
                        new StringTextComponent("" + (count - (Math.round(Math.max(1, ReviveMeConfig.sacrificialItemPercent * count)))))
                                .withStyle(TextFormatting.BOLD).withStyle(TextFormatting.RED);

                float startX = (halfWidth / 4) + halfWidth + (padding + itemSize + padding);
                float spacePortion = ((halfWidth / 2) - (itemSize + (padding * 3)))/3F;
                float startY = (fifthHeight * 2) + offset + padding;
                TextUtil.renderText(stack, countComp, true, (int) startX, spacePortion, startY, itemSize, 1, TextUtil.txtAlignment.MIDDLE);
                TextUtil.renderText(stack, arrowComp, true, (int) startX + spacePortion, spacePortion, startY, itemSize, 1, TextUtil.txtAlignment.MIDDLE);
                TextUtil.renderText(stack, newCountComp, true, (int) startX + (spacePortion*2), spacePortion, startY, itemSize, 1, TextUtil.txtAlignment.MIDDLE);

                offset += itemSize + (padding * 3);
            }
        }
        //endregion

        //region This will be if the player is holding a button

        //I have to render the mouse later on so that it isn't affected by blackout
        ClientUtil.Image chosenMouse = mouse_idle_IMG;

        //This will be chance
        if (VanillaKeybindHandler.attackHeld) {
            ClientUtil.blitColor(stack, halfWidth, halfWidth, fifthHeight, height,
                    new Color(0,0,0, Math.min((int) (255 * (FallenPlayerActionsEvent.timeHeld/40F)), 255)).getRGB());

            chosenMouse = mouse_left_IMG;
        }

        //This will use items
        else if (VanillaKeybindHandler.useHeld) {
            ClientUtil.blitColor(stack, 0, halfWidth, fifthHeight, height,
                    new Color(0,0,0, Math.min((int) (255 * (FallenPlayerActionsEvent.timeHeld/40F)), 255)).getRGB());

            chosenMouse = mouse_right_IMG;
        }
        //endregion

        //Divider Lines in the middle
        ClientUtil.blitColor(stack, halfWidth - 1, 2, fifthHeight, height, new Color(0, 0, 0,255).getRGB());
        ClientUtil.blitColor(stack, 0, width, fifthHeight, 2, new Color(0, 0, 0,255).getRGB());



        //region this will black out the options you've already used

        //This is for item sacrifice
        if (cap.usedSacrificedItems() || cap.getItemList().size() == 0){
            ClientUtil.blitColor(stack, halfWidth, halfWidth, fifthHeight, height, new Color(0,0,0, 204).getRGB());

            IFormattableTextComponent item_text = new TranslationTextComponent(cap.usedSacrificedItems() ? "fallenScreen.self_revive.used" : "fallenScreen.self_revive.no_items");
            item_text = item_text.withStyle(TextFormatting.DARK_RED);

            TextUtil.renderText(stack, item_text, true,
                    halfWidth, halfWidth, startHeight,
                    (halfWidth/2F) + 20,  (int) (halfWidth/4F), TextUtil.txtAlignment.MIDDLE);
        }
        //This is for chance
        if (cap.usedChance()){
            ClientUtil.blitColor(stack, 0, halfWidth, fifthHeight, height, new Color(0,0,0, 204).getRGB());
            TextUtil.renderText(stack, new TranslationTextComponent("fallenScreen.self_revive.used").withStyle(TextFormatting.DARK_RED), true,
                    0, (halfWidth), startHeight,
                    (halfWidth/2F) + 20,  (int) (halfWidth/4F), TextUtil.txtAlignment.MIDDLE);
        }
        //endregion

        //This will render the mouse
        chosenMouse.resetScale();
        chosenMouse.setActualSize(chosenMouse.getWidth() * 2, chosenMouse.getHeight() * 2);
        chosenMouse.centerImageX(0, width);
        chosenMouse.moveTo(chosenMouse.x0,fifthHeight);
        chosenMouse.RenderImage(stack);

        //System.out.println(seconds);



        //region The timer stuff
        float seconds = cap.GetTimeLeft(true);
        //green color: 2616150
        float endAngle = seconds <= 0 ? 360 : seconds * 360;
        float radius = 36;
        CircleRender.drawArc(stack, halfWidth, thirdHeight, radius, 0, endAngle, greenColor);

        //Increase seconds by 1 if seconds isn't at 0
        seconds = cap.GetTimeLeft(false);
        seconds += (seconds <= 0 ? 0 : 1);

        IFormattableTextComponent timeLeftString =
                new StringTextComponent((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                        .withStyle(TextFormatting.RED, TextFormatting.BOLD);

        //This is the timer background
        timerIMG.resetScale();
        timerIMG.setActualSize(64, 64);
        timerIMG.moveTo(0, 0);
        timerIMG.centerImageX(0, width);
        timerIMG.centerImageY((int) (thirdHeight - radius), (int) (radius * 2));
        timerIMG.RenderImage(stack);

        TextUtil.renderText(stack, timeLeftString, false,timerIMG.x0 + 17, 30, timerIMG.y0 + 17, 30,
                0, TextUtil.txtAlignment.MIDDLE);
        //endregion

    }

    @SubscribeEvent
    public static void renderCompactSelfReviveScreen(RenderGameOverlayEvent.Post event){
        if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
        if (mC.screen instanceof ChatScreen) return;
        if (!ReviveMeConfig.compactReviveUI) return;
        if (!ReviveMeConfig.selfReviveMultiplayer &&
                (
                        (mC.hasSingleplayerServer() && mC.getSingleplayerServer().getPlayerList().getPlayers().size() > 1) ||
                                !mC.isLocalServer()
                )
        ) return;

        FallenCapability cap = FallenCapability.GetFallCap(inst.player);
        if (!cap.isFallen()) return;
        if (cap.getOtherPlayer() != null) return;
        if (cap.usedChance() && cap.getItemList().isEmpty()) return;

        MatrixStack stack = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        float halfWidth = (width / 2F);
        float thirdHeight = (height / 3F);
        //This is for the green circle that is around the Timer img
        float radius = (height/5F)/2;
        //This is the top of the timer picture
        int timerOrigY = (int) (thirdHeight + (radius/2)) + 2;
        int mouseOrigY = (int) (timerOrigY + (timerIMG.getHeight()/2F) + radius) + 2;
        FontRenderer font = mC.font;

        AbstractGui.fill(stack, 0, 0, width, height, 1615855616);

        //Title text
        stack.pushPose();
        stack.scale(2, 2, 2);
        AbstractGui.drawCenteredString(stack, font, titleText, (width / 2) / 2, (int) (timerOrigY - radius)/2, 16777215);
        stack.popPose();

        //Other revive items top
        float startHeight = mouseOrigY + 2;
        //How much room the text has width
        float txtRoomWidth = (width/2.25F)/2F;
        //How much room half of the mouse takes
        float halfMouseSizeX = (mouse_idle_IMG.getWidth()/2F);
        //Text room height
        float txtRoomHeight = (int)(mouse_idle_IMG.getHeight() * 1.25f);
        IFormattableTextComponent stringToRender;

        //Background
        ClientUtil.blitColor(stack, halfWidth - txtRoomWidth, (width/2.25F), mouse_idle_IMG.y0,
                txtRoomHeight, new Color(0,0,0, 182).getRGB());

        //region This will be if the player is holding a button

        //This will be chance
        if (VanillaKeybindHandler.attackHeld) {
            ClientUtil.blitColor(stack, halfWidth - txtRoomWidth - 1, txtRoomWidth + 1, mouse_idle_IMG.y0 - 1,
                    txtRoomHeight + 2, new Color(255, 255, 255, 255).getRGB());
        }

        //This will be for items
        else if (VanillaKeybindHandler.useHeld) {
            ClientUtil.blitColor(stack, halfWidth, txtRoomWidth + 1, mouse_idle_IMG.y0 - 1,
                    txtRoomHeight + 2, new Color(255, 255, 255, 255).getRGB());
        }
        //endregion

        //region Left mouse to give chance
        if (!cap.usedChance()) {
            //background
            ClientUtil.blitColor(stack, halfWidth - txtRoomWidth, txtRoomWidth - halfMouseSizeX - 5,
                    mouse_idle_IMG.y0, txtRoomHeight, new Color(0,0,0, 255).getRGB());

            //Top portion
            stringToRender = new TranslationTextComponent("fallenScreen.self_revive.give_chance_1");
            stringToRender = new StringTextComponent(stringToRender.getString().replace("{attack}",inst.options.keyAttack.getKey().getDisplayName().getString()));
            TextUtil.renderText(stack, stringToRender.withStyle(TextFormatting.BOLD), 2, true, halfWidth - txtRoomWidth, txtRoomWidth - halfMouseSizeX - 5,
                    mouse_idle_IMG.y0, (txtRoomHeight/4), 2, TextUtil.txtAlignment.MIDDLE);

            //Middle potion
//                ClientUtil.blitColor(stack, halfWidth - txtRoomWidth + 2, txtRoomWidth - halfMouseSizeX - 4, mouse_idle_IMG.y0 + (txtRoomHeight/4),
//                        (txtRoomHeight/2), new Color(0,0,0, 255).getRGB());
            stringToRender = new StringTextComponent(Math.round(ReviveMeConfig.reviveChance * 100) + "%");
            TextUtil.renderText(stack, stringToRender.withStyle(TextFormatting.BOLD).withStyle(TextFormatting.GOLD),
                    true, halfWidth - txtRoomWidth, txtRoomWidth - halfMouseSizeX - 5,
                    mouse_idle_IMG.y0 + (txtRoomHeight/4), (txtRoomHeight/2), 2, TextUtil.txtAlignment.MIDDLE);

            //Bottom portion
            stringToRender = new TranslationTextComponent("fallenScreen.self_revive.give_chance_2");
            TextUtil.renderText(stack, stringToRender.withStyle(TextFormatting.BOLD), 2, true, halfWidth - txtRoomWidth, txtRoomWidth - halfMouseSizeX - 5,
                    mouse_idle_IMG.y0 + ((txtRoomHeight/4) * 3), (txtRoomHeight/4), 2, TextUtil.txtAlignment.MIDDLE);
        }
        //endregion

        //Right mouse to give items
        stringToRender = new TranslationTextComponent("fallenScreen.self_revive.give_items");
        stringToRender = new StringTextComponent(stringToRender.getString().replace("{use}",inst.options.keyUse.getKey().getDisplayName().getString()));
        if (!cap.usedSacrificedItems() && cap.getItemList().size() != 0) {
            ClientUtil.blitColor(stack, halfWidth + halfMouseSizeX + 5, txtRoomWidth - halfMouseSizeX - 5,
                    mouse_idle_IMG.y0, (txtRoomHeight / 4), new Color(0, 0, 0, 255).getRGB());
            TextUtil.renderText(stack, stringToRender.withStyle(TextFormatting.BOLD), 2, true, halfWidth + halfMouseSizeX + 5,
                    txtRoomWidth - halfMouseSizeX - 5, mouse_idle_IMG.y0, (txtRoomHeight / 4), 1, TextUtil.txtAlignment.MIDDLE);
        }

        //region this will be for sacrificial items
        float offset = 0;
        int padding = 0;
        float itemSize = ((((txtRoomHeight/4) * 3)/4F) - (padding * 2));

        if (cap.getItemList().size() != 0){
            ArrayList<Item> itemArrayList = cap.getItemList();
            for (Item item : itemArrayList) {
                float backgroundX = halfWidth + halfMouseSizeX + 5;
                float backgroundW = txtRoomWidth - halfMouseSizeX - 5;
                float backgroundY = mouse_idle_IMG.y0 + (txtRoomHeight/4) + offset;
                float backgroundH = itemSize + (padding * 2);

                //Draw the background
                ClientUtil.blitColor(stack, backgroundX, backgroundW, backgroundY,
                        backgroundH, new Color(0, 0, 0, 255).getRGB());

                float smallestSize = Math.min((backgroundW/3),backgroundH);

                //Draw the item
                ClientUtil.blitItem(stack, backgroundX + (((backgroundW/4F) - smallestSize))/2F, smallestSize,
                        backgroundY + padding, smallestSize, new ItemStack(item));
//                mC.getItemRenderer().renderAndDecorateItem(new ItemStack(item),
//                        (int) (halfWidth + halfMouseSizeX + 5 + padding), (int) (mouse_idle_IMG.y0 + (txtRoomHeight/4) + offset + padding));

                //Draw the amount they have, then the amount they will have after reduction
                int count = inst.player.inventory.countItem(item);
                IFormattableTextComponent countComp = new StringTextComponent("" + count).withStyle(TextFormatting.BOLD).withStyle(TextFormatting.GREEN);
                IFormattableTextComponent arrowComp = new StringTextComponent(" -> ").withStyle(TextFormatting.BOLD);
                IFormattableTextComponent newCountComp =
                        new StringTextComponent("" + (count - (Math.round(Math.max(1, ReviveMeConfig.sacrificialItemPercent * count)))))
                                .withStyle(TextFormatting.BOLD).withStyle(TextFormatting.RED);

                float startX = backgroundX + (backgroundW/4F);
                float spacePortion = backgroundW/4F;
                float startY = mouse_idle_IMG.y0 + (txtRoomHeight/4) + offset + padding;
                TextUtil.renderText(stack, countComp, true, (int) startX, spacePortion, startY, itemSize, 1, TextUtil.txtAlignment.MIDDLE);
                TextUtil.renderText(stack, arrowComp, 1, true, (int) startX + spacePortion, spacePortion, startY-1, itemSize + (padding * 2), 0, TextUtil.txtAlignment.MIDDLE);
                TextUtil.renderText(stack, newCountComp, true, (int) startX + (spacePortion*2), spacePortion, startY, itemSize, 1, TextUtil.txtAlignment.MIDDLE);

                offset += backgroundH;
            }
        }
        //endregion

        //region This will be if the player is holding a button

        //I have to render the mouse later on so that it isn't affected by blackout
        ClientUtil.Image chosenMouse = mouse_idle_IMG;

        //This will be chance
        if (VanillaKeybindHandler.attackHeld) {
            ClientUtil.blitColor(stack, halfWidth, txtRoomWidth, mouse_idle_IMG.y0, txtRoomHeight,
                    new Color(0,0,0, Math.min((int) (255 * (FallenPlayerActionsEvent.timeHeld/40F)), 255)).getRGB());

            chosenMouse = mouse_left_IMG;
        }

        //This will use items
        else if (VanillaKeybindHandler.useHeld) {
            ClientUtil.blitColor(stack, halfWidth - txtRoomWidth, txtRoomWidth, mouse_idle_IMG.y0, txtRoomHeight,
                    new Color(0,0,0, Math.min((int) (255 * (FallenPlayerActionsEvent.timeHeld/40F)), 255)).getRGB());

            chosenMouse = mouse_right_IMG;
        }
        //endregion

        //region this will black out the options you've already used

        //This is for item sacrifice
        if (cap.usedSacrificedItems() || cap.getItemList().size() == 0){
            ClientUtil.blitColor(stack, halfWidth, txtRoomWidth, mouse_idle_IMG.y0, txtRoomHeight, new Color(0,0,0, 255).getRGB());

            IFormattableTextComponent item_text = new TranslationTextComponent(cap.usedSacrificedItems() ? "fallenScreen.self_revive.used" : "fallenScreen.self_revive.no_items");
            item_text = item_text.withStyle(TextFormatting.DARK_RED);

            TextUtil.renderText(stack, item_text, true,
                    halfWidth + halfMouseSizeX, txtRoomWidth - halfMouseSizeX, mouse_idle_IMG.y0,
                    txtRoomHeight, 4, TextUtil.txtAlignment.MIDDLE);
        }
        //This is for chance
        if (cap.usedChance()){
            ClientUtil.blitColor(stack, halfWidth - txtRoomWidth, txtRoomWidth, mouse_idle_IMG.y0, txtRoomHeight, new Color(0,0,0, 255).getRGB());
            TextUtil.renderText(stack, new TranslationTextComponent("fallenScreen.self_revive.used").withStyle(TextFormatting.DARK_RED), true,
                    halfWidth - txtRoomWidth, txtRoomWidth - halfMouseSizeX, mouse_idle_IMG.y0, txtRoomHeight,  4, TextUtil.txtAlignment.MIDDLE);
        }
        //endregion

        //System.out.println(seconds);


        //region The timer stuff
        float seconds = cap.GetTimeLeft(true);
        //green color: 2616150
        float endAngle = seconds <= 0 ? 360 : seconds * 360;

        //Increase seconds by 1 if seconds isn't at 0
        seconds = cap.GetTimeLeft(false);
        seconds += (seconds <= 0 ? 0 : 1);

        IFormattableTextComponent timeLeftString =
                new StringTextComponent((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                        .withStyle(TextFormatting.RED, TextFormatting.BOLD);

        //This is the timer background
        timerIMG.resetScale();
        timerIMG.setActualSize((int) (radius * 1.75F), (int) (radius * 1.75F));
        timerIMG.centerImageX(0, width);
        timerIMG.moveTo(timerIMG.x0, timerOrigY);
        CircleRender.drawArc(stack, halfWidth, timerIMG.y0 + (timerIMG.getHeight()/2F), radius, 0, endAngle, greenColor);
        timerIMG.RenderImage(stack);

        float timeSizeDiff = 64F/timerIMG.getHeight();
        TextUtil.renderText(stack, timeLeftString, false,(timerIMG.x0 + 17/timeSizeDiff), (30/timeSizeDiff),
                (timerIMG.y0 + 17/timeSizeDiff), (30)/timeSizeDiff,0, TextUtil.txtAlignment.MIDDLE);
        //endregion


        //This will render the mouse
        chosenMouse.resetScale();
        chosenMouse.setActualSize((int) (chosenMouse.getWidth() * ((height/5F)/chosenMouse.getHeight())), height/5);
        chosenMouse.centerImageX(0, width);
        chosenMouse.moveTo(chosenMouse.x0, mouseOrigY);
        chosenMouse.RenderImage(stack);

    }

    @SubscribeEvent
    public static void renderChatFallenTimerScreen(RenderGameOverlayEvent.Pre event){
        if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
        if (!(mC.screen instanceof ChatScreen)) return;

        FallenCapability cap = FallenCapability.GetFallCap(inst.player);
        if (!cap.isFallen()) return;
        if (cap.getOtherPlayer() != null) return;

        MatrixStack stack = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        float halfWidth = (width / 2F);
        float thirdHeight = height - (height / 3F);

        //Where the timer will be placed.
        float seconds = cap.GetTimeLeft(true);
        //green color: 2616150
        float endAngle = seconds <= 0 ? 360 : seconds * 360;
        float radius = 36;
        CircleRender.drawArc(stack, halfWidth, thirdHeight, radius, 0, endAngle, greenColor);

        //Increase seconds by 1 if seconds isn't at 0
        seconds = cap.GetTimeLeft(false);
        seconds += (seconds <= 0 ? 0 : 1);

        IFormattableTextComponent timeLeftString =
                new StringTextComponent((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                        .withStyle(TextFormatting.RED, TextFormatting.BOLD);

        //This is the timer background
        timerIMG.resetScale();
        timerIMG.setActualSize(64, 64);
        timerIMG.moveTo(0, 0);
        timerIMG.centerImageX(0, width);
        timerIMG.centerImageY((int) (thirdHeight - radius), (int) (radius * 2));
        timerIMG.RenderImage(stack);

        TextUtil.renderText(stack, timeLeftString, false, timerIMG.x0 + 17, 30, timerIMG.y0 + 17, 30,
                0, TextUtil.txtAlignment.MIDDLE);
    }
}


