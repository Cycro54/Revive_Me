package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.text.DecimalFormat;

import static invoker54.invocore.client.ClientUtil.mC;
import static invoker54.reviveme.client.event.FallScreenEvent.*;
import static invoker54.reviveme.client.event.ReviveScreenEvent.bgColor;
import static invoker54.reviveme.client.event.ReviveScreenEvent.progressColor;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class RenderFallPlateEvent {
    private static final Minecraft inst = Minecraft.getInstance();
    private static final DecimalFormat df = new DecimalFormat("0.0");
    private static final int greenProgCircle = new Color(39, 235, 86, 255).getRGB();
    private static final int redProgCircle = new Color(173, 17, 17, 255).getRGB();
    private static final int blackBg = new Color(0, 0, 0, 176).getRGB();

    @SubscribeEvent
    public static void renderWorldFallTimer(RenderWorldLastEvent event) {

        for (Entity entity : inst.level.entitiesForRendering()) {
            if (!(entity instanceof PlayerEntity)) continue;
            if (entity.equals(mC.player)) continue;
            if (entity.distanceTo(mC.player) > 20) continue;

            PlayerEntity player = (PlayerEntity) entity;
            FallenCapability cap = FallenCapability.GetFallCap(player);
            MatrixStack stack = event.getMatrixStack();


            if (!cap.isFallen()) return;
            float f = entity.getBbHeight() * 0.30f;
            stack.pushPose();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            //Getting into position
            Vector3d difference = entity.position().subtract(mC.gameRenderer.getMainCamera().getPosition());
            stack.translate(difference.x, difference.y + f, difference.z);
            stack.mulPose(mC.getEntityRenderDispatcher().cameraOrientation());
            stack.scale(-0.025F, -0.025F, 0.025F);

            if (cap.getOtherPlayer() == null) {
                //This txt is for showing how the long the player has left to die
                if (!mC.player.isCrouching()) {
                    //Green circular progress
                    int radius = 22;
                    int modSize = 40;
                    if (ReviveMeConfig.timeLeft == 0 || cap.GetTimeLeft(false) <= 0)
                        CircleRender.drawArc(stack, 0, 0, radius, 0, 360, greenProgCircle);
                    else CircleRender.drawArc(stack, 0, 0, radius, 0, cap.GetTimeLeft(true) * 360, greenProgCircle);

                    //Timer texture
                    timerIMG.setActualSize(40, 40);
                    timerIMG.moveTo(-(timerIMG.getWidth() / 2), -(timerIMG.getHeight() / 2));
                    timerIMG.RenderImage(stack);

                    //Revive type background
//        fill(stack, 2, -14, 14, -2, blackBg);
                    ClientUtil.blitColor(stack, (int) (modSize / 2F), 10, (int) (-modSize / 2F), 9, blackBg);
                    //Revive type item texture
                    switch (cap.getPenaltyType()) {
                        case NONE:
                            break;
                        case HEALTH:
                            heartIMG.moveTo((int) ((modSize / 2F) + 1), (int) (-modSize / 2F));
                            heartIMG.RenderImage(stack);
//                        ClientUtil.TEXTURE_MANAGER.bind(HEALTH_TEXTURE);
//                        ClientUtil.blitImage(stack, , 8, , 8, 0, 64, 0, 64, 64);
////                ClientUtil.blitImageWorld(stack, pos, 1, 0, 64, 0, 64, 64);
////                blit(stack, 4, -12, 0, 0F, 0F, 8, 8, 8, 8);
//                        ClientUtil.TEXTURE_MANAGER.release(HEALTH_TEXTURE);
                            break;
                        case EXPERIENCE:
                            xpIMG.moveTo((int) ((modSize / 2F) + 1), (int) (-modSize / 2F));
                            xpIMG.setActualSize(8, 8);
                            xpIMG.RenderImage(stack);
//                        ClientUtil.TEXTURE_MANAGER.bind(EXPERIENCE_TEXTURE);
//                        ClientUtil.blitImage(stack, (int) ((modSize / 2F) + 1), 8, (int) (-modSize / 2F), 8, 0, 16, 0, 16, 16);
////                blit(stack, 4, -12, 0, 0F, 0F, 8, 8, 8, 8);
//                        ClientUtil.TEXTURE_MANAGER.release(EXPERIENCE_TEXTURE);
                            break;
                        case FOOD:
                            foodIMG.moveTo((int) ((modSize / 2F) + 1), (int) ((-modSize / 2F) + 1));
                            foodIMG.setActualSize(8, 8);
                            foodIMG.RenderImage(stack);
//                        ClientUtil.TEXTURE_MANAGER.bind(FOOD_TEXTURE);
//                        ClientUtil.blitImage(stack, , 8, (-modSize / 2F) + 1, 8, 0, 18, 0, 18, 18);
////                blit(stack, 4, -12, 0, 0F, 0F, 8, 8, 8, 8);
//                        ClientUtil.TEXTURE_MANAGER.release(FOOD_TEXTURE);
                            break;
                    }

                    //Penalty txt
                    ITextComponent penaltyAmount = new StringTextComponent(Integer.toString((int) cap.getPenaltyAmount(player)))
                            .withStyle(TextFormatting.BOLD)
                            .withStyle(cap.hasEnough(inst.player) ? TextFormatting.GREEN : TextFormatting.RED);

                    float scaleFactor = (timerIMG.getWidth() / 64F);
                    TextUtil.renderText(stack, penaltyAmount, false, timerIMG.x0 + (17 * scaleFactor), 30 * scaleFactor,
                            timerIMG.y0 + (17 * scaleFactor), 30 * scaleFactor, 0, TextUtil.txtAlignment.MIDDLE);
                }
                //This txt is for showing if the player wishes to kill the fallen player
                else if (mC.player.isCrouching() || player.isDeadOrDying()) {
                    //Green circular progress
                    int radius = 22;
                    if (ReviveMeConfig.timeLeft == 0 || cap.GetTimeLeft(false) <= 0)
                        CircleRender.drawArc(stack, 0, 0, radius, 0, 360, redProgCircle);
                    else CircleRender.drawArc(stack, 0, 0, radius, 0, cap.GetTimeLeft(true) * 360, redProgCircle);

                    //Timer texture
                    timerIMG.setActualSize(40, 40);
                    timerIMG.moveTo(-(timerIMG.getWidth() / 2), -(timerIMG.getHeight() / 2));
                    timerIMG.RenderImage(stack);

                    ITextComponent killTxt = new StringTextComponent("0").withStyle(TextFormatting.BOLD, TextFormatting.RED);

                    float scaleFactor = (timerIMG.getWidth() / 64F);
                    TextUtil.renderText(stack, killTxt, false, timerIMG.x0 + (17 * scaleFactor), 30 * scaleFactor,
                            timerIMG.y0 + (17 * scaleFactor), 30 * scaleFactor, 0, TextUtil.txtAlignment.MIDDLE);
                }

                if (mC.crosshairPickEntity == player && !player.isDeadOrDying()) {
                    int radius = 30;

                    ITextComponent message = null;
                    if (mC.player.isCrouching()) {
                        message = new TranslationTextComponent("revive-me.fall_plate.kill");
                        message = new StringTextComponent(message.getString()
                                .replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString()));
                    } else if (cap.hasEnough(mC.player)) {
                        message = new TranslationTextComponent("revive-me.fall_plate.revive");
                        message = new StringTextComponent(message.getString()
                                .replace("{use}", inst.options.keyUse.getKey().getDisplayName().getString()));

                    }

                    if (message != null) {
                        int txtWidth = mC.font.width(message);
                        int padding = 2;

                        int width = txtWidth + (padding * 2);
                        int height = (mC.font.lineHeight + (padding * 2));

                        RenderSystem.disableDepthTest();
                        ClientUtil.blitColor(stack, -(width) / 2, width, -(height + radius), height, blackBg);
                        RenderSystem.enableDepthTest();

                        height = mC.font.lineHeight;
                        TextUtil.renderText(message, stack, -txtWidth / 2F, -(height + radius + padding), false);
                    }
                }
            }
            else if (!mC.player.getUUID().equals(cap.getOtherPlayer())){
                int radius = 20;

                //region Render the revive text
                ITextComponent message = ReviveScreenEvent.beingRevivedText;
                int txtWidth = mC.font.width(message);
                int padding = 1;

                int width = txtWidth + (padding * 2);
                int height = (mC.font.lineHeight + (padding * 2));

                RenderSystem.disableDepthTest();
                ClientUtil.blitColor(stack, -(width) / 2, width, -(height + radius), height, blackBg);
                RenderSystem.enableDepthTest();

                height = mC.font.lineHeight;
                TextUtil.renderText(message, stack, -txtWidth / 2F, -(height + radius + padding), false);
                //endregion

                int xOrigin = -20;
                int yOrigin = -10;

                //progress bar background
                RenderSystem.disableDepthTest();
                AbstractGui.fill(event.getMatrixStack(), xOrigin - padding, 0 + padding,
                        Math.abs(xOrigin - padding), yOrigin - padding, bgColor); //prev color: 2302755

                float progress = cap.getProgress();

                //System.out.println(progress);

                //Actual progress bar
                AbstractGui.fill(event.getMatrixStack(),  Math.round (xOrigin * -progress), 0,
                        Math.round (xOrigin * progress), yOrigin, progressColor);
                RenderSystem.enableDepthTest();
            }

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            stack.popPose();
        }
    }

//    @SubscribeEvent
//    public static void renderFallenInfo(RenderNameplateEvent event){
//        if (!(event.getEntity() instanceof PlayerEntity)) return;
//
//        FallenCapability cap = FallenCapability.GetFallCap((LivingEntity) event.getEntity());
//        if (!cap.isFallen()) return;
//        if (mC.crosshairPickEntity != event.getEntity()) return;
//        if (cap.getOtherPlayer() != null) return;
//
//        event.setResult(Event.Result.DENY);
//
////
////        ITextComponent message = null;
////        if (mC.player.isCrouching()) {
////            message = new TranslationTextComponent("revive-me.fall_plate.kill");
////            message = new StringTextComponent(message.getString()
////                    .replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString()));
////        }
////        else if (cap.hasEnough(mC.player)) {
////            message = new TranslationTextComponent("revive-me.fall_plate.revive");
////            message = new StringTextComponent(message.getString()
////                    .replace("{use}", inst.options.keyUse.getKey().getDisplayName().getString()));
////
////        }
////
////        if (message != null) event.setContent(message);
//
////        if (mC.crosshairPickEntity == player) {
////            int radius = 22;
////
////            ITextComponent message = null;
////            if (mC.player.isCrouching()) {
////                message = new TranslationTextComponent("revive-me.fall_plate.kill");
////                message = new StringTextComponent(message.getString()
////                        .replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString()));
////            }
////            else if (cap.hasEnough(mC.player)) {
////                message = new TranslationTextComponent("revive-me.fall_plate.revive");
////                message = new StringTextComponent(message.getString()
////                        .replace("{use}", inst.options.keyUse.getKey().getDisplayName().getString()));
////
////            }
////
////            if (message != null) {
////                int txtWidth = mC.font.width(message);
////                int padding = 2;
////
////                int width = txtWidth + (padding * 2);
////                int height = (mC.font.lineHeight + (padding * 2));
////
////                RenderSystem.disableDepthTest();
////                ClientUtil.blitColor(stack, -(width) / 2, width, -(height + radius), height, blackBg);
////                RenderSystem.enableDepthTest();
////
////                height = mC.font.lineHeight;
////                GuiUtil.renderText(message, stack, -txtWidth / 2F, -(height + radius + padding), false);
////            }
////        }
//    }
}
