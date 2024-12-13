package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.Color;
import java.text.DecimalFormat;

import static invoker54.invocore.client.ClientUtil.mC;
import static invoker54.reviveme.client.event.FallScreenEvent.timerIMG;
import static invoker54.reviveme.client.event.ReviveScreenEvent.bgColor;
import static invoker54.reviveme.client.event.ReviveScreenEvent.progressColor;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class RenderFallPlateEvent {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Minecraft inst = Minecraft.getInstance();
    private static final DecimalFormat df = new DecimalFormat("0.0");
    public static final int greenProgCircle = new Color(39, 235, 86, 255).getRGB();
    public static final int redProgCircle = new Color(173, 17, 17, 255).getRGB();
    public static final int blackBg = new Color(0, 0, 0, 176).getRGB();

    @SubscribeEvent
    public static void renderWorldFallTimer(RenderWorldLastEvent event) {
        if (event.isCanceled()) return;
        if (ClientUtil.mC.isPaused()) return;

        for (Entity entity : inst.level.entitiesForRendering()) {
            if (!(entity instanceof PlayerEntity)) continue;
            if (entity.equals(mC.player)) continue;
            if (entity.distanceTo(mC.player) > 20) continue;

            PlayerEntity player = (PlayerEntity) entity;
            FallenCapability cap = FallenCapability.GetFallCap(player);
            MatrixStack stack = event.getMatrixStack();

            if (!cap.isFallen()) continue;
            float f = entity.getBbHeight() * 0.40f;
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
            stack.scale(0.5F, 0.5F, 0.5F);

            if (cap.getOtherPlayer() == null) {
                //This txt is for showing how the long the player has left to die
                if (!mC.player.isCrouching() && !player.isDeadOrDying()) {
                    //Green circular progress
                    int radius = 22;
                    int modSize = 40;
                    if (cap.GetTimeLeft(false) <= 0)
                        CircleRender.drawArc(stack, 0, 0, radius, 0, 360, greenProgCircle);
                    else CircleRender.drawArc(stack, 0, 0, radius, 0, cap.GetTimeLeft(true) * 360, greenProgCircle);

                    //Timer texture
                    timerIMG.setActualSize(40, 40);
                    timerIMG.moveTo(-(timerIMG.getWidth() / 2), -(timerIMG.getHeight() / 2));
                    timerIMG.RenderImage(stack);

//                    //Penalty txt
                    float seconds = cap.GetTimeLeft(false);
                    seconds += (seconds <= 0 ? 0 : 1);

                    IFormattableTextComponent penaltyAmount = new StringTextComponent((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                            .withStyle(TextFormatting.BOLD)
                            .withStyle(cap.hasEnough(inst.player) ? TextFormatting.GREEN : TextFormatting.RED);

                    float scaleFactor = (timerIMG.getWidth() / 64F);
                    TextUtil.renderText(stack, penaltyAmount, 1,false, timerIMG.x0 + (17 * scaleFactor), 30 * scaleFactor,
                            timerIMG.y0 + (17 * scaleFactor), 30 * scaleFactor, 0, TextUtil.txtAlignment.MIDDLE);
                }
                //This txt is for showing if the player wishes to kill the fallen player
                else if (mC.player.isCrouching() || player.isDeadOrDying()) {
                    //Green circular progress
                    int radius = 22;
                    if (cap.GetTimeLeft(false) <= 0)
                        CircleRender.drawArc(stack, 0, 0, radius, 0, 360, redProgCircle);
                    else CircleRender.drawArc(stack, 0, 0, radius, 0, cap.GetTimeLeft(true) * 360, redProgCircle);

                    //Timer texture
                    timerIMG.setActualSize(40, 40);
                    timerIMG.moveTo(-(timerIMG.getWidth() / 2), -(timerIMG.getHeight() / 2));
                    timerIMG.RenderImage(stack);

                    IFormattableTextComponent killTxt = new StringTextComponent("" + cap.getKillTime()).withStyle(TextFormatting.BOLD, TextFormatting.RED);

                    float scaleFactor = (timerIMG.getWidth() / 64F);
                    TextUtil.renderText(stack, killTxt, false, timerIMG.x0 + (17 * scaleFactor), 30 * scaleFactor,
                            timerIMG.y0 + (17 * scaleFactor), 30 * scaleFactor, 0, TextUtil.txtAlignment.MIDDLE);
                }

                if (mC.crosshairPickEntity == player && !player.isDeadOrDying()) {
                    int radius = 30;

                    IFormattableTextComponent message = null;
                    if (mC.player.isCrouching()){
                        if (cap.getKillTime() > 0){
                            message = new TranslationTextComponent("revive-me.fall_plate.cant_kill");
                        }
                        else {
                            message = new TranslationTextComponent("revive-me.fall_plate.kill");
                            message = new StringTextComponent(message.getString()
                                    .replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString()));
                        }
                    }
                    else if (cap.hasEnough(mC.player)) {
                        message = new TranslationTextComponent("revive-me.fall_plate.revive");
                        message = new StringTextComponent(message.getString()
                                .replace("{use}", inst.options.keyUse.getKey().getDisplayName().getString()));

                    }

                    if (message != null) {
                        int txtWidth = mC.font.width(message);
                        int padding = 2;

                        int width = txtWidth + (padding * 2);
                        int height = (mC.font.lineHeight + (padding * 2));

                        ClientUtil.blitColor(stack, -(width) / 2, width, -(height + radius), height, blackBg);

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

                ClientUtil.blitColor(stack, -(width) / 2, width, -(height + radius), height, blackBg);

                height = mC.font.lineHeight;
                TextUtil.renderText(message, stack, -txtWidth / 2F, -(height + radius + padding), false);
                //endregion

                int xOrigin = -20;
                int yOrigin = -10;

                //progress bar background
                AbstractGui.fill(stack, xOrigin - padding, padding,
                        Math.abs(xOrigin - padding), yOrigin - padding, bgColor); //prev color: 2302755

                float progress = cap.getProgress();

                //System.out.println(progress);

                //Actual progress bar
                ClientUtil.blitColor(stack,  xOrigin * progress,
                        xOrigin * -progress * 2F,0, yOrigin, progressColor);
            }

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            stack.popPose();
        }
    }
}
