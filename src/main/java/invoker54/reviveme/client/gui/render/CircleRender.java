package invoker54.reviveme.client.gui.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.*;
import invoker54.invocore.client.util.ClientUtil;
import invoker54.invocore.client.util.InvoZone;
import invoker54.invocore.common.ModLogger;
import invoker54.reviveme.common.config.ReviveMeConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.jspecify.annotations.Nullable;

public class CircleRender {
    private static final ModLogger LOGGER = ModLogger.getLogger(CircleRender.class, ReviveMeConfig.debugMode);
    //TODO: Clean and reuse circle code

    /**
     * Draw an arc centred around the zero point.  Setup translatef, colour and line width etc before calling.
     * @param radius
     * @param startAngle clockwise starting from 12 O'clock (degrees)
     * @param endAngle (degreesO
     */
    public record CircleRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, float origX, float origY, double radius,
                                    double startAngle, double endAngle, int colorCode,  @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds) implements GuiElementRenderState{
        public CircleRenderState(RenderPipeline pipeline, TextureSetup textureSetup, Matrix3x2f pose, float origX, float origY, double radius, double startAngle, double endAngle, int colorCode, @Nullable ScreenRectangle scissorArea){
            this(pipeline, textureSetup, pose, origX, origY, radius, startAngle, endAngle, colorCode, scissorArea, getBound(origX, origY, radius, pose,scissorArea));
        }

        public static ScreenRectangle getBound(float origX, float origY, double radius, Matrix3x2f pose, ScreenRectangle scissorArea){
            InvoZone boundZone = new InvoZone((float) (origX-(radius/2f)), (float) (radius), (float) (origY-(radius/2f)), (float) (radius));
            ScreenRectangle boundRectangle = boundZone.rect(true).transformMaxBounds(pose);
            return scissorArea != null ? scissorArea.intersection(boundRectangle) : boundRectangle;
        }

        @Override
        public void buildVertices(@NotNull VertexConsumer vertexConsumer) {
//            LOGGER.error("This is running btw");

//            pose.pushPose();
//            Matrix3x2f lastPos = pose;
            //This is how much the angle will increase
            final double angleIncrement = Math.toRadians(5.0);
            //This will flip the direction of the circle
            float direction = (endAngle >= startAngle) ? 1.0F : -1.0F;
            //Delta angle is just the difference between start and end
            double deltaAngle = Math.abs(endAngle - startAngle);
            //This makes it so the difference stays between 360
            deltaAngle %= 360.0;
            //If the difference is 360, this will make it so
            deltaAngle = (deltaAngle == 0 ? 360 : deltaAngle);
            double start = startAngle;
            start = direction < 0 ? endAngle : start;

            //All this did was move the start angle 1 number up or down
            start -= Math.floor(start / 360.0);

            //This converts the numbers into actual angle data
            start = Math.toRadians(start);
            deltaAngle = Math.toRadians(deltaAngle);

            double x, y;
            //How many degrees has been renderer already
            double arcPos = 0;
            boolean arcFinished = false;

            //The coloring of the angle
            float f3 = (float) (colorCode >> 24 & 255) / 255.0F;
            float f = (float) (colorCode >> 16 & 255) / 255.0F;
            float f1 = (float) (colorCode >> 8 & 255) / 255.0F;
            float f2 = (float) (colorCode & 255) / 255.0F;

            //Setting up the render system
//            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
//            RenderSystem.disableCull();
//            RenderSystem.enableBlend();
//            RenderSystem.disableDepthTest();
////        RenderSystem.texture();
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            //Places a point in the middle of the circle only if it isn't going to be a full circle
            if(deltaAngle < 360)
                vertexConsumer.addVertexWith2DPose(this.pose(), origX, origY).setColor(this.colorCode);

            do {
                //Trunc angle is pretty much the current degree we are on. (can't be higher than delta angle)
                double truncAngle = Math.min(arcPos, deltaAngle);
                x = origX + (radius * Math.sin(start + (direction * truncAngle)));
                y = origY + (-radius * Math.cos(start + truncAngle));
//                LOGGER.error("x:" + x + ",y:"+y);
//                LOGGER.error("y:" + x);
                //System.out.println("X Coordinates are: " + String.valueOf(x) + "," + String.valueOf(y));

                vertexConsumer.addVertexWith2DPose(this.pose(), (float) x, (float) y).setColor(this.colorCode);
                //GL11.glVertex3d(x, y, zLevel);

                //if the current angle (arcpos) is greater than or equal to delta angle
                arcFinished = (arcPos >= deltaAngle);
                //Increases the current angle by angleIncrement for the next cycle
                arcPos += angleIncrement;
            } while (!arcFinished && arcPos <= Math.toRadians(360.0)); // arcPos test is a fail safe to prevent infinite loop in case of problem with angle arguments
//            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow())
            return;
        }
    }

    public static void draw2DArc(GuiGraphics graphics, float origX, float origY, double radius,
                                 double startAngle, double endAngle, int colorCode) {
//        LOGGER.warn("Hey, how's it going");
        graphics.submitGuiElementRenderState(
                new CircleRenderState(RenderPipelines.GUI.toBuilder().withCull(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN).build(),
                        TextureSetup.noTexture(), new Matrix3x2f(graphics.pose()), origX, origY, radius/2f, startAngle, endAngle, colorCode, graphics.peekScissorStack()));
    }

    public static void draw3DArc(PoseStack poseStack, float origX, float origY, double radius,
                                 double startAngle, double endAngle, int colorCode) {
        draw3DArc(poseStack, RenderType.create("InvoArc", RenderSetup.builder(RenderPipelines.GUI.toBuilder().withCull(false).withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                        .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_FAN).build()).createRenderSetup()),
                origX, origY, radius, startAngle, endAngle, colorCode);
    }

    public static void draw3DArc(PoseStack poseStack, RenderType renderType, float origX, float origY, double radius,
                                   double startAngle, double endAngle, int colorCode) {
        poseStack.pushPose();
//            poseStack.translate(renderZone.x(), renderZone.y(), 0);
//            poseStack.scale(Math.abs(renderZone.width()), Math.abs(renderZone.height()), 1);
        ClientUtil.getMinecraft().gameRenderer.getSubmitNodeStorage().submitCustomGeometry(poseStack, renderType, new ClientUtil.Custom3DRenderState(
                (vertexConsumer -> {
//            LOGGER.error("This is running btw");

//            pose.pushPose();
//            Matrix3x2f lastPos = pose;
                    //This is how much the angle will increase
                    final double angleIncrement = Math.toRadians(5.0);
                    //This will flip the direction of the circle
                    float direction = (endAngle >= startAngle) ? 1.0F : -1.0F;
                    //Delta angle is just the difference between start and end
                    double deltaAngle = Math.abs(endAngle - startAngle);
                    //This makes it so the difference stays between 360
                    deltaAngle %= 360.0;
                    //If the difference is 360, this will make it so
                    deltaAngle = (deltaAngle == 0 ? 360 : deltaAngle);
                    double start = startAngle;
                    start = direction < 0 ? endAngle : start;

                    //All this did was move the start angle 1 number up or down
                    start -= Math.floor(start / 360.0);

                    //This converts the numbers into actual angle data
                    start = Math.toRadians(start);
                    deltaAngle = Math.toRadians(deltaAngle);

                    double x, y;
                    //How many degrees has been renderer already
                    double arcPos = 0;
                    boolean arcFinished = false;

                    //The coloring of the angle
                    float f3 = (float) (colorCode >> 24 & 255) / 255.0F;
                    float f = (float) (colorCode >> 16 & 255) / 255.0F;
                    float f1 = (float) (colorCode >> 8 & 255) / 255.0F;
                    float f2 = (float) (colorCode & 255) / 255.0F;

                    //Setting up the render system
//            BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
//            RenderSystem.disableCull();
//            RenderSystem.enableBlend();
//            RenderSystem.disableDepthTest();
////        RenderSystem.texture();
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//            RenderSystem.setShader(GameRenderer::getPositionColorShader);

                    //Places a point in the middle of the circle only if it isn't going to be a full circle
                    if(deltaAngle < 360)
                        vertexConsumer.addVertex(poseStack.last(), origX, origY, 0).setColor(colorCode);

                    do {
                        //Trunc angle is pretty much the current degree we are on. (can't be higher than delta angle)
                        double truncAngle = Math.min(arcPos, deltaAngle);
                        x = origX + (radius * Math.sin(start + (direction * truncAngle)));
                        y = origY + (-radius * Math.cos(start + truncAngle));
//                LOGGER.error("x:" + x + ",y:"+y);
//                LOGGER.error("y:" + x);
                        //System.out.println("X Coordinates are: " + String.valueOf(x) + "," + String.valueOf(y));

                        vertexConsumer.addVertex(poseStack.last(), (float) x, (float) y, 0).setColor(colorCode);
                        //GL11.glVertex3d(x, y, zLevel);

                        //if the current angle (arcpos) is greater than or equal to delta angle
                        arcFinished = (arcPos >= deltaAngle);
                        //Increases the current angle by angleIncrement for the next cycle
                        arcPos += angleIncrement;
                    } while (!arcFinished && arcPos <= Math.toRadians(360.0)); // arcPos test is a fail safe to prevent infinite loop in case of problem with angle arguments
//            BufferUploader.drawWithShader(bufferbuilder.buildOrThrow())
                    return;
                })
                , renderType));
        ClientUtil.getMinecraft().gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
        ClientUtil.getMinecraft().renderBuffers().bufferSource().endBatch();
        poseStack.popPose();
    }

    public static void drawArcWorld(PoseStack stack, Vector3d origin, double radius, double startAngle, double endAngle, int colorCode)
    {
        Matrix4f lastPos = stack.last().pose();
        final double angleIncrement = Math.toRadians(5.0);
        float direction = (endAngle >= startAngle) ? -1.0F : 1.0F;
        double deltaAngle = Math.abs(endAngle - startAngle);
        deltaAngle %= 360.0;

        startAngle -= Math.floor(startAngle/360.0);
        startAngle = Math.toRadians(startAngle);
        deltaAngle = Math.toRadians(deltaAngle);

        //GL11.glBegin(GL11.GL_LINE_STRIP);

        double x, y;
        double arcPos = 0;
        boolean arcFinished = false;

        float f3 = (float)(colorCode >> 24 & 255) / 255.0F;
        float f = (float)(colorCode >> 16 & 255) / 255.0F;
        float f1 = (float)(colorCode >> 8 & 255) / 255.0F;
        float f2 = (float)(colorCode & 255) / 255.0F;

//        RenderSystem.disableTexture();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        if(Math.abs(endAngle - startAngle) < 360)
            bufferbuilder.addVertex(lastPos, (float) origin.x, (float) origin.y, (float) origin.z).setColor(f, f1, f2, f3);

        //System.out.println("COORDINATES START ");
        do {
            double truncAngle = Math.min(arcPos, deltaAngle);
            x = origin.x + (radius * Math.sin(startAngle + direction * truncAngle));
            y = origin.y + (-radius * Math.cos(startAngle + direction * truncAngle));
            //System.out.println("X Coordinates are: " + String.valueOf(x) + "," + String.valueOf(y));

            bufferbuilder.addVertex(lastPos, (float) x, (float) y, (float) origin.z).setColor(f, f1, f2, f3);
            //GL11.glVertex3d(x, y, zLevel);

            arcFinished = (arcPos >= deltaAngle);
            arcPos += angleIncrement;
        } while (!arcFinished && arcPos <= Math.toRadians(360.0));      // arcPos test is a fail safe to prevent infinite loop in case of problem with angle arguments
        //System.out.println("COORDINATES STOP ");
//        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
//        RenderSystem.enableTexture();
        //GL11.glEnd();
    }
}
