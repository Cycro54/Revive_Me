package invoker54.reviveme.client.event;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import invoker54.reviveme.ReviveMe;
import invoker54.reviveme.client.ClientUtil;
import invoker54.reviveme.client.gui.render.CircleRender;
import invoker54.reviveme.common.capability.FallenCapability;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.text.DecimalFormat;

import static invoker54.reviveme.client.ClientUtil.mC;
import static invoker54.reviveme.client.event.FallScreenEvent.*;

@Mod.EventBusSubscriber(modid = ReviveMe.MOD_ID, value = Dist.CLIENT)
public class RenderFallPlateEvent {
    private static final Minecraft inst = Minecraft.getInstance();
    private static final DecimalFormat df = new DecimalFormat("0.0");
    private static final int progCircle = new Color(39, 235, 86, 255).getRGB();
    private static final int blackBg = new Color(0, 0, 0, 255).getRGB();

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
            if (cap.getOtherPlayer() != null) return;

            float f = entity.getBbHeight() * 0.5f;
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

            //Green circular progress
            float origSize = 22;
            float modSize = 40;
            if (ReviveMeConfig.timeLeft == 0 || cap.GetTimeLeft(false) <= 0) CircleRender.drawArc(stack, 0, 0, origSize, 0, 360, progCircle);
            else CircleRender.drawArc(stack, 0, 0, origSize, 0, cap.GetTimeLeft(true) * 360, progCircle);

            //Timer texture
            ClientUtil.TEXTURE_MANAGER.bind(Timer_TEXTURE);
            ClientUtil.blitImage(stack, 0 - (modSize / 2F), modSize, 0 - (modSize / 2F), modSize, 0, 64, 0, 64, 64);
//        ClientUtil.blitImageWorld(stack, pos, 6, 0, 64, 0, 64, 64);
            ClientUtil.TEXTURE_MANAGER.release(Timer_TEXTURE);

            //Revive type background
//        fill(stack, 2, -14, 14, -2, blackBg);
            ClientUtil.blitColor(stack, modSize / 2F, 10, -modSize / 2F, 9, blackBg);
            //Revive type item texture
            switch (cap.getPenaltyType()) {
                case NONE:
                    break;
                case HEALTH:
                    ClientUtil.TEXTURE_MANAGER.bind(HEALTH_TEXTURE);
                    ClientUtil.blitImage(stack, (modSize / 2F) + 1, 8, (-modSize / 2F), 8, 0, 64, 0, 64, 64);
//                ClientUtil.blitImageWorld(stack, pos, 1, 0, 64, 0, 64, 64);
//                blit(stack, 4, -12, 0, 0F, 0F, 8, 8, 8, 8);
                    ClientUtil.TEXTURE_MANAGER.release(HEALTH_TEXTURE);
                    break;
                case EXPERIENCE:
                    ClientUtil.TEXTURE_MANAGER.bind(EXPERIENCE_TEXTURE);
                    ClientUtil.blitImage(stack, (modSize / 2F) + 1, 8, (-modSize / 2F), 8, 0, 16, 0, 16, 16);
//                blit(stack, 4, -12, 0, 0F, 0F, 8, 8, 8, 8);
                    ClientUtil.TEXTURE_MANAGER.release(EXPERIENCE_TEXTURE);
                    break;
                case FOOD:
                    ClientUtil.TEXTURE_MANAGER.bind(FOOD_TEXTURE);
                    ClientUtil.blitImage(stack, (modSize / 2F) + 1, 8, (-modSize / 2F) + 1, 8, 0, 18, 0, 18, 18);
//                blit(stack, 4, -12, 0, 0F, 0F, 8, 8, 8, 8);
                    ClientUtil.TEXTURE_MANAGER.release(FOOD_TEXTURE);
                    break;
            }

            //Penalty txt
            String penaltyAmount = Float.toString(cap.getPenaltyAmount(player));
            int txtWidth = mC.font.width(penaltyAmount);
            int txtHeight = 9;
            int txtColor = (cap.hasEnough(inst.player) ? TextFormatting.GREEN.getColor() : TextFormatting.RED.getColor());
            float scale = (mC.font.width("0") / (float) txtWidth) * 4F;
            scale = Float.parseFloat(df.format(scale));
            stack.scale(scale, scale, scale);

            RenderSystem.disableDepthTest();
//            mC.font.drawShadow(stack, penaltyAmount, -txtWidth/2F, -txtHeight/2F, txtColor, false);
            IRenderTypeBuffer.Impl irendertypebuffer$impl = IRenderTypeBuffer.immediate(Tessellator.getInstance().getBuilder());
            renderText(player, penaltyAmount, stack, irendertypebuffer$impl, 15728880, txtColor, txtWidth, txtHeight, scale);
            irendertypebuffer$impl.endBatch();

            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            stack.popPose();
            //drawInternal(font,penaltyAmount,-txtHalfWidth/2, -9/2, txtColor, false, stack.last().pose(), event.getRenderTypeBuffer(), event.getPackedLight());
        }
    }

    private static void renderText(PlayerEntity player, String text, MatrixStack stack, IRenderTypeBuffer buffer,
                                   int lightcoords, int txtColor, int width, int height, float scale){
        boolean flag = !player.isDiscrete();
        float f = player.getBbHeight() * 0.5f;
        int i = "deadmau5".equals(text) ? -10 : 0;
        Matrix4f matrix4f = stack.last().pose();

        //float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);
        int j = (int)(0 * 255.0F) << 24;
        FontRenderer fontrenderer = inst.font;
        float x = -width/2;
        float y = -height/2;

        fontrenderer.drawInBatch(text, x, y, txtColor, true, matrix4f, buffer, flag, j, lightcoords);
        if (flag) {
            fontrenderer.drawInBatch(text, x, y, txtColor, true, matrix4f, buffer, false, 0, lightcoords);
        }
    }


    public static void testRender(MatrixStack stack, int x, int y, int width, int height, int colorCode){
        Matrix4f lastPos = stack.last().pose();

        if (x > width) {
            int i = x;
            x = width;
            width = i;
        }

        if (y > height) {
            int j = y;
            y = height;
            height = j;
        }

        float f3 = (float)(colorCode >> 24 & 255) / 255.0F;
        float f = (float)(colorCode >> 16 & 255) / 255.0F;
        float f1 = (float)(colorCode >> 8 & 255) / 255.0F;
        float f2 = (float)(colorCode & 255) / 255.0F;

        //System.out.println(f3);
        //System.out.println(f);
        //System.out.println(f1);
        //System.out.println(f2);

        RenderSystem.disableTexture();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        bufferbuilder.vertex(lastPos, (float) x, (float) height, 0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float) width, (float) height, 0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float) width, (float) y, 0F).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float) x, (float) y, 0F).color(f, f1, f2, f3).endVertex();

        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();

    }
}
