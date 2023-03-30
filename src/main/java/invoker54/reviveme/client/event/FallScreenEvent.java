package invoker54.reviveme.client.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static invoker54.invocore.client.ClientUtil.mC;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FallScreenEvent {
    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean isPaused = false;
    private static Minecraft inst = Minecraft.getInstance();

    public static final ResourceLocation Timer_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID,"textures/screens/timer_background.png");
    public static final ResourceLocation HEALTH_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID,"textures/revive_types/heart.png");
    public static final ResourceLocation FOOD_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID,"textures/revive_types/hunger.png");
    public static final ResourceLocation EXPERIENCE_TEXTURE = new
            ResourceLocation(ReviveMe.MOD_ID,"textures/revive_types/experience_bottle.png");

    //Images
    public static ClientUtil.Image timerIMG = new ClientUtil.Image(Timer_TEXTURE, 0, 64, 0, 64, 64);
    public static ClientUtil.Image heartIMG = new ClientUtil.Image(HEALTH_TEXTURE, 0, 8, 0, 8, 8);
    public static ClientUtil.Image xpIMG = new ClientUtil.Image(EXPERIENCE_TEXTURE, 0, 16, 0, 16, 16);
    public static ClientUtil.Image foodIMG = new ClientUtil.Image(FOOD_TEXTURE, 0, 18, 0, 18,18);

    private static final MutableComponent titleText = Component.translatable("fallenScreen.fallen_text");
    private static final MutableComponent waitText = Component.translatable("fallenScreen.wait_text");
    private static final MutableComponent forceDeathText = Component.translatable("fallenScreen.force_death_text");
    private static final DecimalFormat df = new DecimalFormat("0.0");
    private static final int greenColor = new Color(39, 235, 86, 255).getRGB();

    @SubscribeEvent
    public static void registerFallenScreen(RegisterGuiOverlaysEvent event){
        //if (true) return;
        event.registerAboveAll("fallen_screen", (gui, stack, partialTicks, width, height) -> {
            if (mC.getSingleplayerServer() != null &&
                    mC.getSingleplayerServer().getPlayerList().getPlayers().size() == 1) return;

            FallenCapability cap = FallenCapability.GetFallCap(inst.player);
            if (!cap.isFallen()) return;

            if (cap.getOtherPlayer() != null) return;

            int startTextHeight = (height / 5);
            Font font = mC.font;

            Gui.fill(stack, 0, 0, width, height, 1615855616);

            //Title text
            stack.pushPose();
            stack.scale(2, 2, 2);
            Gui.drawCenteredString(stack, font, titleText, (width / 2) / 2, (startTextHeight - 10) / 2, 16777215);
            stack.popPose();

            //Wait For text
            Gui.drawCenteredString(stack, font, waitText, width / 2, (int) (startTextHeight * 1.5f), 16777215);
            //Force death text
            String editText = forceDeathText.getString();
            editText = editText.replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString());
            editText = editText.replace("{seconds}", df.format(2 - (FallenPlayerActionsEvent.timeHeld / 20f)));
            Gui.drawCenteredString(stack, font, editText, width / 2, (startTextHeight * 2), 16777215);

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
            seconds += (seconds == 0 ? 0 : 1);

            MutableComponent timeLeftString =
                    Component.literal((ReviveMeConfig.timeLeft == 0 && seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

            //This is the timer background
            timerIMG.resetScale();
            timerIMG.setActualSize(64, 64);
            timerIMG.moveTo(0, 0);
            timerIMG.centerImageX(0, width);
            timerIMG.centerImageY((int) (y - radius), (int) (radius * 2));
            timerIMG.RenderImage(stack);

            TextUtil.renderText(stack, timeLeftString, false, timerIMG.x0 + 17, 30, timerIMG.y0 + 17, 30,
                    0, TextUtil.txtAlignment.MIDDLE);
        });

        event.registerAboveAll("fallen_single_player_screen", (gui, stack, partialTicks, width, height) -> {
            if (!Minecraft.getInstance().hasSingleplayerServer()) return;
            if (mC.getSingleplayerServer().getPlayerList().getPlayers().size() != 1) return;

            FallenCapability cap = FallenCapability.GetFallCap(inst.player);
            if (!cap.isFallen()) return;
            if (cap.getOtherPlayer() != null) return;

            int fifthHeight = (height / 5);
            float halfWidth = (width / 2F);
            float thirdHeight = height - (height / 3F);
            Font font = mC.font;

            Gui.fill(stack, 0, 0, width, height, 1615855616);

            //Title text
            stack.pushPose();
            stack.scale(2, 2, 2);
            Gui.drawCenteredString(stack, font, titleText, (width / 2) / 2, ((fifthHeight - 18)/2)/2, 16777215);
            stack.popPose();

            //Other revive items top
            float startHeight = fifthHeight + ((fifthHeight - 9)/2F);
            
            ClientUtil.blitColor(stack, halfWidth/4F, (halfWidth/2F), startHeight,
                    20, new Color(0,0,0, 182).getRGB());
            MutableComponent stringToRender = Component.translatable("fallenScreen.single_player.give_chance_1");
            stringToRender = Component.literal(stringToRender.getString().replace("{attack}",inst.options.keyAttack.getKey().getDisplayName().getString()));
            //Left mouse to give chance
            TextUtil.renderText(stack, stringToRender.withStyle(ChatFormatting.BOLD), true, halfWidth/4, (halfWidth/2F),
                    startHeight, 20, 2, TextUtil.txtAlignment.MIDDLE);

            ClientUtil.blitColor(stack, halfWidth/4F, (halfWidth/2F), startHeight + 40,
                    (halfWidth/2F) - 60, new Color(0,0,0, 255).getRGB());
            stringToRender = Component.literal("" + Math.round(ReviveMeConfig.reviveChance * 100) + "%");
            TextUtil.renderText(stack, stringToRender.withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GOLD),
                    true, halfWidth/4, (halfWidth/2F), startHeight,
                    (halfWidth/2F) + 20, 2, TextUtil.txtAlignment.MIDDLE);

            ClientUtil.blitColor(stack, halfWidth/4F, (halfWidth/2F), startHeight + (halfWidth/2F),
                    20, new Color(0,0,0, 182).getRGB());
            stringToRender = Component.translatable("fallenScreen.single_player.give_chance_2");
            TextUtil.renderText(stack, stringToRender.withStyle(ChatFormatting.BOLD), true, halfWidth/4, (halfWidth/2F),
                    startHeight + (halfWidth/2F), 20, 2, TextUtil.txtAlignment.MIDDLE);


//            (ReviveMeConfig.reviveChance * 100)+"%"


            //Right mouse to give items
            ClientUtil.blitColor(stack, (halfWidth + halfWidth/4), (halfWidth/2F),
                    startHeight, 20, new Color(0,0,0, 182).getRGB());
            stringToRender = Component.translatable("fallenScreen.single_player.give_items");
            stringToRender = Component.literal(stringToRender.getString().replace("{use}",inst.options.keyUse.getKey().getDisplayName().getString()));
            TextUtil.renderText(stack, stringToRender.withStyle(ChatFormatting.BOLD), true, (halfWidth + halfWidth/4), (halfWidth/2F),
                                startHeight, 20, 1, TextUtil.txtAlignment.MIDDLE);

            //Where the timer will be placed.
            float seconds = cap.GetTimeLeft(true);

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
                int count = inst.player.getInventory().countItem(item);
                MutableComponent countComp = Component.literal("" + count).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN);
                MutableComponent arrowComp = Component.literal(" -> ").withStyle(ChatFormatting.BOLD);
                MutableComponent newCountComp =
                        Component.literal("" + (count - (Math.round(Math.max(1, ReviveMeConfig.sacrificialItemPercent * count)))))
                                .withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED);

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

            //Divider Lines in the middle
            ClientUtil.blitColor(stack, halfWidth - 1, 2, fifthHeight, height, new Color(0, 0, 0,255).getRGB());
            ClientUtil.blitColor(stack, 0, width, fifthHeight, 2, new Color(0, 0, 0,255).getRGB());

            //region Black out for when holding a button down
            //This will be chance
            if (inst.options.keyAttack.isDown()) {
                ClientUtil.blitColor(stack, halfWidth, halfWidth, fifthHeight, height,
                        new Color(0,0,0, Math.min((int) (255 * (FallenPlayerActionsEvent.timeHeld/40F)), 255)).getRGB());
            }

            //This will use items
            else if (inst.options.keyUse.isDown()) {
                ClientUtil.blitColor(stack, 0, halfWidth, fifthHeight, height,
                        new Color(0,0,0, Math.min((int) (255 * (FallenPlayerActionsEvent.timeHeld/40F)), 255)).getRGB());
            }
            //endregion

            if (cap.usedSacrificedItems()){
                ClientUtil.blitColor(stack, halfWidth, halfWidth, fifthHeight, height, new Color(0,0,0, 179).getRGB());
            }
            if (cap.usedChance()){
                ClientUtil.blitColor(stack, 0, halfWidth, fifthHeight, height, new Color(0,0,0, 179).getRGB());
            }

            //System.out.println(seconds);

            //green color: 2616150
            float endAngle = seconds <= 0 ? 360 : seconds * 360;
            float radius = 36;
            CircleRender.drawArc(stack, halfWidth, thirdHeight, radius, 0, endAngle, greenColor);

            //Increase seconds by 1 if seconds isn't at 0
            seconds = cap.GetTimeLeft(false);
            seconds += (seconds == 0 ? 0 : 1);

            MutableComponent timeLeftString =
                    Component.literal((ReviveMeConfig.timeLeft == 0 || seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                            .withStyle(ChatFormatting.RED, ChatFormatting.BOLD);

            //This is the timer background
            timerIMG.resetScale();
            timerIMG.setActualSize(64, 64);
            timerIMG.moveTo(0, 0);
            timerIMG.centerImageX(0, width);
            timerIMG.centerImageY((int) (thirdHeight - radius), (int) (radius * 2));
            timerIMG.RenderImage(stack);

            TextUtil.renderText(stack, timeLeftString, false, timerIMG.x0 + 17, 30, timerIMG.y0 + 17, 30,
                    0, TextUtil.txtAlignment.MIDDLE);
        });
    }
}


