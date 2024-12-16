package invoker54.reviveme.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReviveScreenEvent {
    private static final Minecraft inst = Minecraft.getInstance();
    public static MutableComponent beingRevivedText = Component.translatable("reviveScreen.being_revived");
    public static MutableComponent revivingText = Component.translatable("reviveScreen.reviving");

    public static final int bgColor = new Color(35,35,35,255).getRGB();
    public static final int revColor = new Color(77, 77, 77, 121).getRGB();
    public static final int progressColor = new Color(247,247,247,255).getRGB();

    @SubscribeEvent
    public static void registerReviveScreen(RegisterGuiOverlaysEvent event){
        event.registerBelow(VanillaGuiOverlay.CHAT_PANEL.id(),"revive_screen", (gui, guiGraphics, partialTicks, width, height) -> {
            FallenCapability cap = FallenCapability.GetFallCap(inst.player);

            //MAKE SURE this only happens if you are being revived, or reviving someone
            if (cap.getOtherPlayer() == null) return;

            int startTextHeight = (height / 5);
            Font font = inst.font;
            PoseStack stack = guiGraphics.pose();
            RenderSystem.disableDepthTest();

            MutableComponent titleText;
            //Only do the red if you are the fallen
            if (cap.isFallen()) {
                ClientUtil.blitColor(stack, 0, width, 0, height, 1615855616);
                titleText = beingRevivedText;
            } else {
                ClientUtil.blitColor(stack, 0,width, 0, height, revColor);
                titleText = revivingText;
            }

            //Being Revived text
            TextUtil.renderText(stack, titleText, 1, true, 0, width, startTextHeight, 16, 0, TextUtil.txtAlignment.MIDDLE);

            int xOrigin = width / 2;
            int yOrigin = height / 2;


            //progress bar background
            ClientUtil.blitColor(stack, (int) (xOrigin * 0.5f), xOrigin, yOrigin + 8, 16, bgColor);

            float progress = Math.min(cap.getProgress(), 1);

            //System.out.println(progress);

            //Actual progress bar
            ClientUtil.blitColor( stack,(xOrigin * (1 - 0.5f * progress)), xOrigin * progress,
                    yOrigin + 10, 12, progressColor);

            RenderSystem.enableDepthTest();
        });
    }
}
