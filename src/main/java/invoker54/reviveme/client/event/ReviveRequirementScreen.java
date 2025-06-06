package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import static invoker54.invocore.client.util.ClientUtil.getPlayer;
import static invoker54.invocore.client.util.ClientUtil.mC;
import static invoker54.reviveme.client.event.FallScreenEvent.*;
import static invoker54.reviveme.client.event.RenderFallPlateEvent.blackBg;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = ReviveMe.MOD_ID)
public class ReviveRequirementScreen {
    private static final ModLogger LOGGER = ModLogger.getLogger(ReviveRequirementScreen.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void renderRequirementScreen(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CHAT) return;
        if (getPlayer().isCreative() || getPlayer().isSpectator()) return;
        //if (true) return;
        if (!(mC.crosshairPickEntity instanceof PlayerEntity)) return;
        if (((PlayerEntity) mC.crosshairPickEntity).isDeadOrDying()) return;
        if (getPlayer().isCrouching()) return;
        FallenCapability cap = FallenCapability.GetFallCap((LivingEntity) mC.crosshairPickEntity);
        if (!cap.isFallen()) return;
        if (cap.getOtherPlayer() != null) return;
        if (ReviveMeConfig.penaltyType == FallenCapability.PENALTYPE.NONE) return;

        MatrixStack stack = event.getMatrixStack();
        InvoZone workZone = new InvoZone(0, event.getWindow().getGuiScaledWidth(),
                0, event.getWindow().getGuiScaledHeight());

        float penaltyTypeSize = 16;
        int padding = 2;
        padding *= 2;
        float space = Math.min(Math.min(workZone.height() / 8, workZone.width() / 8), (penaltyTypeSize * 4) + padding);
        space -= padding;

        float scaleFactor = 1;
        if (space > penaltyTypeSize) scaleFactor = space / penaltyTypeSize;
        penaltyTypeSize *= scaleFactor;
        float panelWidth = (space * 2) + (padding * 2);
        float panelHeight = space + padding;

        InvoZone requirementZone = new InvoZone(workZone.copy().splitWidth(8, 5).right(),
                panelWidth, workZone.height() / 8, panelHeight);

        //This is the background of the requirements
        ClientUtil.blitColor(stack, requirementZone, blackBg);

        ClientUtil.Image chosenImg = null;
        InvoZone chosenZone = requirementZone.copy();

        //This is the picture
        //Revive type item texture
        switch (ReviveMeConfig.penaltyType) {
            case NONE:
                return;
            case HEALTH:
                chosenImg = heartIMG;
                chosenZone = chosenImg.getRenderZone();
                break;
            case EXPERIENCE:
                chosenImg = xpIMG;
                chosenZone = chosenImg.getRenderZone();
                break;
            case FOOD:
                chosenImg = foodIMG;
                chosenZone = chosenImg.getRenderZone();
                break;
            case ITEM:
                break;
        }
        chosenZone.setWidth(penaltyTypeSize).setHeight(penaltyTypeSize).center(requirementZone.copy().splitWidth(2, 1));

        if (ReviveMeConfig.penaltyType == FallenCapability.PENALTYPE.ITEM) {
            ItemStack penaltyStack = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(ReviveMeConfig.penaltyItem)));
            penaltyStack.deserializeNBT(ReviveMeConfig.penaltyItemData);
            ClientUtil.blitItem(stack, chosenZone, penaltyStack);
        }
        if (chosenImg != null) {chosenImg.render(stack);}

        //This is penalty amount txt
        //Penalty txt
        InvoText penaltyAmount = InvoText.literal(Integer.toString((int) cap.getPenaltyAmount(mC.player)))
                .withStyle(true,TextFormatting.BOLD)
                .withStyle(false,cap.hasEnough(mC.player) ? TextFormatting.GREEN : TextFormatting.RED);

        TextUtil.renderText(stack, penaltyAmount.getText(), false, 1,
                requirementZone.copy().setX(requirementZone.middleX()).splitWidth(2,1)
                        .inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);

        //This is how much you have, and how much you will have after
        int startAmount = (int) Math.round(cap.countReviverPenaltyAmount(mC.player));
        int endAmount = Math.round(startAmount - cap.getPenaltyAmount(mC.player));

        InvoText startTxt = InvoText.literal("" + startAmount)
                .withStyle(true,TextFormatting.BOLD, TextFormatting.GREEN);

        InvoText arrowTxt = InvoText.literal("->")
                .withStyle(true,TextFormatting.BOLD);

        InvoText endTxt = InvoText.literal("" + endAmount)
                .withStyle(true,TextFormatting.BOLD,TextFormatting.RED);

        requirementZone.splitHeight(2,1).shift(0, (requirementZone.height() * 2) + 10);
        ClientUtil.blitColor(stack, requirementZone, blackBg);

        requirementZone.splitWidth(3,1);

        TextUtil.renderText(stack, startTxt.getText(), true, 1,
                requirementZone.copy().inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);

        TextUtil.renderText(stack, arrowTxt.getText(), true, 1,
                requirementZone.shift(requirementZone.width(),0).copy().inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);

        TextUtil.renderText(stack, endTxt.getText(), true, 1,
                requirementZone.shift(requirementZone.width(),0).copy().inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);
    }
}