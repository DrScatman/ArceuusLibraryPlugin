package kourendlibrary.data;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.Arrays;

public class ImageUtil {

    /**
     * Reads an image resource from a given path relative to a given class.
     * This method is primarily shorthand for the synchronization and error handling required for
     * loading image resources from classes.
     *
     * @param c    The class to be referenced for resource path.
     * @param path The path, relative to the given class.
     * @return     A {@link BufferedImage} of the loaded image resource from the given path.
     */
    public static BufferedImage getResourceStreamFromClass(final Class c, final String path)
    {
        try
        {
            synchronized (ImageIO.class)
            {
                return ImageIO.read(c.getResourceAsStream(path));
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Offsets an image's alpha component by a given offset.
     *
     * @param image  The image to be made more or less transparent.
     * @param offset A signed 8-bit integer value to modify the image's alpha component with.
     *               Values above 0 will increase transparency, and values below 0 will decrease
     *               transparency.
     * @return       The given image with its alpha component adjusted by the given offset.
     */
    public static BufferedImage alphaOffset(final BufferedImage image, final int offset)
    {
        final float offsetFloat = (float) offset;
        final int numComponents = image.getColorModel().getNumComponents();
        final float[] scales = new float[numComponents];
        final float[] offsets = new float[numComponents];

        Arrays.fill(scales, 1f);
        Arrays.fill(offsets, 0f);
        offsets[numComponents - 1] = offsetFloat;
        return offset(image, scales, offsets);
    }

    /**
     * Performs a rescale operation on the image's color components.
     *
     * @param image   The image to be adjusted.
     * @param scales  An array of scale operations to be performed on the image's color components.
     * @param offsets An array of offset operations to be performed on the image's color components.
     * @return        The modified image after applying the given adjustments.
     */
    private static BufferedImage offset(final BufferedImage image, final float[] scales, final float[] offsets)
    {
        return new RescaleOp(scales, offsets, null).filter(image, null);
    }
}
