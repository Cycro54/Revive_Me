package invoker54.reviveme.client.event;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.OverlayRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static invoker54.invocore.client.ClientUtil.mC;
import static invoker54.reviveme.client.event.FallScreenEvent.*;
import static invoker54.reviveme.client.event.RenderFallPlateEvent.blackBg;

public class ReviveRequirementScreen {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void registerRequirementScreen(){
        //if (true) return;
        OverlayRegistry.registerOverlayTop("requirement_screen", (gui, stack, partialTicks, fullWidth, fullHeight) -> {
            if (!(mC.crosshairPickEntity instanceof Player)) return;
            if (((Player) mC.crosshairPickEntity).isDeadOrDying()) return;
            FallenCapability cap = FallenCapability.GetFallCap((LivingEntity) mC.crosshairPickEntity);
            if (!cap.isFallen()) return;
            if (cap.getOtherPlayer() != null) return;
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
                    stack.scale(scaleFactor, scaleFactor, 1);

                    Lighting.setupForFlatItems();
                    MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
                    ItemRenderer renderer = mC.getItemRenderer();
                    ItemStack itemStack = cap.getPenaltyItem();
                    BakedModel bakedModel = renderer.getModel(itemStack, null, null, 0);
                    RenderSystem.disableDepthTest();

                    PoseStack posestack = RenderSystem.getModelViewStack();
                    posestack.pushPose();
                    posestack.translate((x0 + (((panelWidth/2F) - (penaltyTypeSize/scaleFactor))/2F)),
                            (y0 + ((panelHeight - (penaltyTypeSize/scaleFactor))/2F)), (double)(100.0F + renderer.blitOffset));
                    posestack.translate(8.0D, 8.0D, 0.0D);
                    posestack.scale(1.0F, -1.0F, 1.0F);
                    posestack.scale(16.0F, 16.0F, 16.0F);
                    RenderSystem.applyModelViewMatrix();
                    boolean flag = !bakedModel.usesBlockLight();
                    if (flag) {
                        Lighting.setupForFlatItems();
                    }
                    renderer.render(itemStack, ItemTransforms.TransformType.GUI, false, stack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedModel);
                    bufferSource.endBatch();
                    if (flag) {
                        Lighting.setupFor3DItems();
                    }
                    posestack.popPose();
                    RenderSystem.applyModelViewMatrix();
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
        });
    }

}
