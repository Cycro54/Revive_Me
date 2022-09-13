package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.DecimalFormat;

import static net.minecraft.client.gui.AbstractGui.blit;

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
            editText = editText.replace("{seconds}", df.format(2 - (FallenPlayerActionsEvent.timeHeld/20f)));
            AbstractGui.drawCenteredString(stack, font, editText, width/2, (startTextHeight * 2), 16777215);

            //Where the timer will be placed.
            int x = (width / 2);
            int y = height - (height/3);
            float seconds = cap.GetTimeLeft(true);

            //System.out.println(seconds);

            //green color: 2616150
            CircleRender.drawArc(stack, x, y, 36, 0, 360 * seconds, 2616150);

            //Increase seconds by 1 if seconds isn't at 0
            seconds = cap.GetTimeLeft(false);
            seconds += (seconds == 0 ? 0 : 1);

            stack.pushPose();
            stack.scale(2, 2, 1);

            //This is the timer background
            inst.getTextureManager().bind(Timer_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            blit(stack,(x - 32)/2, (y - 32)/2, 0, 0F, 0F, 32, 32, 32, 32);
            RenderSystem.disableBlend();
            inst.getTextureManager().release(Timer_TEXTURE);

            //Seconds left text (the 9 here stands for font height)
            font.draw(stack, Integer.toString((int) seconds), (x - font.width(Integer.toString((int) seconds)))/2f,
                    (y/2f - 9/2), TextFormatting.RED.getColor());
            stack.popPose();
        }
    }
}


