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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.text.DecimalFormat;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class FallScreenEvent {
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
            FallenCapability cap = FallenCapability.GetFallCap(inst.player);
            if(!cap.isFallen()) return;

            if (cap.getOtherPlayer() != null) return;

            int startTextHeight = (height/5);
            Font font = ClientUtil.mC.font;

            Gui.fill(stack, 0, 0, width, height, 1615855616);

            //Title text
            stack.pushPose();
            stack.scale(2, 2,2 );
            Gui.drawCenteredString(stack, font, titleText, (width/2)/2, (startTextHeight - 10)/2, 16777215);
            stack.popPose();

            //Wait For text
            Gui.drawCenteredString(stack, font, waitText, width/2, (int) (startTextHeight * 1.5f), 16777215);
            //Force death text
            String editText = forceDeathText.getString();
            editText = editText.replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString());
            editText = editText.replace("{seconds}", df.format(2 - (FallenPlayerActionsEvent.timeHeld/20f)));
            Gui.drawCenteredString(stack, font, editText, width/2, (startTextHeight * 2), 16777215);

            //Where the timer will be placed.
            float x = (width / 2F);
            float y = height - (height/3F);
            float seconds = cap.GetTimeLeft(true);

            //System.out.println(seconds);

            //green color: 2616150
            float endAngle = seconds <= 0 ? 360 : seconds * 360;
            float radius = 36;
            CircleRender.drawArc(stack, x, y, radius, 0, endAngle, greenColor);

            //Increase seconds by 1 if seconds isn't at 0
            seconds = cap.GetTimeLeft(false);
            seconds += (seconds == 0 ? 0 : 1);

            Component timeLeftString =
                    Component.literal((ReviveMeConfig.timeLeft == 0 || seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                            .withStyle(ChatFormatting.RED,ChatFormatting.BOLD);

            //This is the timer background
            timerIMG.resetScale();
            timerIMG.setActualSize(64,64);
            timerIMG.moveTo(0,0);
            timerIMG.centerImageX(0, width);
            timerIMG.centerImageY((int) (y - radius), (int) (radius * 2));
            timerIMG.RenderImage(stack);

            TextUtil.renderText(stack, timeLeftString, false,timerIMG.x0 + 17, 30, timerIMG.y0 + 17, 30,
                    0, TextUtil.txtAlignment.MIDDLE);

        });
    }
}


