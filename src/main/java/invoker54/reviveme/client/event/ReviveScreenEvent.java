package invoker54.reviveme.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import invoker54.invocore.client.ClientUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.client.gui.OverlayRegistry;

import java.awt.*;

public class ReviveScreenEvent {
    private static Minecraft inst = Minecraft.getInstance();
    public static MutableComponent beingRevivedText = new TranslatableComponent("reviveScreen.being_revived");
    public static MutableComponent revivingText = new TranslatableComponent("reviveScreen.reviving");

    public static final int bgColor = new Color(35,35,35,255).getRGB();
    public static final int revColor = new Color(77, 77, 77, 121).getRGB();
    public static final int progressColor = new Color(247,247,247,255).getRGB();

    public static void registerReviveScreen(){
        OverlayRegistry.registerOverlayTop("revive_screen", (gui, stack, partialTicks, width, height) -> {
            FallenCapability cap = FallenCapability.GetFallCap(inst.player);

            //MAKE SURE this only happens if you are being revived, or reviving someone
            if (cap.getOtherPlayer() == null) return;

            int startTextHeight = (height / 5);
            Font font = inst.font;

            MutableComponent titleText;
            //Only do the red if you are the fallen
            if (cap.isFallen()) {
                Gui.fill(stack, 0, 0, width, height, 1615855616);
                titleText = beingRevivedText;
            } else {
                Gui.fill(stack, 0, 0, width, height, revColor);
                titleText = revivingText;
            }

            //Being Revived text
            stack.pushPose();
            stack.scale(2, 2, 2);
            Gui.drawCenteredString(stack, font, titleText, (width / 2) / 2, (startTextHeight - 5) / 2, 16777215);
            stack.popPose();

            int xOrigin = width / 2;
            int yOrigin = height / 2;


            RenderSystem.disableDepthTest();
            //progress bar background
            Gui.fill(stack, (int) (xOrigin * 0.5f), yOrigin + 8,
                    (int) (xOrigin * 1.5f), yOrigin - 8, bgColor); //prev color: 2302755

            float progress = Math.min(cap.getProgress(), 1);

            //System.out.println(progress);

            //Actual progress bar
            ClientUtil.blitColor(stack, (xOrigin * (1 - 0.5f * progress)), xOrigin * progress,
                    yOrigin - 6, 12, progressColor);
            RenderSystem.enableDepthTest();
        });
    }
}
