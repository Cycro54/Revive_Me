package invoker54.reviveme.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.InvoTextFormat;
import invoker54.reviveme.common.ReviveMathUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static invoker54.invocore.client.util.ClientUtil.getPlayer;
import static invoker54.reviveme.client.event.FallScreenEvent.*;
import static invoker54.reviveme.client.event.RenderFallPlateEvent.blackBg;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ReviveRequirementScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void registerRequirementScreen(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("requirement_screen", (gui, stack, partialTicks, fullWidth, fullHeight) -> {
            if (getPlayer().isSpectator()) return;
            if (FallenCapability.get(ClientUtil.getPlayer()).isFallen()) return;
            if (!(ClientUtil.mC.crosshairPickEntity instanceof Player)) return;
            if (((Player) ClientUtil.mC.crosshairPickEntity).isDeadOrDying()) return;
            if (ClientUtil.getPlayer().isShiftKeyDown()) return;
            FallenCapability cap = FallenCapability.get((LivingEntity) ClientUtil.mC.crosshairPickEntity);
            if (!cap.isFallen()) return;
            if (cap.getOtherPlayer() != null) return;
            if (ReviveMeConfig.penaltyType == FallenCapability.PENALTYPE.NONE) return;

            InvoZone workZone = new InvoZone(0, fullWidth, 0, fullHeight);

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
            if (cap.canPlayerRevive() && !getPlayer().isCreative()) {
                renderRequirements(stack, requirementZone, cap, penaltyTypeSize);
                requirementZone.shift(0, requirementZone.height() + 2);
            } else {
                requirementZone.splitHeight(2, 1);
            }

            renderRevivesLeft(stack, requirementZone, cap);
        });
    }

    public static void renderRequirements(PoseStack stack, InvoZone requirementZone, FallenCapability cap, float penaltyTypeSize) {
        ClientUtil.Image chosenImg = null;
        InvoZone chosenZone = requirementZone.copy();

        ReviveItemData itemData = ReviveItemData.getData(ClientUtil.getPlayer().getMainHandItem(), ReviveItemData.USER.REVIVER);
        //This is the picture
        //Revive type item texture
        if (itemData == null) {
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
                    return;
            }
        }
        //This is the background of the requirements
        ClientUtil.blitColor(stack, requirementZone, blackBg);

        chosenZone.setWidth(penaltyTypeSize).setHeight(penaltyTypeSize).center(requirementZone.copy().splitWidth(2, 1));

        if (itemData != null) {
            ClientUtil.blitItem(stack, chosenZone, ClientUtil.getPlayer().getMainHandItem());
        }
        if (chosenImg != null) {
            chosenImg.render(stack);
        }

        //This is penalty amount txt
        //Penalty txt
        float penaltyAmount = cap.getPenaltyAmount(ClientUtil.getPlayer());
        if (ReviveMeConfig.penaltyType == FallenCapability.PENALTYPE.EXPERIENCE) {
            float totalExperience = ReviveMathUtil.getExperienceFromLevel(ClientUtil.getPlayer());
            float prevLevel = ClientUtil.getPlayer().experienceLevel + ClientUtil.getPlayer().experienceProgress;
            float newLevel = ReviveMathUtil.getLevelFromExperience((int) (totalExperience - penaltyAmount));
            penaltyAmount = Math.max(ReviveMathUtil.getLevelFromExperience((int) penaltyAmount), (prevLevel - newLevel));
        }

        InvoText penaltyText = InvoText.literal(df.format(penaltyAmount));
        if (itemData != null) penaltyText = InvoText.literal(Integer.toString(itemData.getCountRequired()));

        penaltyText.withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD))
                .withStyle(false, InvoTextFormat.filter(cap.hasEnough(ClientUtil.getPlayer()) ? ChatFormatting.GREEN : ChatFormatting.RED));

        TextUtil.renderText(stack, penaltyText.getText(), false, 1,
                requirementZone.copy().setX(requirementZone.middleX()).splitWidth(2, 1)
                        .inflate(-4, -4), TextUtil.txtAlignment.MIDDLE);

        //This is how much you have, and how much you will have after
        int startAmount;
        float endAmount;
        if (itemData == null) {
            startAmount = (int) Math.round(cap.countReviverPenaltyAmount(ClientUtil.getPlayer()));
            endAmount = Math.round(startAmount - penaltyAmount);

            if (ReviveMeConfig.penaltyType == FallenCapability.PENALTYPE.EXPERIENCE) {
                float totalExperience = ReviveMathUtil.getExperienceFromLevel(ClientUtil.getPlayer().experienceLevel, ClientUtil.getPlayer().experienceProgress);
                totalExperience = (totalExperience - cap.getPenaltyAmount(ClientUtil.getPlayer()));
                endAmount = ReviveMathUtil.getLevelFromExperience((int) totalExperience);
            }
        } else {
            startAmount = itemData.getItemCount(ClientUtil.getPlayer(), ClientUtil.getPlayer().getMainHandItem());
            endAmount = startAmount - itemData.getCountRequired();
        }

        InvoText startTxt = InvoText.literal("" + startAmount)
                .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.GREEN));

        InvoText arrowTxt = InvoText.literal("->")
                .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD));

        InvoText endTxt = InvoText.literal(df.format(endAmount))
                .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.RED));

        requirementZone.splitHeight(2, 1).shift(0, (requirementZone.height() * 2) + 10);
        ClientUtil.blitColor(stack, requirementZone, blackBg);

        InvoZone textZone = requirementZone.copy().splitWidth(3, 1);

        TextUtil.renderText(stack, startTxt.getText(), true, 1,
                textZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);

        TextUtil.renderText(stack, arrowTxt.getText(), true, 1,
                textZone.shift(textZone.width(), 0).copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);

        TextUtil.renderText(stack, endTxt.getText(), true, 1,
                textZone.shift(textZone.width(), 0).copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);


    }

    public static void renderRevivesLeft(PoseStack stack, InvoZone requirementZone, FallenCapability cap) {
        int playerReviveCount = cap.getPlayerReviveCount(true);
        if (playerReviveCount == -1) return;
        ClientUtil.blitColor(stack, requirementZone.inflate(5, 0), blackBg);

        InvoText pluralText = playerReviveCount == 1 ? reviveCountSingleText : reviveCountMultipleText;

        TextUtil.renderText(stack, InvoText.translate("revive_me.fall_plate.revive_count", InvoText.literal(cap.getPlayerReviveCount(true) + "")
                                .withStyle(true, InvoTextFormat.filter((playerReviveCount <= 1 ? ChatFormatting.RED : ChatFormatting.YELLOW), ChatFormatting.BOLD)).getText(),
                        pluralText.getText()).getText(), true, 2,
                requirementZone.inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);
    }
}