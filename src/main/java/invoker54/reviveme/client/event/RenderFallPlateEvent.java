package invoker54.reviveme.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.ClientUtil;
import invoker54.invocore.client.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
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
    public static void renderWorldFallTimer(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        for (Entity entity : inst.level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (entity.equals(mC.player)) continue;
            if (entity.distanceTo(mC.player) > 20) continue;

            FallenCapability cap = FallenCapability.GetFallCap(player);
            PoseStack stack = event.getPoseStack();

            if (!cap.isFallen()) continue;
            float f = entity.getBbHeight() * 0.30f;
            stack.pushPose();

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            //Getting into position
            Vec3 difference = entity.position().subtract(mC.gameRenderer.getMainCamera().getPosition());
            stack.translate(difference.x, difference.y + f, difference.z);
            stack.mulPose(mC.getEntityRenderDispatcher().cameraOrientation());
            stack.scale(-0.025F, -0.025F, 0.025F);

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

                    MutableComponent penaltyAmount = Component.literal((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                            .withStyle(ChatFormatting.BOLD)
                            .withStyle(cap.hasEnough(inst.player) ? ChatFormatting.GREEN : ChatFormatting.RED);

                    float scaleFactor = (timerIMG.getWidth() / 64F);
                    TextUtil.renderText(stack, penaltyAmount, false, timerIMG.x0 + (17 * scaleFactor), 30 * scaleFactor,
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

                    MutableComponent killTxt = Component.literal("0").withStyle(ChatFormatting.BOLD, ChatFormatting.RED);

                    float scaleFactor = (timerIMG.getWidth() / 64F);
                    TextUtil.renderText(stack, killTxt, false, timerIMG.x0 + (17 * scaleFactor), 30 * scaleFactor,
                            timerIMG.y0 + (17 * scaleFactor), 30 * scaleFactor, 0, TextUtil.txtAlignment.MIDDLE);
                }

                if (mC.crosshairPickEntity == player && !player.isDeadOrDying()) {
                    int radius = 30;

                    MutableComponent message = null;
                    if (mC.player.isCrouching()) {
                        message = Component.translatable("revive-me.fall_plate.kill");
                        message = Component.literal(message.getString()
                                .replace("{attack}", inst.options.keyAttack.getKey().getDisplayName().getString()));
                    } else if (cap.hasEnough(mC.player)) {
                        message = Component.translatable("revive-me.fall_plate.revive");
                        message = Component.literal(message.getString()
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
                MutableComponent message = ReviveScreenEvent.beingRevivedText;
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
                Gui.fill(event.getPoseStack(), xOrigin - padding, 0 + padding,
                        Math.abs(xOrigin - padding), yOrigin - padding, bgColor); //prev color: 2302755

                float progress = cap.getProgress();

                //System.out.println(progress);

                //Actual progress bar
                ClientUtil.blitColor(event.getPoseStack(),  xOrigin * progress,
                        xOrigin * -progress * 2F,0, yOrigin, progressColor);
            }

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            stack.popPose();
        }
    }
}
