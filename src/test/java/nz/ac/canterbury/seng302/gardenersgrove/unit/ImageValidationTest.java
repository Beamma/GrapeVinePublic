package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.validation.ImageValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ImageValidationTest {

    /**
     * This class contains all the processing for the images in the TestImages folder so they can
     * be used in other tests.
     */


    // Mock Svg Image Too Big
    static String svgTooBigFilePath = "src/test/resources/test-images/svg_too_big.svg";
    static Path svgTooBigPath = Paths.get(svgTooBigFilePath);
    static String svgTooBigName = "svg_too_big";
    static String svgTooBigOriginalFileName = "svg_too_big.svg";
    static String svgTooBigContentType = "image/svg+xml";
    static byte[] svgTooBigContent;
    static {
        try {
            svgTooBigContent = Files.readAllBytes(svgTooBigPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MultipartFile mockSvgTooBig = new MockMultipartFile(svgTooBigName,
            svgTooBigOriginalFileName,
            svgTooBigContentType,
            svgTooBigContent);

    // Mock Svg Image Valid

    static String svgValidFilePath = "src/test/resources/test-images/svg_valid.svg";
    static Path svgValidPath = Paths.get(svgValidFilePath);
    static String svgValidName = "svg_valid";
    static String svgValidOriginalFileName = "svg_valid.svg";
    static String svgValidContentType = "image/svg+xml";
    static byte[] svgValidContent;
    static {
        try {
            svgValidContent = Files.readAllBytes(svgValidPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MultipartFile mockSvgValid = new MockMultipartFile(svgValidName,
            svgValidOriginalFileName,
            svgValidContentType,
            svgValidContent);

    // Mock Jpeg Image Too Big

    static String jpegTooBigFilePath = "src/test/resources/test-images/jpg_too_big.jpg";
    static Path jpegTooBigPath = Paths.get(jpegTooBigFilePath);
    static String jpegTooBigName = "jpg_too_big";
    static String jpegTooBigOriginalFileName = "jpeg_too_big.jpg";
    static String jpegTooBigContentType = "image/jpg";
    static byte[] jpegTooBigContent;
    static {
        try {
            jpegTooBigContent = Files.readAllBytes(jpegTooBigPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MultipartFile mockJpegTooBig = new MockMultipartFile(jpegTooBigName,
            jpegTooBigOriginalFileName,
            jpegTooBigContentType,
            jpegTooBigContent);

    // Mock Jpeg Image Valid

    String jpegValidFilePath = "src/test/resources/test-images/jpg_valid.jpg";
    Path jpegValidPath = Paths.get(jpegValidFilePath);
    static String jpegValidName = "jpg_valid";
    static String jpegValidOriginalFileName = "jpeg_valid.jpg";
    static String jpegValidContentType = "image/jpeg";
    static byte[] jpegValidContent;
    {
        try {
            jpegValidContent = Files.readAllBytes(jpegValidPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MultipartFile mockJpegValid = new MockMultipartFile(jpegValidName,
            jpegValidOriginalFileName,
            jpegValidContentType,
            jpegValidContent);

    // Mock Png Image Too Big

    String pngTooBigFilePath = "src/test/resources/test-images/png_too_big.png";
    Path pngTooBigPath = Paths.get(pngTooBigFilePath);
    String pngTooBigName = "png_too_big";
    String pngTooBigOriginalFileName = "png_too_big.png";
    String pngTooBigContentType = "image/png";
    byte[] pngTooBigContent;
    {
        try {
            pngTooBigContent = Files.readAllBytes(pngTooBigPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MultipartFile mockPngTooBig = new MockMultipartFile(pngTooBigName,
            pngTooBigOriginalFileName,
            pngTooBigContentType,
            pngTooBigContent);

    // Mock Png Image Valid

    String pngValidFilePath = "src/test/resources/test-images/png_valid.png";
    Path pngValidPath = Paths.get(pngValidFilePath);
    String pngValidName = "png_valid";
    String pngValidOriginalFileName = "png_valid.png";
    String pngValidContentType = "image/png";
    byte[] pngValidContent;
    {
        try {
            pngValidContent = Files.readAllBytes(pngValidPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MultipartFile mockPngValid = new MockMultipartFile(pngValidName,
            pngValidOriginalFileName,
            pngValidContentType,
            pngValidContent);

    // Mock Gif Image Too Big

    String gifTooBigFilePath = "src/test/resources/test-images/gif_too_big.gif";
    Path gifTooBigPath = Paths.get(gifTooBigFilePath);
    String gifTooBigName = "gif_too_big";
    String gifTooBigOriginalFileName = "gif_too_big.gif";
    String gifTooBigContentType = "image/gif";
    byte[] gifTooBigContent;
    {
        try {
            gifTooBigContent = Files.readAllBytes(gifTooBigPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MultipartFile mockGifTooBig = new MockMultipartFile(gifTooBigName,
            gifTooBigOriginalFileName,
            gifTooBigContentType,
            gifTooBigContent);

    // Mock Gif Image Valid

    static String gifValidFilePath = "src/test/resources/test-images/gif_valid.gif";
    static Path gifValidPath = Paths.get(gifValidFilePath);
    static String gifValidName = "gif_valid";
    static String gifValidOriginalFileName = "gif_valid.gif";
    static String gifValidContentType = "image/gif";
    static byte[] gifValidContent;
    static {
        try {
            gifValidContent = Files.readAllBytes(gifValidPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MultipartFile mockGifValid = new MockMultipartFile(gifValidName,
            gifValidOriginalFileName,
            gifValidContentType,
            gifValidContent);






    @Test
    public void SvgImageIsTooBigTest() {
        Assertions.assertFalse(ImageValidator.isImageSizeValid(mockSvgTooBig));
    }

    @Test
    public void SvgImageIsValidTest() {
        Assertions.assertTrue(ImageValidator.isImageSizeValid(mockSvgValid));
        Assertions.assertTrue(ImageValidator.isImageTypeCorrect(mockSvgValid));
    }

    @Test
    public void JpegImageIsTooBigTest() {
        Assertions.assertFalse(ImageValidator.isImageSizeValid(mockJpegTooBig));
    }

    @Test
    public void JpegImageIsValidTest() {
        Assertions.assertTrue(ImageValidator.isImageSizeValid(mockJpegValid));
        Assertions.assertTrue(ImageValidator.isImageTypeCorrect(mockJpegValid));
    }

    @Test
    public void PngImageIsTooBigTest() {
        Assertions.assertFalse(ImageValidator.isImageSizeValid(mockPngTooBig));
    }

    @Test
    public void PngImageIsValidTest() {
        Assertions.assertTrue(ImageValidator.isImageSizeValid(mockPngValid));
        Assertions.assertTrue(ImageValidator.isImageTypeCorrect(mockPngValid));
    }

    @Test
    public void GifImageIsTooBigTest() {
        Assertions.assertFalse(ImageValidator.isImageSizeValid(mockGifTooBig));
    }

    @Test
    public void GifImageIsValidTest() {
        Assertions.assertTrue(ImageValidator.isImageSizeValid(mockGifValid));
        Assertions.assertFalse(ImageValidator.isImageTypeCorrect(mockGifValid));
    }


}
