package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ReviveScreenEvent {
    private static Minecraft inst = Minecraft.getInstance();
    public static InvoText beingRevivedText = InvoText.translate("reviveScreen.being_revived");
    public static InvoText revivingText = InvoText.translate("reviveScreen.reviving");

    public static final int bgColor = new Color(35,35,35,255).getRGB();
    public static final int revColor = new Color(77, 77, 77, 121).getRGB();
    public static final int progressColor = new Color(247,247,247,255).getRGB();

    @SubscribeEvent
    public static void renderReviveScreen(RenderGameOverlayEvent.Pre event){
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        FallenCapability cap = FallenCapability.GetFallCap(inst.player);


        //MAKE SURE this only happens if you are being revived, or reviving someone
        if (cap.getOtherPlayer() == null) return;
        InvoZone workZone = new InvoZone(0, event.getWindow().getGuiScaledWidth(),
                0, event.getWindow().getGuiScaledHeight());

        MatrixStack stack = event.getMatrixStack();

        InvoText titleText;
        //Only do the red if you are the fallen
        if (cap.isFallen()) {
            ClientUtil.blitColor(stack, workZone, 1615855616);
            titleText = beingRevivedText;
        } else {
            ClientUtil.blitColor(stack, workZone, revColor);
            titleText = revivingText;
        }

        InvoZone textZone = workZone.copy().splitHeight(5, 1);
        textZone.setY(textZone.down()).setHeight(16);
        //Being Revived text
        TextUtil.renderText(stack, titleText.getText(), true, 1, textZone, TextUtil.txtAlignment.MIDDLE);

        InvoZone barZone = workZone.copy().setHeight(16).splitWidth(2,1).center(workZone);
        //progress bar background
        ClientUtil.blitColor(stack, barZone, bgColor);

        float progress = Math.min(cap.getProgress(), 1);

        //System.out.println(progress);

        //Actual progress bar
        ClientUtil.blitColor(stack, barZone.copy().splitWidth(1, progress)
                .inflate(0,-2).center(barZone), progressColor);
    }

    //This will be what the person reviving someone will see
}
