package invoker54.reviveme.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.awt.*;

import static invoker54.reviveme.ReviveMe.makeResource;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class ReviveScreenEvent {
    public static InvoText beingRevivedText = InvoText.translate("reviveScreen.being_revived");
    public static InvoText useItemText = InvoText.translate("reviveScreen.use_item");
    public static InvoText revivingText = InvoText.translate("reviveScreen.reviving");

    public static final int bgColor = new Color(35,35,35,255).getRGB();
    public static final int revColor = new Color(77, 77, 77, 121).getRGB();
    public static final int progressColor = new Color(247,247,247,255).getRGB();

    @SubscribeEvent
    public static void registerReviveScreen(RegisterGuiLayersEvent event){
        event.registerBelow(VanillaGuiLayers.CHAT,makeResource("revive_screen"), (GuiGraphicsExtractor, tracker) -> {
            FallenData cap = FallenData.get(ClientUtil.getPlayer());
//            PoseStack GuiGraphicsExtractor = GuiGraphicsExtractor.pose();

            //MAKE SURE this only happens if you are being revived, or reviving someone
            if (cap.getOtherPlayer() == null) return;
            InvoZone workZone = new InvoZone(0, GuiGraphicsExtractor.guiWidth(), 0, GuiGraphicsExtractor.guiHeight());

            InvoText titleText;

//        Player reviver = cap.isFallen() ? ClientUtil.getMinecraft().player.level().getPlayerByUUID(cap.getOtherPlayer()) : ClientUtil.getMinecraft().player;
            boolean hasReviveItem = cap.getReviveStack() != null;
            //Only do the red if you are the fallen
            if (cap.isFallen()) {
                ClientUtil.blit2DColor(GuiGraphicsExtractor, workZone, 1615855616);
                titleText = beingRevivedText;
                if (hasReviveItem) titleText = useItemText;
            } else {
                ClientUtil.blit2DColor(GuiGraphicsExtractor, workZone, revColor);
                titleText = revivingText;
                if (hasReviveItem) titleText = useItemText;
            }

            InvoZone textZone = workZone.copy().splitHeight(5, 1);
            textZone.setY(textZone.down()).setHeight(16);
            //Being Revived text
            TextUtil.render2DText(GuiGraphicsExtractor, titleText.getText(), true, 1, textZone, TextUtil.txtAlignment.MIDDLE);

            InvoZone barZone = workZone.copy().setHeight(16).splitWidth(2, 1).center(workZone);
            //progress bar background
            ClientUtil.blit2DColor(GuiGraphicsExtractor, barZone, bgColor);

            float progress = cap.getProgress(true);

            //System.out.println(progress);

            //Actual progress bar
            ClientUtil.blit2DColor(GuiGraphicsExtractor, barZone.copy().splitWidth(1, progress)
                    .inflate(0, -2).center(barZone), progressColor);
        });
    }
}
