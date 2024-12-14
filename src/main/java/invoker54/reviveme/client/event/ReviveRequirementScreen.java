package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static invoker54.invocore.client.ClientUtil.mC;
import static invoker54.reviveme.client.event.FallScreenEvent.*;
import static invoker54.reviveme.client.event.RenderFallPlateEvent.blackBg;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class ReviveRequirementScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void renderRequirementScreen(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
        if (ClientUtil.getPlayer().isCreative() || ClientUtil.getPlayer().isSpectator()) return;
        //if (true) return;
        if (!(mC.crosshairPickEntity instanceof PlayerEntity)) return;
        if (((PlayerEntity) mC.crosshairPickEntity).isDeadOrDying()) return;
        FallenCapability cap = FallenCapability.GetFallCap((LivingEntity) mC.crosshairPickEntity);
        if (!cap.isFallen()) return;
        if (cap.getOtherPlayer() != null) return;
        if (cap.getPenaltyType() == FallenCapability.PENALTYPE.NONE) return;

        MatrixStack stack = event.getMatrixStack();
        int fullWidth = event.getWindow().getGuiScaledWidth();
        int fullHeight = event.getWindow().getGuiScaledHeight();

        //50%
        int halfWidth = fullWidth / 2;
        int halfHeight = fullHeight / 2;

        //25%
        int quarterWidth = halfWidth / 2;
        int quarterHeight = halfHeight / 2;

        //12.5%
        int eighthWidth = quarterWidth / 2;
        int eighthHeight = quarterHeight / 2;

        int x0 = halfWidth + eighthWidth;
        int y0 = eighthHeight;

        int penaltyTypeSize = 16;
        int padding = 2;
        padding *= 2;
        int space = Math.min(Math.min(eighthHeight, eighthWidth), (penaltyTypeSize * 4) + padding);
        space -= padding;

        float scaleFactor = 1;
        stack.pushPose();
        if (space > penaltyTypeSize) scaleFactor = space / (float) penaltyTypeSize;
        penaltyTypeSize *= scaleFactor;
        int panelWidth = (space * 2) + (padding * 2);
        int panelHeight = space + padding;

        //This is the background of the requirements
        ClientUtil.blitColor(stack, x0, panelWidth, eighthHeight, panelHeight, blackBg);

        //This is the picture
        //Revive type item texture
        switch (cap.getPenaltyType()) {
            case NONE:
                break;
            case HEALTH:
                heartIMG.centerImageX(x0, panelWidth / 2);
                heartIMG.centerImageY(eighthHeight, panelHeight);
                heartIMG.setActualSize(penaltyTypeSize, penaltyTypeSize);
                heartIMG.RenderImage(stack);
                break;
            case EXPERIENCE:
                xpIMG.centerImageX(x0, panelWidth / 2);
                xpIMG.centerImageY(eighthHeight, panelHeight);
                xpIMG.setActualSize(penaltyTypeSize, penaltyTypeSize);
                xpIMG.RenderImage(stack);
                break;
            case FOOD:
                foodIMG.centerImageX(x0, panelWidth / 2);
                foodIMG.centerImageY(eighthHeight, panelHeight);
                foodIMG.setActualSize(penaltyTypeSize, penaltyTypeSize);
                foodIMG.RenderImage(stack);
                break;
            case ITEM:
                ClientUtil.blitItem(stack, x0 + (((panelWidth / 2F) - penaltyTypeSize) / 2F), penaltyTypeSize,
                        (y0 + ((panelHeight - penaltyTypeSize) / 2F)), penaltyTypeSize, cap.getPenaltyItem());
                break;
        }

        //This is penalty amount txt
        //Penalty txt
        IFormattableTextComponent penaltyAmount = new StringTextComponent(Integer.toString((int) cap.getPenaltyAmount(mC.player)))
                .withStyle(TextFormatting.BOLD)
                .withStyle(cap.hasEnough(mC.player) ? TextFormatting.GREEN : TextFormatting.RED);

        TextUtil.renderText(stack, penaltyAmount, false, x0 + (panelWidth / 2F),
                (panelWidth / 2F), eighthHeight, panelHeight, padding / 2, TextUtil.txtAlignment.MIDDLE);

        //This is how much you have, and how much you will have after
        int requirementBoxHeight = (eighthHeight+panelHeight) + 10;
        int startAmount = (int) Math.round(cap.countReviverPenaltyAmount(mC.player));
        int endAmount = Math.round(startAmount-cap.getPenaltyAmount(mC.player));
        float panelThirdWidth = panelWidth/3F;

        IFormattableTextComponent startTxt = new StringTextComponent(""+startAmount)
                .withStyle(TextFormatting.BOLD)
                .withStyle(TextFormatting.GREEN);

        IFormattableTextComponent arrowTxt = new StringTextComponent("->")
                .withStyle(TextFormatting.BOLD);

        IFormattableTextComponent endTxt = new StringTextComponent(""+endAmount)
                .withStyle(TextFormatting.BOLD)
                .withStyle(TextFormatting.RED);

        ClientUtil.blitColor(stack, x0, panelWidth, requirementBoxHeight, panelHeight/2F, blackBg);
        TextUtil.renderText(stack, startTxt, 1, true, x0,
                panelThirdWidth,requirementBoxHeight, panelHeight/2F, 2, TextUtil.txtAlignment.MIDDLE);
        TextUtil.renderText(stack, arrowTxt, 1, true, x0+(panelThirdWidth),
                panelThirdWidth,requirementBoxHeight, panelHeight/2F, 2, TextUtil.txtAlignment.MIDDLE);
        TextUtil.renderText(stack, endTxt, 1, true, x0+(panelThirdWidth*2),
                panelThirdWidth,requirementBoxHeight, panelHeight/2F, 2, TextUtil.txtAlignment.MIDDLE);
    }
}