package nz.ac.canterbury.seng302.gardenersgrove.utility;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class ImageTools {

    /*
    Code in this class is based on the baeldung tutorial on image resizing
    https://www.baeldung.com/java-resize-image
     */

    /**
     * Resizes an image to the given width and height, cropping it to a square first
     * @param image inputstream (bytes) of the image to resize
     * @param width width of the resized image
     * @param height height of the resized image
     * @param extension file extension of the image
     * @return InputStream of the resized image
     * @throws IOException if the image cannot be read or written
     */
    public InputStream resize(InputStream image, int width, int height, String extension) throws IOException {

        BufferedImage originalImage = ImageIO.read(image);

        int squareSize = Math.min(originalImage.getWidth(), originalImage.getHeight());
        BufferedImage croppedImage = new BufferedImage(squareSize, squareSize, originalImage.getType());
        Graphics2D g = croppedImage.createGraphics();
        g.drawImage(originalImage, 0, 0, squareSize, squareSize, (originalImage.getWidth() - squareSize) / 2, (originalImage.getHeight() - squareSize) / 2, (originalImage.getWidth() + squareSize) / 2, (originalImage.getHeight() + squareSize) / 2, null);
        g.dispose();


        Image resultingImage = croppedImage.getScaledInstance(width, height, Image.SCALE_DEFAULT);
        BufferedImage outputImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        outputImage.getGraphics().drawImage(resultingImage, 0, 0, null);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(outputImage, extension, os);
        return new ByteArrayInputStream(os.toByteArray());
    }
}
