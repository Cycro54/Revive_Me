package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.awt.*;
import java.text.DecimalFormat;

import static invoker54.invocore.client.util.ClientUtil.mC;
import static invoker54.reviveme.client.event.FallScreenEvent.timerIMG;
import static invoker54.reviveme.client.event.ReviveScreenEvent.bgColor;
import static invoker54.reviveme.client.event.ReviveScreenEvent.progressColor;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class RenderFallPlateEvent {
    private static final ModLogger LOGGER = ModLogger.getLogger(RenderFallPlateEvent.class, ReviveMeConfig.debugMode);
    private static final Minecraft inst = Minecraft.getInstance();
    public static final DecimalFormat df = new DecimalFormat("0.0");
    public static final int greenProgCircle = new Color(39, 235, 86, 255).getRGB();
    public static final int redProgCircle = new Color(173, 17, 17, 255).getRGB();
    public static final int blackBg = new Color(0, 0, 0, 176).getRGB();
    public static final int whiteBg = new Color(255, 255, 255, 255).getRGB();

    @SubscribeEvent
    public static void renderWorldFallTimer(RenderWorldLastEvent event) {
        if (event.isCanceled()) return;
        if (ClientUtil.mC.isPaused()) return;

        for (Entity entity : inst.level.entitiesForRendering()) {
            if (!(entity instanceof PlayerEntity)) continue;
            if (entity.equals(mC.player)) continue;
            float distance = entity.distanceTo(mC.player);
            double maxDistance = Math.max(ReviveMeConfig.reviveGlowMaxDistance, ReviveMeConfig.deathTimerMaxDistance);
            if (distance > maxDistance) continue;

            PlayerEntity player = (PlayerEntity) entity;
            FallenCapability cap = FallenCapability.GetFallCap(player);
            MatrixStack stack = event.getMatrixStack();

            if (!cap.isFallen()) continue;

            BlockRayTraceResult rayResult = mC.player.level.clip(
                    new RayTraceContext(mC.player.getEyePosition(1.0F), entity.getEyePosition(1.0F)
                            , RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entity));
            boolean targetSeen = rayResult.getType() == RayTraceResult.Type.MISS;

            if (distance > 10 && !cap.isCallingForHelp() && !targetSeen) continue;

            float yOffset = entity.getBbHeight() * 0.40f;
            float sizeOffset = 0.5F;

            if (distance > 10) {
                float distanceOffset = (distance - 10);
                yOffset = (entity.getBbHeight() * 1.4f) + (distanceOffset * 0.03f);
                sizeOffset += (distanceOffset * (0.5f / 10f));
            }

            stack.pushPose();

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            //Getting into position
            Vector3d difference = entity.position().subtract(mC.gameRenderer.getMainCamera().getPosition());
            stack.translate(difference.x, difference.y + yOffset, difference.z);
            stack.mulPose(mC.getEntityRenderDispatcher().cameraOrientation());
            stack.scale(-0.025F, -0.025F, 0.025F);
            stack.scale(sizeOffset, sizeOffset, sizeOffset);

            if (cap.getOtherPlayer() == null) {
                int radius = 22;
                InvoText chosenText = null;
                int chosenColor = 0;
                boolean canRender = false;

                if (!mC.player.isCrouching() && !player.isDeadOrDying()) {
                    canRender = true;
                    chosenColor = greenProgCircle;

                    float seconds = cap.GetTimeLeft(false);
                    seconds += (seconds <= 0 ? 0 : 1);
                    chosenText = InvoText.literal((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                            .withStyle(true, TextFormatting.BOLD)
                            .withStyle(false, cap.hasEnough(inst.player) ? TextFormatting.GREEN : TextFormatting.RED);
                } else if (mC.player.isCrouching() || player.isDeadOrDying()) {
                    canRender = true;
                    chosenColor = redProgCircle;

                    chosenText = InvoText.literal(Integer.toString((int) Math.ceil(cap.getKillTime(false))))
                            .withStyle(true, TextFormatting.BOLD, TextFormatting.RED);
                }

                if (canRender && distance < ReviveMeConfig.deathTimerMaxDistance) {
                    float endAngle = 360;
                    if (inst.player.isCrouching()) {
                        endAngle = endAngle * (cap.getKillTime(true));
                    } else if (ReviveMeConfig.timeLeft != 0) endAngle *= cap.GetTimeLeft(true);

                    if (cap.GetTimeLeft(false) <= 0)
                        CircleRender.drawArc(stack, 0, 0, radius, 0, endAngle, chosenColor);
                    else CircleRender.drawArc(stack, 0, 0, radius, 0, endAngle, chosenColor);

                    InvoZone timerZone = timerIMG.getRenderZone();
                    timerZone.setHeight(40).setWidth(40);
                    timerZone.centerX(0).centerY(0);
                    timerIMG.render(stack);

                    TextUtil.renderText(stack, chosenText.getText(), false, 1,
                            timerZone.inflate(-timerZone.width() / 4, -timerZone.height() / 4), TextUtil.txtAlignment.MIDDLE);
                }

                InvoText message = null;
                if (mC.crosshairPickEntity == player && !player.isDeadOrDying()) {
                    if (mC.player.isCrouching()) {
                        if (cap.getKillTime(false) > 0) {
                            message = InvoText.translate("revive-me.fall_plate.cant_kill");
                        } else {
                            message = InvoText.translate("revive-me.fall_plate.kill").setArgs(
                                    InvoText.literal(VanillaKeybindHandler.getKey(inst.options.keyAttack).getDisplayName().getString())
                                            .withStyle(true, TextFormatting.YELLOW, TextFormatting.BOLD).getText()
                            );
                        }
                    } else if (cap.hasEnough(mC.player)) {
                        message = InvoText.translate("revive-me.fall_plate.revive").setArgs(
                                InvoText.literal(VanillaKeybindHandler.getKey(inst.options.keyUse).getDisplayName().getString())
                                        .withStyle(true, TextFormatting.YELLOW, TextFormatting.BOLD).getText()
                        );

                    }
                }
                if (message == null && cap.isCallingForHelp()) {
                    message = InvoText.literal("")
                            .append(InvoText.literal("ABBA ").withStyle(true, TextFormatting.BOLD, TextFormatting.RED, TextFormatting.OBFUSCATED))
                            .append(InvoText.literal("[").withStyle(true, TextFormatting.BOLD).getText())
                            .append(InvoText.translate("revive-me.call_for_help").withStyle(true, TextFormatting.BOLD, TextFormatting.GOLD))
                            .append(InvoText.literal("]").withStyle(true, TextFormatting.BOLD))
                            .append(InvoText.literal(" ABBA").withStyle(true, TextFormatting.BOLD, TextFormatting.RED, TextFormatting.OBFUSCATED));
                }

                if (message != null) {
                    int txtWidth = mC.font.width(message.getText());
                    int padding = 2;
                    int width = txtWidth + (padding * 2);
                    int height = (mC.font.lineHeight + (padding * 2));
                    int x0 = (-(width) / 2);
                    int y0 = -(height + radius);
                    InvoZone txtZone = new InvoZone(x0, width, y0, height);
                    if (distance > ReviveMeConfig.deathTimerMaxDistance) txtZone.setY(0);

                    ClientUtil.blitColor(stack, txtZone, blackBg);
                    TextUtil.renderText(stack, message.getText(), false, 1,
                            txtZone.inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);
                }
            } else if (!mC.player.getUUID().equals(cap.getOtherPlayer()) && distance < ReviveMeConfig.deathTimerMaxDistance) {
                int radius = 20;

                //region Render the revive text
                InvoText message = ReviveScreenEvent.beingRevivedText;
                int txtWidth = mC.font.width(message.getText());
                int padding = 1;

                int width = txtWidth + (padding * 2);
                int height = (mC.font.lineHeight + (padding * 2));

                ClientUtil.blitColor(stack, -(width) / 2, width, -(height + radius), height, blackBg);

                height = mC.font.lineHeight;
                TextUtil.renderText(message.getText(), stack, -txtWidth / 2F, -(height + radius + padding), false);
                //endregion

                //progress bar background
                InvoZone progressZone = new InvoZone(0, 40, -5, 10);
                progressZone.centerX(0);
                ClientUtil.blitColor(stack, progressZone, bgColor); //prev color: 2302755

                float progress = cap.getProgress(true);
                progressZone.inflate(-1, -1).setWidth(progressZone.width() * progress);
                progressZone.centerX(0);

                //System.out.println(progress);

                //Actual progress bar
                ClientUtil.blitColor(stack, progressZone, progressColor);
            }

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            stack.popPose();
        }
    }
}
