package invoker54.reviveme.client.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
            if (entity.equals(ClientUtil.mC.player)) continue;
            float distance = entity.distanceTo(ClientUtil.mC.player);
            if (distance > 30) continue;

            FallenCapability cap = FallenCapability.GetFallCap(player);
            PoseStack stack = event.getPoseStack();

            if (!cap.isFallen()) continue;
            if (!cap.isCallingForHelp() && distance > 10) continue;

            float f = entity.getBbHeight() * 0.40f;
            stack.pushPose();

            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableDepthTest();

            //Getting into position
            Vec3 difference = entity.position().subtract(ClientUtil.mC.gameRenderer.getMainCamera().getPosition());
            stack.translate(difference.x, difference.y + f, difference.z);
            stack.mulPose(ClientUtil.mC.getEntityRenderDispatcher().cameraOrientation());
            stack.scale(-0.025F, -0.025F, 0.025F);
            stack.scale(0.5F, 0.5F, 0.5F);

            if (cap.getOtherPlayer() == null) {
                int radius = 22;
                InvoText chosenText = null;
                int chosenColor = 0;
                boolean canRender = false;

                if (!ClientUtil.mC.player.isCrouching() && !player.isDeadOrDying()){
                    canRender = true;
                    chosenColor = greenProgCircle;

                    float seconds = cap.GetTimeLeft(false);
                    seconds += (seconds <= 0 ? 0 : 1);
                    chosenText = InvoText.literal((seconds <= 0) ? "INF" : Integer.toString((int) seconds))
                            .withStyle(true, ChatFormatting.BOLD)
                            .withStyle(false,cap.hasEnough(inst.player) ? ChatFormatting.GREEN : ChatFormatting.RED);
                }
                else if (ClientUtil.mC.player.isCrouching() || player.isDeadOrDying()){
                    canRender = true;
                    chosenColor = redProgCircle;

                    chosenText = InvoText.literal(Integer.toString((int) Math.ceil(cap.getKillTime())))
                            .withStyle(true,ChatFormatting.BOLD, ChatFormatting.RED);
                }

                if (canRender) {
                    float endAngle = 360;
                    if (inst.player.isCrouching()){
                        endAngle = endAngle * (cap.getKillTime() / ReviveMeConfig.reviveKillTime);
                    }
                    else if (ReviveMeConfig.timeLeft != 0) endAngle *= cap.GetTimeLeft(true);

                    if (cap.GetTimeLeft(false) <= 0)
                        CircleRender.drawArc(stack, 0, 0, radius, 0, endAngle, chosenColor);
                    else CircleRender.drawArc(stack, 0, 0, radius, 0, endAngle, chosenColor);

                    InvoZone timerZone = timerIMG.getRenderZone();
                    timerZone.setHeight(40).setWidth(40);
                    timerZone.centerX(0).centerY(0);
                    timerIMG.render(stack);

                    TextUtil.renderText(stack, chosenText.getText(),  false, 1,
                            timerZone.inflate(-timerZone.width() / 4, -timerZone.height() / 4), TextUtil.txtAlignment.MIDDLE);
                }

                InvoText message = null;
                if (ClientUtil.mC.crosshairPickEntity == player && !player.isDeadOrDying()) {
                    if (ClientUtil.mC.player.isCrouching()) {
                        if (cap.getKillTime() > 0) {
                            message = InvoText.translate("revive-me.fall_plate.cant_kill");
                        } else {
                            message = InvoText.translate("revive-me.fall_plate.kill").setArgs(
                                    InvoText.literal(VanillaKeybindHandler.getKey(inst.options.keyAttack).getDisplayName().getString())
                                            .withStyle(true, ChatFormatting.YELLOW, ChatFormatting.BOLD).getText()
                            );
                        }
                    } else if (cap.hasEnough(ClientUtil.mC.player)) {
                        message = InvoText.translate("revive-me.fall_plate.revive").setArgs(
                                InvoText.literal(VanillaKeybindHandler.getKey(inst.options.keyUse).getDisplayName().getString())
                                        .withStyle(true, ChatFormatting.YELLOW, ChatFormatting.BOLD).getText()
                        );

                    }
                }
                if (message == null && cap.isCallingForHelp()) {
                    message = InvoText.literal("")
                            .append(InvoText.literal("ABBA ").withStyle(true,ChatFormatting.BOLD, ChatFormatting.RED, ChatFormatting.OBFUSCATED))
                            .append(InvoText.literal("[").withStyle(true, ChatFormatting.BOLD).getText())
                            .append(InvoText.translate("revive-me.call_for_help").withStyle(true,ChatFormatting.BOLD, ChatFormatting.GOLD))
                            .append(InvoText.literal("]").withStyle(true,ChatFormatting.BOLD))
                            .append(InvoText.literal(" ABBA").withStyle(true,ChatFormatting.BOLD, ChatFormatting.RED, ChatFormatting.OBFUSCATED));
                }

                if (message != null) {
                    int txtWidth = ClientUtil.mC.font.width(message.getText());
                    int padding = 2;
                    int width = txtWidth + (padding * 2);
                    int height = (ClientUtil.mC.font.lineHeight + (padding * 2));
                    int x0 = (-(width) / 2);
                    int y0 = -(height + radius);
                    InvoZone txtZone = new InvoZone(x0, width, y0, height);

                    ClientUtil.blitColor(stack, txtZone, blackBg);
                    TextUtil.renderText(stack, message.getText(),  false, 1,
                            txtZone.inflate(-2,-2), TextUtil.txtAlignment.MIDDLE);
                }
            }
            else if (!ClientUtil.mC.player.getUUID().equals(cap.getOtherPlayer())){
                int radius = 20;

                //region Render the revive text
                InvoText message = ReviveScreenEvent.beingRevivedText;
                int txtWidth = ClientUtil.mC.font.width(message.getText());
                int padding = 1;

                int width = txtWidth + (padding * 2);
                int height = (ClientUtil.mC.font.lineHeight + (padding * 2));

                ClientUtil.blitColor(stack, -(width) / 2, width, -(height + radius), height, blackBg);

                height = ClientUtil.mC.font.lineHeight;
                TextUtil.renderText(message.getText(), stack, -txtWidth / 2F, -(height + radius + padding), false);
                //endregion

                int xOrigin = -20;
                int yOrigin = -10;

                //progress bar background
                ClientUtil.blitColor(stack,  xOrigin,
                        xOrigin * 2F,0, yOrigin, bgColor); //prev color: 2302755

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
