package invoker54.reviveme.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;

public class ClientUtil {
    public static final Minecraft mC = Minecraft.getInstance();
    public static final TextureManager TEXTURE_MANAGER = mC.textureManager;
    public static final ItemRenderer ITEM_RENDERER = mC.getItemRenderer();
    public static final DecimalFormat d1 = new DecimalFormat("0.0");
    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    //This will face the player dependent on the players position, NOT camera orientation.
    public static void drawWorldLine(MatrixStack stack, Vector3d origin, Vector3d target, float lineWidth, int color){
        stack.pushPose();
        Vector3d cam = mC.gameRenderer.getMainCamera().getPosition().reverse();
        stack.translate(cam.x(), cam.y(), cam.z());
        cam = cam.reverse();
        Matrix4f lastPos = stack.last().pose();

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;
        //This gives me the up/down vector of the plane
        Vector3d directionVector = target.vectorTo(cam).cross(origin.vectorTo(cam)).normalize();

        Vector3d originUP = origin.add(directionVector.scale(lineWidth/2F));
        Vector3d originDOWN = origin.add(directionVector.scale(-lineWidth/2F));
        Vector3d targetUP = target.add(directionVector.scale(lineWidth/2F));
        Vector3d targetDOWN = target.add(directionVector.scale(-lineWidth/2F));

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(lastPos, (float) originUP.x(), (float) originUP.y(), (float) originUP.z()).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float)targetUP.x(), (float)targetUP.y(), (float)targetUP.z()).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float)targetDOWN.x(), (float)targetDOWN.y(), (float)targetDOWN.z()).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float)originDOWN.x(), (float)originDOWN.y(), (float)originDOWN.z()).color(f, f1, f2, f3).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);
        RenderSystem.enableDepthTest();
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        stack.popPose();
    }
    public static void blitImage(MatrixStack stack, float x0, float width, float y0, float height, float u0, float imageWidth, float v0, float imageHeight, float imageScale){
        Matrix4f lastPos = stack.last().pose();
        float x1 = x0 + width;
        float y1 = y0 + height;
        u0 /= imageScale;
        float u1 = u0 + (imageWidth/imageScale);
        v0 /= imageScale;
        float v1 = v0 + (imageHeight/imageScale);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(lastPos, (float)x0, (float)y1, (float)0).uv(u0, v1).endVertex();
        bufferbuilder.vertex(lastPos, (float)x1, (float)y1, (float)0).uv(u1, v1).endVertex();
        bufferbuilder.vertex(lastPos, (float)x1, (float)y0, (float)0).uv(u1, v0).endVertex();
        bufferbuilder.vertex(lastPos, (float)x0, (float)y0, (float)0).uv(u0, v0).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }
    public static void blitImageWorld(MatrixStack stack, Vector3d origin, float scale, float u0, float imageWidth, float v0, float imageHeight, float imageScale){
        Matrix4f lastPos = stack.last().pose();
        float x0 = (float) origin.x() - (scale/2F);
        float x1 = (float) origin.x() + (scale/2F);
        float y0 = (float) origin.y() + (scale/2F);
        float y1 = (float) origin.y() - (scale/2F);
        u0 /= imageScale;
        float u1 = u0 + (imageWidth/imageScale);
        v0 /= imageScale;
        float v1 = v0 + (imageHeight/imageScale);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(lastPos, x0, y1, (float)origin.z()).uv(u0, v1).endVertex();
        bufferbuilder.vertex(lastPos, x1, y1, (float)origin.z()).uv(u1, v1).endVertex();
        bufferbuilder.vertex(lastPos, x1, y0, (float)origin.z()).uv(u1, v0).endVertex();
        bufferbuilder.vertex(lastPos, x0, y0, (float)origin.z()).uv(u0, v0).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);

        RenderSystem.enableDepthTest();
    }
    public static void blitColorWorld(MatrixStack stack, Vector3d origin, float scale, int color){
        Matrix4f lastPos = stack.last().pose();

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;

        float x0 = (float) origin.x() - (scale/2F);
        float x1 = (float) origin.x() + (scale/2F);
        float y0 = (float) origin.y() + (scale/2F);
        float y1 = (float) origin.y() - (scale/2F);

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(lastPos, x0, y1, (float)origin.z()).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, x1, y1, (float)origin.z()).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, x1, y0, (float)origin.z()).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, x0, y0, (float)origin.z()).color(f, f1, f2, f3).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);

        RenderSystem.enableDepthTest();
    }

    public static void blitColor(MatrixStack stack, float x0, float width, float y0, float height, int color){
        Matrix4f lastPos = stack.last().pose();
        float x1 = x0 + width;
        float y1 = y0 + height;

        float f3 = (float)(color >> 24 & 255) / 255.0F;
        float f = (float)(color >> 16 & 255) / 255.0F;
        float f1 = (float)(color >> 8 & 255) / 255.0F;
        float f2 = (float)(color & 255) / 255.0F;

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.vertex(lastPos, (float)x0, (float)y1, (float)0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float)x1, (float)y1, (float)0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float)x1, (float)y0, (float)0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.vertex(lastPos, (float)x0, (float)y0, (float)0).color(f, f1, f2, f3).endVertex();
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    public static boolean inBounds (float xSpot, float ySpot, Bounds bounds){
        if (xSpot < bounds.x0 || xSpot > bounds.x1) return false;
        if (ySpot < bounds.y0 || ySpot > bounds.y1) return false;

        return  true;
    }

    protected static final ArrayList<Bounds> cropBounds = new ArrayList<>();
    public static void beginCrop (double x, double width, double y, double height, boolean fresh){
        if (fresh) cropBounds.add(new Bounds((int) x, (int) width, (int) y, (int) height));
//        Bounds bounds = cropBounds.get(cropBounds.size() - 1);
//        XPShop.LOGGER.debug((String.valueOf(x)) + (bounds.x0));
//        XPShop.LOGGER.debug((String.valueOf(width)) + (bounds.x1 - bounds.x0));
//        XPShop.LOGGER.debug((String.valueOf(y)) + (bounds.y0));
//        XPShop.LOGGER.debug((String.valueOf(height)) + (bounds.y1 - bounds.y0));
        double scale = mC.getWindow().getGuiScale();
        int windowHeight = mC.getWindow().getGuiScaledHeight();

        //This is inverses y since scissor test requires it
        y = windowHeight - (height + y);

//        LOGGER.debug("The y before is: " + y);
//        LOGGER.debug("The height before is: " + height);
        x *= scale;
        y *= scale;
        width *= scale;
        height *= scale;

//        LOGGER.debug("The y is: " + y);
//        LOGGER.debug("The height is: " + height);

        RenderSystem.enableScissor((int) x, (int) y, (int) width, (int) height);
        //LOGGER.debug("Start " + cropBounds.size());
    }

    public static void endCrop(){
        //LOGGER.debug("End " + cropBounds.size());
        if (cropBounds.size() != 0) cropBounds.remove(cropBounds.size() - 1);
        if (!cropBounds.isEmpty()) {
            Bounds cropBound = cropBounds.get(cropBounds.size() - 1);
            beginCrop(cropBound.x0, (cropBound.x1 - cropBound.x0), cropBound.y0, cropBound.y1 - cropBound.y0, false);
        }
        else {
            RenderSystem.disableScissor();
        }
    }

    public static class Bounds{
        int x0;
        int x1;
        int y0;
        int y1;

        public Bounds(int x, int width, int y, int height){
            this.x0 = x;
            this.x1 = x + width;
            this.y0 = y;
            this.y1 = y + height;
        }

        public Bounds(){}

        public void adjustBounds(int x, int width, int y, int height) {
            this.x0 = x;
            this.x1 = x + width;
            this.y0 = y;
            this.y1 = y + height;

        }

        public int getMinX() {
            return x0;
        }
        public int getMaxX() {
        return x1;
        }
        public int getMinY() {
        return y0;
        }
        public int getMaxY() {
        return y1;
        }
    }

    public static class SimpleButton extends Button {

        public boolean hidden = false;

        public SimpleButton(int x, int y, int width, int height, ITextComponent textComponent, IPressable onPress) {
            super(x, y, width, height, textComponent, onPress);
            this.visible = true;
        }

        @Override
        public void renderButton(MatrixStack stack, int xMouse, int yMouse, float partialTicks) {
            if (hidden) return;

            FontRenderer fontrenderer = mC.font;
            TEXTURE_MANAGER.bind(WIDGETS_LOCATION);
            int i = this.getYImage(this.isHovered());
            i = 46 + i * 20;

            //left part of the button
            ClientUtil.blitImage(stack, this.x,  this.width / 2, this.y, this.height,
                    0, this.width / 2f, i, 20, 256);
//            //left part of the button
//            this.blit(stack, this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);

            //right part of the button
            ClientUtil.blitImage(stack, this.x + this.width / 2,  this.width/2, this.y, this.height,
                    200 - (this.width/2), this.width/2, i, 20, 256);
//            //right part of the button
//            this.blit(stack, this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);

            int j = getFGColor();
            drawCenteredString(stack, fontrenderer, this.getMessage(), this.x + this.width / 2, this.y + (this.height - 8) / 2, j | MathHelper.ceil(this.alpha * 255.0F) << 24);
        }
    }
    public static String ticksToTime(int ticks){
        //Each second is 20 ticks
        //each minute is 1200 ticks
        //Each hour is 72000 ticks
        //20 = ticks, 60 = seconds, 60 = minutes
        int hours = ticks/72000;
        ticks -= (hours * 7200);
        int minutes = ticks/1200;
        ticks -= (minutes * 1200);
        int seconds = ticks/20;

        return (hours <= 9 ? "0" : "") + hours + ":" +
                (minutes <= 9 ? "0" : "") + minutes + ":" +
                (seconds <= 9 ? "0" : "") + seconds;
    }

    public static String formatValue(double value) {
        if (value == 0) return "0";

        int power;
        String suffix = " KMBT";
        String formattedNumber = "";

        NumberFormat formatter = new DecimalFormat("#,###.#");
        power = (int)StrictMath.log10(value);
        value = value/(Math.pow(10,(power/3)*3));
        formattedNumber=formatter.format(value);
        formattedNumber = formattedNumber + suffix.charAt(power/3);
        return formattedNumber.length()>4 ?  formattedNumber.replaceAll("\\.[0-9]+", "") : formattedNumber;
    }
}
