package invoker54.reviveme.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.InvoTextFormat;
import invoker54.reviveme.common.ReviveMathUtil;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

import static invoker54.reviveme.ReviveMe.makeResource;
import static invoker54.reviveme.client.event.FallScreenEvent.*;
import static invoker54.reviveme.client.event.RenderFallPlateEvent.blackBg;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ReviveRequirementScreen {
    private static final ModLogger LOGGER = ModLogger.getLogger(ReviveRequirementScreen.class, ReviveMeConfig.debugMode);

    @SubscribeEvent
    public static void registerRequirementScreen(RegisterGuiLayersEvent event) {
        Minecraft mC = ClientUtil.getMinecraft();
        event.registerAboveAll(makeResource("requirement_screen"), (guiGraphics, tracker) -> {
            if (ClientUtil.getPlayer().isCreative() || ClientUtil.getPlayer().isSpectator()) return;
            if (FallenData.get(ClientUtil.getPlayer()).isFallen()) return;
            if (!(ClientUtil.getMinecraft().crosshairPickEntity instanceof Player)) return;
            if (((Player) ClientUtil.getMinecraft().crosshairPickEntity).isDeadOrDying()) return;
            if (ClientUtil.getPlayer().isShiftKeyDown()) return;
            FallenData cap = FallenData.get((LivingEntity) ClientUtil.getMinecraft().crosshairPickEntity);
            if (!cap.isFallen()) return;
            if (cap.getOtherPlayer() != null) return;
            if (ReviveMeConfig.penaltyType == FallenData.PENALTYPE.NONE) return;

            InvoZone workZone = new InvoZone(0, guiGraphics.guiWidth(), 0, guiGraphics.guiHeight());

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

            if (cap.canPlayerRevive()) {
                renderRequirements(guiGraphics.pose(), requirementZone, cap, penaltyTypeSize);
                requirementZone.shift(0, requirementZone.height() + 2);
            } else {
                requirementZone.splitHeight(2, 1);
            }

            renderRevivesLeft(guiGraphics.pose(), requirementZone, cap);
        });
    }

    public static void renderRequirements(PoseStack stack, InvoZone requirementZone, FallenData cap, float penaltyTypeSize){
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
        if (chosenImg != null) {chosenImg.render(stack);}

        //This is penalty amount txt
        //Penalty txt
        float penaltyAmount = cap.getPenaltyAmount(ClientUtil.getMinecraft().player);
        if (ReviveMeConfig.penaltyType == FallenData.PENALTYPE.EXPERIENCE){
            float totalExperience = ReviveMathUtil.getExperienceFromLevel(ClientUtil.getMinecraft().player);
            float prevLevel = ClientUtil.getMinecraft().player.experienceLevel + ClientUtil.getMinecraft().player.experienceProgress;
            float newLevel = ReviveMathUtil.getLevelFromExperience((int) (totalExperience - penaltyAmount));
            penaltyAmount = Math.max(ReviveMathUtil.getLevelFromExperience((int) penaltyAmount), (prevLevel - newLevel));
        }

        InvoText penaltyText = InvoText.literal(df.format(penaltyAmount));
        if (itemData != null) penaltyText = InvoText.literal(Integer.toString(itemData.getCountRequired()));

        penaltyText.withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD))
                .withStyle(false, InvoTextFormat.filter(cap.hasEnough(ClientUtil.getMinecraft().player) ? ChatFormatting.GREEN : ChatFormatting.RED));

        TextUtil.renderText(stack, penaltyText.getText(), false, 1,
                requirementZone.copy().setX(requirementZone.middleX()).splitWidth(2,1)
                        .inflate(-4,-4), TextUtil.txtAlignment.MIDDLE);

        //This is how much you have, and how much you will have after
        int startAmount;
        float endAmount;
        if (itemData == null){
            startAmount = (int) Math.round(cap.countReviverPenaltyAmount(ClientUtil.getMinecraft().player));
            endAmount = Math.round(startAmount - penaltyAmount);

            if (ReviveMeConfig.penaltyType == FallenData.PENALTYPE.EXPERIENCE){
                float totalExperience = ReviveMathUtil.getExperienceFromLevel(ClientUtil.getMinecraft().player.experienceLevel, ClientUtil.getMinecraft().player.experienceProgress);
                totalExperience = (totalExperience - cap.getPenaltyAmount(ClientUtil.getMinecraft().player));
                endAmount = ReviveMathUtil.getLevelFromExperience((int) totalExperience);
            }
        }
        else {
            startAmount = itemData.getItemCount(ClientUtil.getMinecraft().player, ClientUtil.getMinecraft().player.getMainHandItem());
            endAmount = startAmount - itemData.getCountRequired();
        }

        InvoText startTxt = InvoText.literal("" + startAmount)
                .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.GREEN));

        InvoText arrowTxt = InvoText.literal("->")
                .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD));

        InvoText endTxt = InvoText.literal(df.format(endAmount))
                .withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD,ChatFormatting.RED));

        requirementZone.splitHeight(2,1).shift(0, (requirementZone.height() * 2) + 10);
        ClientUtil.blitColor(stack, requirementZone, blackBg);

        InvoZone textZone = requirementZone.copy().splitWidth(3,1);

        TextUtil.renderText(stack, startTxt.getText(), true, 1,
                textZone.copy().inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);

        TextUtil.renderText(stack, arrowTxt.getText(), true, 1,
                textZone.shift(textZone.width(),0).copy().inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);

        TextUtil.renderText(stack, endTxt.getText(), true, 1,
                textZone.shift(textZone.width(),0).copy().inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);


    }

    public static void renderRevivesLeft(PoseStack stack, InvoZone requirementZone, FallenData cap){
        int playerReviveCount = cap.getPlayerReviveCount(true);
        if (playerReviveCount == -1) return;
        ClientUtil.blitColor(stack, requirementZone.inflate(5, 0), blackBg);

        InvoText pluralText = playerReviveCount == 1 ? reviveCountSingleText : reviveCountMultipleText;

        TextUtil.renderText(stack, InvoText.translate("revive_me.fall_plate.revive_count", InvoText.literal(cap.getPlayerReviveCount(true) + "")
                                .withStyle(true, InvoTextFormat.filter((playerReviveCount <= 1 ? ChatFormatting.RED : ChatFormatting.YELLOW), ChatFormatting.BOLD)).getText(),
                        pluralText.getText()).getText(), true, 2,
                requirementZone.inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);
    }

}
