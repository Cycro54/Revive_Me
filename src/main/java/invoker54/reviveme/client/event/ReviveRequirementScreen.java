package invoker54.reviveme.client.event;

import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static invoker54.invocore.client.ClientUtil.getPlayer;
import static invoker54.invocore.client.ClientUtil.mC;
import static invoker54.reviveme.client.event.FallScreenEvent.*;
import static invoker54.reviveme.client.event.RenderFallPlateEvent.blackBg;

public class ReviveRequirementScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void registerRequirementScreen(){
        //if (true) return;
        OverlayRegistry.registerOverlayTop("requirement_screen", (gui, stack, partialTicks, fullWidth, fullHeight) -> {
            if (ClientUtil.getPlayer().isCreative() || ClientUtil.getPlayer().isSpectator()) return;
            if (!(mC.crosshairPickEntity instanceof Player)) return;
            if (((Player) mC.crosshairPickEntity).isDeadOrDying()) return;
            if (getPlayer().isCrouching()) return;
            FallenCapability cap = FallenCapability.GetFallCap((LivingEntity) mC.crosshairPickEntity);
            if (!cap.isFallen()) return;
            if (cap.getOtherPlayer() != null) return;
            if (cap.getPenaltyType() == FallenCapability.PENALTYPE.NONE) return;

            //50%
            int halfWidth = fullWidth/2;
            int halfHeight = fullHeight/2;

            //25%
            int quarterWidth = halfWidth/2;
            int quarterHeight = halfHeight/2;

            //12.5%
            int eighthWidth = quarterWidth/2;
            int eighthHeight = quarterHeight/2;

            int x0 = halfWidth + eighthWidth;
            int y0 = eighthHeight;
            
            int penaltyTypeSize = 16;
            int padding = 2;
            padding *= 2;
            int space = Math.min(Math.min(eighthHeight, eighthWidth), (penaltyTypeSize * 4) + padding);
            space -= padding;
            
            float scaleFactor = 1;
            stack.pushPose();
            if (space > penaltyTypeSize) scaleFactor = space/(float)penaltyTypeSize;
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
                    heartIMG.centerImageX(x0, panelWidth/2);
                    heartIMG.centerImageY(eighthHeight, panelHeight);
                    heartIMG.setActualSize(penaltyTypeSize, penaltyTypeSize);
                    heartIMG.RenderImage(stack);
                    break;
                case EXPERIENCE:
                    xpIMG.centerImageX(x0, panelWidth/2);
                    xpIMG.centerImageY(eighthHeight, panelHeight);
                    xpIMG.setActualSize(penaltyTypeSize, penaltyTypeSize);
                    xpIMG.RenderImage(stack);
                    break;
                case FOOD:
                    foodIMG.centerImageX(x0, panelWidth/2);
                    foodIMG.centerImageY(eighthHeight, panelHeight);
                    foodIMG.setActualSize(penaltyTypeSize, penaltyTypeSize);
                    foodIMG.RenderImage(stack);
                    break;
                case ITEM:
                    ClientUtil.blitItem(stack, x0 + (((panelWidth/2F) - penaltyTypeSize)/2F), penaltyTypeSize,
                            (y0 + ((panelHeight - penaltyTypeSize)/2F)), penaltyTypeSize, cap.getPenaltyItem());
                    break;
            }
            
            stack.popPose();

            //This is penalty amount txt
            //Penalty txt
            MutableComponent penaltyAmount = new TextComponent(Integer.toString((int) cap.getPenaltyAmount(mC.player)))
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(cap.hasEnough(mC.player) ? ChatFormatting.GREEN : ChatFormatting.RED);

            TextUtil.renderText(stack, penaltyAmount, false, x0 + (panelWidth/2F),
                    (panelWidth/2F), eighthHeight, panelHeight, padding/2, TextUtil.txtAlignment.MIDDLE);

            //This is how much you have, and how much you will have after
            int requirementBoxHeight = (eighthHeight+panelHeight) + 10;
            int startAmount = (int) Math.round(cap.countReviverPenaltyAmount(mC.player));
            int endAmount = Math.round(startAmount-cap.getPenaltyAmount(mC.player));
            float panelThirdWidth = panelWidth/3F;

            MutableComponent startTxt = new TextComponent(""+startAmount)
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.GREEN);

            MutableComponent arrowTxt = new TextComponent("->")
                    .withStyle(ChatFormatting.BOLD);

            MutableComponent endTxt = new TextComponent(""+endAmount)
                    .withStyle(ChatFormatting.BOLD)
                    .withStyle(ChatFormatting.RED);

            ClientUtil.blitColor(stack, x0, panelWidth, requirementBoxHeight, panelHeight/2F, blackBg);
            TextUtil.renderText(stack, startTxt, 1, true, x0,
                    panelThirdWidth,requirementBoxHeight, panelHeight/2F, 2, TextUtil.txtAlignment.MIDDLE);
            TextUtil.renderText(stack, arrowTxt, 1, true, x0+(panelThirdWidth),
                    panelThirdWidth,requirementBoxHeight, panelHeight/2F, 2, TextUtil.txtAlignment.MIDDLE);
            TextUtil.renderText(stack, endTxt, 1, true, x0+(panelThirdWidth*2),
                    panelThirdWidth,requirementBoxHeight, panelHeight/2F, 2, TextUtil.txtAlignment.MIDDLE);
        });
    }

}
