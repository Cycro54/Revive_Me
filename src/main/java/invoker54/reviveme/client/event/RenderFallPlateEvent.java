package invoker54.reviveme.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoText;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.client.util.TextUtil;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.VanillaKeybindHandler;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.InvoTextFormat;
import invoker54.reviveme.common.capability.FallenData;
import invoker54.reviveme.common.config.ReviveMeConfig;
import invoker54.reviveme.common.data.ReviveItemData;
import invoker54.reviveme.init.KeyInit;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.text.DecimalFormat;

import static invoker54.reviveme.client.event.FallScreenEvent.timerIMG;
import static invoker54.reviveme.client.event.ReviveScreenEvent.bgColor;
import static invoker54.reviveme.client.event.ReviveScreenEvent.progressColor;

@EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class RenderFallPlateEvent {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final DecimalFormat df = new DecimalFormat("0.0");
    public static final int greenProgCircle = new Color(39, 235, 86, 255).getRGB();
    public static final int redProgCircle = new Color(173, 17, 17, 255).getRGB();
    public static final int goldProgCircle = new Color(227, 175, 7,244).getRGB();
    public static final int blackBg = new Color(0, 0, 0, 176).getRGB();

    @SubscribeEvent
    public static void renderWorldFallTimer(RenderLevelStageEvent.AfterWeather event) {
//        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mC = ClientUtil.getMinecraft();
        boolean iAmFallen = FallenData.get(ClientUtil.getPlayer()).isFallen();

        for (Entity entity : ClientUtil.getMinecraft().level.entitiesForRendering()) {
            if (!(entity instanceof Player player)) continue;
            if (entity.equals(mC.player)) continue;
            float distance = entity.distanceTo(mC.player);
            double maxDistance = Math.max(ReviveMeConfig.reviveGlowMaxDistance, ReviveMeConfig.deathTimerMaxDistance);
            if (distance > maxDistance) continue;

            FallenData cap = FallenData.get(player);
            PoseStack stack = event.getPoseStack();

            if (!cap.isFallen()) continue;

            HitResult rayResult = mC.player.level().clip(
                    new ClipContext(mC.player.getEyePosition(1.0F), entity.getEyePosition(1.0F)
                            , ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
            boolean targetSeen = rayResult.getType() == HitResult.Type.MISS;

            if (distance > 10 && !cap.isCallingForHelp() && !targetSeen) continue;
            if (!targetSeen && entity.getTeam() != ClientUtil.getPlayer().getTeam()) continue;

            float yOffset = entity.getBbHeight() * 0.40f;
            float sizeOffset = 0.5F;

            if (distance > 10) {
                float distanceOffset = (distance - 10);
                yOffset = (entity.getBbHeight() * 1.4f) + (distanceOffset * 0.03f);
                sizeOffset += (distanceOffset * (0.5f / 10f));
            }

            stack.pushPose();

//            RenderSystem.disableCull();
//            RenderSystem.enableBlend();
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.disableDepthTest();

            //Getting into position
            Vec3 difference = entity.position().subtract(mC.gameRenderer.getMainCamera().position());
            stack.translate(difference.x, difference.y + yOffset, difference.z);
            stack.mulPose(mC.getEntityRenderDispatcher().camera.rotation());
            stack.scale(0.025F, -0.025F, 0.025F);
            stack.scale(sizeOffset, sizeOffset, sizeOffset);

            if (cap.getOtherPlayer() == null) {
                int radius = 22;
                InvoText chosenText = null;
                int chosenColor = 0;
                boolean canRender = false;

                if (!mC.player.isShiftKeyDown() && !player.isDeadOrDying()) {
                    canRender = true;
                    chosenColor = greenProgCircle;

                    float seconds = cap.getTimeLeft(false);
                    seconds += (seconds <= 0 ? 0 : 1);
                    if (ReviveMeConfig.timeLeft == 0) chosenText = InvoText.literal("INF");
                    else if (seconds > 0) chosenText = InvoText.literal(Integer.toString((int) seconds));
                    else chosenText = InvoText.literal("RIP");
                    chosenText.withStyle(true, InvoTextFormat.filter( ChatFormatting.BOLD))
                            .withStyle(false, InvoTextFormat.filter( cap.hasEnough(ClientUtil.getPlayer()) ? ChatFormatting.GREEN : ChatFormatting.RED));
                } else if (ClientUtil.getMinecraft().player.isShiftKeyDown() || player.isDeadOrDying()) {
                    canRender = true;
                    chosenColor = redProgCircle;

                    chosenText = InvoText.literal(Integer.toString((int) Math.ceil(cap.getKillTime(false))))
                            .withStyle(true, InvoTextFormat.filter( ChatFormatting.BOLD, ChatFormatting.RED));
                }

                if (canRender && distance < ReviveMeConfig.deathTimerMaxDistance) {
                    float endAngle = 360;
                    if (ClientUtil.getPlayer().isShiftKeyDown()) {
                        endAngle = endAngle * (cap.getKillTime(true));
                    } else if (ReviveMeConfig.timeLeft != 0) endAngle *= Math.max(0, cap.getTimeLeft(true));

                    //Overheal thing
                    CircleRender.draw3DArc(stack, 0, 0, radius + 2, 0,
                            Math.max(0.001D,cap.getOverhealPercentage() * 360), goldProgCircle);

                    if (cap.getTimeLeft(false) <= 0)
                        CircleRender.draw3DArc(stack, 0, 0, radius, 0, endAngle, chosenColor);
                    else CircleRender.draw3DArc(stack, 0, 0, radius, 0, endAngle, chosenColor);

                    InvoZone timerZone = timerIMG.getRenderZone();
                    timerZone.setHeight(40).setWidth(40);
                    timerZone.centerX(0).centerY(0);
                    timerIMG.render(stack);

                    TextUtil.render3DText(stack, chosenText.getText(), false, 1,
                            timerZone.inflate(-timerZone.width() / 4, -timerZone.height() / 4), TextUtil.txtAlignment.MIDDLE);
                }

                InvoText message = null;
                if (mC.crosshairPickEntity == player && !player.isDeadOrDying() && !iAmFallen) {
                    if (mC.player.isShiftKeyDown()) {
                        if (cap.getKillTime(false) > 0) {
                            message = InvoText.translate("revive_me.fall_plate.cant_kill");
                        } else {
                            message = InvoText.translate("revive_me.fall_plate.kill").setArgs(
                                    InvoText.literal(VanillaKeybindHandler.getKey(ClientUtil.getMinecraft().options.keyAttack).getDisplayName().getString())
                                            .withStyle(true, InvoTextFormat.filter( ChatFormatting.YELLOW, ChatFormatting.BOLD)).getText()
                            );
                        }
                    } else if (cap.hasEnough(mC.player)) {
                        message = InvoText.translate("revive_me.fall_plate.revive").setArgs(
                                InvoText.literal(VanillaKeybindHandler.getKey(KeyInit.rightOption.keyBind).getDisplayName().getString())
                                        .withStyle(true, InvoTextFormat.filter( ChatFormatting.YELLOW, ChatFormatting.BOLD)).getText()
                        );

                    }
                }



                if (message == null && cap.isCallingForHelp()) {
                    InvoText randomText = InvoText.literal("ABBA").withStyle(true, InvoTextFormat.filter(ChatFormatting.BOLD, ChatFormatting.RED, ChatFormatting.OBFUSCATED));
                    InvoText helpText = InvoText.literal("")
                            .append(InvoText.literal(" [").withStyle(true, InvoTextFormat.filter( ChatFormatting.BOLD)).getText())
                            .append(InvoText.translate("revive_me.call_for_help").withStyle(true, InvoTextFormat.filter( ChatFormatting.BOLD, ChatFormatting.GOLD)))
                            .append(InvoText.literal("] ").withStyle(true, InvoTextFormat.filter( ChatFormatting.BOLD)));

                    int txtWidth = mC.font.width(randomText.deepCopy().append(helpText).append(randomText).getText());
                    int padding = 2;
                    int width = txtWidth + (padding * 2);
                    int height = (mC.font.lineHeight + (padding * 2));
                    int x0 = (-(width) / 2);
                    int y0 = -(height + radius);
                    InvoZone txtZone = new InvoZone(x0, width, y0, height);
                    if (distance > ReviveMeConfig.deathTimerMaxDistance) txtZone.setY(0);

                    ClientUtil.blit3DColor(stack, txtZone, blackBg);
                    txtZone.inflate(-2,-3);
                    TextUtil.render3DText(stack, randomText.getText(), false, 1, txtZone, TextUtil.txtAlignment.LEFT);
                    TextUtil.render3DText(stack, helpText.getText(), false, 1, txtZone, TextUtil.txtAlignment.MIDDLE);
                    TextUtil.render3DText(stack, randomText.getText(), false, 1, txtZone, TextUtil.txtAlignment.RIGHT);
                }

                else if (message != null) {
                    int txtWidth = mC.font.width(message.getText());
                    int padding = 2;
                    int width = txtWidth + (padding * 2);
                    int height = (mC.font.lineHeight + (padding * 2));
                    int x0 = (-(width) / 2);
                    int y0 = -(height + radius);
                    InvoZone txtZone = new InvoZone(x0, width, y0, height);
                    if (distance > ReviveMeConfig.deathTimerMaxDistance) txtZone.setY(0);

                    ClientUtil.blit3DColor(stack, txtZone, blackBg);
//                    LOGGER.error("scale factor? ");
                    TextUtil.render3DText(stack, message.getText(), false, 1,
                            txtZone.inflate(-2, -2), TextUtil.txtAlignment.MIDDLE);
                }
            } else if (!mC.player.getUUID().equals(cap.getOtherPlayer()) && distance < ReviveMeConfig.deathTimerMaxDistance) {
                int radius = 20;
                Player reviver = ClientUtil.getMinecraft().player.level().getPlayerByUUID(cap.getOtherPlayer());
                boolean hasReviveItem = ReviveItemData.getData(reviver.getMainHandItem(), ReviveItemData.USER.REVIVER) != null;

                //region Render the revive text
                InvoText message = ReviveScreenEvent.beingRevivedText;
                if (hasReviveItem) message = ReviveScreenEvent.useItemText;
                int txtWidth = mC.font.width(message.getText());
                int padding = 1;

                int width = txtWidth + (padding * 2);
                int height = (mC.font.lineHeight + (padding * 2));
                InvoZone colorZone = new InvoZone(-(width) / 2f, width, -(height + radius), height);
                ClientUtil.blit3DColor(stack, colorZone, blackBg);

                height = mC.font.lineHeight;
                InvoZone helpZone = new InvoZone(-txtWidth / 2F, txtWidth, -(height + radius + padding), 12);
                TextUtil.render3DText(stack, message.getText(), false, 1, helpZone, TextUtil.txtAlignment.MIDDLE);
                //endregion

                //progress bar background
                InvoZone progressZone = new InvoZone(0, 40, -5, 10);
                progressZone.centerX(0);
                ClientUtil.blit3DColor(stack, progressZone, bgColor); //prev color: 2302755

                float progress = cap.getProgress(true);
                progressZone.inflate(-1, -1).setWidth(progressZone.width() * progress);
                progressZone.centerX(0);

                //System.out.println(progress);

                //Actual progress bar
                ClientUtil.blit3DColor(stack, progressZone, progressColor);
            }
            stack.popPose();
        }
        ClientUtil.getMinecraft().gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
    }
}
