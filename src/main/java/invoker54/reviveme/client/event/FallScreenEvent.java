package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
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

    private static final ITextComponent titleText = new TranslationTextComponent("fallenScreen.fallen_text");
    private static final ITextComponent waitText = new TranslationTextComponent("fallenScreen.wait_text");
    private static final ITextComponent forceDeathText = new TranslationTextComponent("fallenScreen.force_death_text");
    private static final DecimalFormat df = new DecimalFormat("0.0");

    @SubscribeEvent
    public static void renderFallenScreen(RenderGameOverlayEvent.Pre event){
        //if (true) return;

        FallenCapability cap = FallenCapability.GetFallCap(inst.player);

        if(!cap.isFallen()) return;

        if (cap.getOtherPlayer() != null) return;

        MatrixStack stack = event.getMatrixStack();
        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();
        int startTextHeight = (height/5);
        FontRenderer font = inst.font;

        if(event.getType() == RenderGameOverlayEvent.ElementType.ALL){
            AbstractGui.fill(event.getMatrixStack(), 0, 0, width, height, 1615855616);

            //Title text
            stack.pushPose();
            stack.scale(2, 2,2 );
            AbstractGui.drawCenteredString(stack, font, titleText, (width/2)/2, (startTextHeight - 10)/2, 16777215);
            stack.popPose();

            //Wait For text
            AbstractGui.drawCenteredString(stack, font, waitText, width/2, (int) (startTextHeight * 1.5f), 16777215);
            //Force death text
            String editText = forceDeathText.getString();
            editText = editText.replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString());
            editText = editText.replace("{seconds}", df.format(2 - (FallenPlayerActionsEvent.timeHeld/20f)));
            AbstractGui.drawCenteredString(stack, font, editText, width/2, (startTextHeight * 2), 16777215);

            //Where the timer will be placed.
            float x = (width / 2F);
            float y = height - (height/3F);
            float seconds = cap.GetTimeLeft(true);

            //System.out.println(seconds);

            //green color: 2616150
            float endAngle = seconds <= 0 ? 360 : seconds * 360;
            float radius = 36;
            CircleRender.drawArc(stack, x, y, radius, 0, endAngle, 2616150);

            //Increase seconds by 1 if seconds isn't at 0
            seconds = cap.GetTimeLeft(false);
            seconds += (seconds == 0 ? 0 : 1);

            ITextComponent timeLeftString =
                    new StringTextComponent((ReviveMeConfig.timeLeft == 0 || seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                    .withStyle(TextFormatting.RED,TextFormatting.BOLD);

            //This is the timer background
            timerIMG.resetScale();
            timerIMG.setActualSize(64,64);
            timerIMG.moveTo(0,0);
            timerIMG.centerImageX(0, width);
            timerIMG.centerImageY((int) (y - radius), (int) (radius * 2));
            timerIMG.RenderImage(stack);
//            blit(stack,Math.round(((x - 32)/2F)), (int) ((y - 32)/2F), 0, 0F, 0F, 32, 32, 32, 32);

            //Seconds left text (the 9 here stands for font height)

//            font.draw(stack, timeLeftString, timerIMG.centerOnImageX(font.width(timeLeftString)),
//                    timerIMG.centerOnImageY(font.lineHeight), -1);
            TextUtil.renderText(stack, timeLeftString, false,timerIMG.x0 + 17, 30, timerIMG.y0 + 17, 30,
                    0, TextUtil.txtAlignment.MIDDLE);
        }
    }
}


