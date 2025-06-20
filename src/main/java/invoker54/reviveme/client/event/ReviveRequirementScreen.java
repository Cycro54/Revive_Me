package invoker54.reviveme.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.JavaOps;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static invoker54.reviveme.ReviveMe.makeResource;
import static invoker54.reviveme.client.event.FallScreenEvent.*;
import static invoker54.reviveme.client.event.RenderFallPlateEvent.blackBg;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ReviveRequirementScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public static void registerRequirementScreen(RegisterGuiLayersEvent event){
        Minecraft mC = ClientUtil.getMinecraft();
        //if (true) return;
        event.registerAboveAll(makeResource("requirement_screen"), (guiGraphics, tracker) -> {
            if (ClientUtil.getPlayer().isCreative() || ClientUtil.getPlayer().isSpectator()) return;
            //if (true) return;
            if (!(mC.crosshairPickEntity instanceof Player)) return;
            if (((Player) mC.crosshairPickEntity).isDeadOrDying()) return;
            if (ClientUtil.getPlayer().isCrouching()) return;
            FallenData cap = FallenData.get((LivingEntity) mC.crosshairPickEntity);
            if (!cap.isFallen()) return;
            if (cap.getOtherPlayer() != null) return;
            if (ReviveMeConfig.penaltyType == FallenData.PENALTYPE.NONE) return;

            InvoZone workZone = new InvoZone(0, guiGraphics.guiWidth(), 0, guiGraphics.guiHeight());
            PoseStack stack = guiGraphics.pose();

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

            if (ReviveMeConfig.penaltyType == FallenData.PENALTYPE.ITEM) {
                ItemStack penaltyStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(ReviveMeConfig.penaltyItem)));
                try {
                    penaltyStack.applyComponents(DataComponentPatch.CODEC.decode(JavaOps.INSTANCE, ReviveMeConfig.penaltyItemData).getOrThrow().getFirst());
                } catch (Exception e) {
                    LOGGER.warn(e.getMessage());
                }
                ClientUtil.blitItem(stack, chosenZone, penaltyStack);
            }
            if (chosenImg != null) {
                chosenImg.render(stack);
            }

            //This is penalty amount txt
            //Penalty txt
            InvoText penaltyAmount = InvoText.literal(Integer.toString((int) cap.getPenaltyAmount(mC.player)))
                    .withStyle(true, ChatFormatting.BOLD)
                    .withStyle(false, cap.hasEnough(mC.player) ? ChatFormatting.GREEN : ChatFormatting.RED);

            TextUtil.renderText(stack, penaltyAmount.getText(), false, 1,
                    requirementZone.copy().setX(requirementZone.middleX()).splitWidth(2, 1)
                            .inflate(-4, -4), TextUtil.txtAlignment.MIDDLE);

            //This is how much you have, and how much you will have after
            int startAmount = (int) Math.round(cap.countReviverPenaltyAmount(mC.player));
            int endAmount = Math.round(startAmount - cap.getPenaltyAmount(mC.player));

            InvoText startTxt = InvoText.literal("" + startAmount)
                    .withStyle(true, ChatFormatting.BOLD, ChatFormatting.GREEN);

            InvoText arrowTxt = InvoText.literal("->")
                    .withStyle(true, ChatFormatting.BOLD);

            InvoText endTxt = InvoText.literal("" + endAmount)
                    .withStyle(true, ChatFormatting.BOLD, ChatFormatting.RED);

            requirementZone.splitHeight(2, 1).shift(0, (requirementZone.height() * 2) + 10);
            ClientUtil.blitColor(stack, requirementZone, blackBg);

            requirementZone.splitWidth(3, 1);

            TextUtil.renderText(stack, startTxt.getText(), true, 1,
                    requirementZone.copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);

            TextUtil.renderText(stack, arrowTxt.getText(), true, 1,
                    requirementZone.shift(requirementZone.width(), 0).copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);

            TextUtil.renderText(stack, endTxt.getText(), true, 1,
                    requirementZone.shift(requirementZone.width(), 0).copy().inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);
        });
    }

}
