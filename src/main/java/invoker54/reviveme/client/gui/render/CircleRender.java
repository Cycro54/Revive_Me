package invoker54.reviveme.client.gui.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;

public class CircleRender {

    /**
     * Draw an arc centred around the zero point.  Setup translatef, colour and line width etc before calling.
     * @param radius
     * @param startAngle clockwise starting from 12 O'clock (degrees)
     * @param endAngle (degreesO
     */
    public static void drawArc(MatrixStack stack, int origX, int origY, double radius, double startAngle, double endAngle, int colorCode)
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
            bufferbuilder.vertex(lastPos, origX, origY, 0).color(f, f1, f2, f3).endVertex();

        //System.out.println("COORDINATES START ");
        do {
            double truncAngle = Math.min(arcPos, deltaAngle);
            x = origX + (radius * Math.sin(startAngle + direction * truncAngle));
            y = origY + (-radius * Math.cos(startAngle + direction * truncAngle));
            //System.out.println("X Coordinates are: " + String.valueOf(x) + "," + String.valueOf(y));

            bufferbuilder.vertex(lastPos, (float) x, (float) y, 0).color(f, f1, f2, f3).endVertex();
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
