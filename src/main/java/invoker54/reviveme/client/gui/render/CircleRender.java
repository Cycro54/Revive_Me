package invoker54.reviveme.client.gui.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;

public class CircleRender {

    /**
     * Draw an arc centred around the zero point.  Setup translatef, colour and line width etc before calling.
     * @param radius
     * @param startAngle clockwise starting from 12 O'clock (degrees)
     * @param endAngle (degreesO
     */
    public static void drawArc(MatrixStack stack, float origX, float origY, double radius, double startAngle, double endAngle, int colorCode)
    {
        stack.pushPose();
        Matrix4f lastPos = stack.last().pose();
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
        startAngle = direction < 0 ? endAngle : startAngle;

        //All this did was move the start angle 1 number up or down
        startAngle -= Math.floor(startAngle / 360.0);

        //This converts the numbers into actual angle data
        startAngle = Math.toRadians(startAngle);
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
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1F,1F,1F,1F);
        bufferbuilder.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);

        //Places a point in the middle of the circle only if it isn't going to be a full circle
        if(deltaAngle < 360)
            bufferbuilder.vertex(lastPos, origX, origY, 0).color(f, f1, f2, f3).endVertex();

        do {
            //Trunc angle is pretty much the current degree we are on. (can't be higher than delta angle)
            double truncAngle = Math.min(arcPos, deltaAngle);
            x = origX + (radius * Math.sin(startAngle + (direction * truncAngle)));
            y = origY + (-radius * Math.cos(startAngle + truncAngle));
            //System.out.println("X Coordinates are: " + String.valueOf(x) + "," + String.valueOf(y));

            bufferbuilder.vertex(lastPos, (float) x, (float) y, 0).color(f, f1, f2, f3).endVertex();
            //GL11.glVertex3d(x, y, zLevel);

            //if the current angle (arcpos) is greater than or equal to delta angle
            arcFinished = (arcPos >= deltaAngle);
            //Increases the current angle by angleIncrement for the next cycle
            arcPos += angleIncrement;
        } while (!arcFinished && arcPos <= Math.toRadians(360.0)); // arcPos test is a fail safe to prevent infinite loop in case of problem with angle arguments
        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        stack.popPose();

//        Matrix4f lastPos = stack.last().pose();
//        final double angleIncrement = Math.toRadians(5.0);
//        float direction = (endAngle >= startAngle) ? -1.0F : 1.0F;
//        double deltaAngle = Math.abs(endAngle - startAngle);
//        deltaAngle %= 360.0;
//
//        startAngle -= Math.floor(startAngle/360.0);
//        startAngle = Math.toRadians(startAngle);
//        deltaAngle = Math.toRadians(deltaAngle);
//
//        //GL11.glBegin(GL11.GL_LINE_STRIP);
//
//        double x, y;
//        double arcPos = 0;
//        boolean arcFinished = false;
//
//        float f3 = (float)(colorCode >> 24 & 255) / 255.0F;
//        float f = (float)(colorCode >> 16 & 255) / 255.0F;
//        float f1 = (float)(colorCode >> 8 & 255) / 255.0F;
//        float f2 = (float)(colorCode & 255) / 255.0F;
//
//        RenderSystem.disableTexture();
//        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
//        bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);
//
//        if(Math.abs(endAngle - startAngle) < 360)
//            bufferbuilder.vertex(lastPos, origX, origY, 0).color(f, f1, f2, f3).endVertex();
//
//        //System.out.println("COORDINATES START ");
//        do {
//            double truncAngle = Math.min(arcPos, deltaAngle);
//            x = origX + (radius * Math.sin(startAngle + direction * truncAngle));
//            y = origY + (-radius * Math.cos(startAngle + direction * truncAngle));
//            //System.out.println("X Coordinates are: " + String.valueOf(x) + "," + String.valueOf(y));
//
//            bufferbuilder.vertex(lastPos, (float) x, (float) y, 0).color(f, f1, f2, f3).endVertex();
//            //GL11.glVertex3d(x, y, zLevel);
//
//            arcFinished = (arcPos >= deltaAngle);
//            arcPos += angleIncrement;
//        } while (!arcFinished && arcPos <= Math.toRadians(360.0));      // arcPos test is a fail safe to prevent infinite loop in case of problem with angle arguments
//        //System.out.println("COORDINATES STOP ");
//
//        bufferbuilder.end();
//        WorldVertexBufferUploader.end(bufferbuilder);
//        RenderSystem.enableTexture();
//        //GL11.glEnd();
    }

    public static void drawArcWorld(MatrixStack stack, Vector3d origin, double radius, double startAngle, double endAngle, int colorCode)
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

        RenderSystem.disableTexture();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);

        if(Math.abs(endAngle - startAngle) < 360)
            bufferbuilder.vertex(lastPos, (float) origin.x(), (float) origin.y(), (float) origin.z()).color(f, f1, f2, f3).endVertex();

        //System.out.println("COORDINATES START ");
        do {
            double truncAngle = Math.min(arcPos, deltaAngle);
            x = origin.x() + (radius * Math.sin(startAngle + direction * truncAngle));
            y = origin.y() + (-radius * Math.cos(startAngle + direction * truncAngle));
            //System.out.println("X Coordinates are: " + String.valueOf(x) + "," + String.valueOf(y));

            bufferbuilder.vertex(lastPos, (float) x, (float) y, (float) origin.z()).color(f, f1, f2, f3).endVertex();
            //GL11.glVertex3d(x, y, zLevel);

            arcFinished = (arcPos >= deltaAngle);
            arcPos += angleIncrement;
        } while (!arcFinished && arcPos <= Math.toRadians(360.0));      // arcPos test is a fail safe to prevent infinite loop in case of problem with angle arguments
        //System.out.println("COORDINATES STOP ");

        bufferbuilder.end();
        WorldVertexBufferUploader.end(bufferbuilder);
        RenderSystem.enableTexture();
        //GL11.glEnd();
    }
}
