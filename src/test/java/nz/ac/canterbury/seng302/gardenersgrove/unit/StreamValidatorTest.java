package nz.ac.canterbury.seng302.gardenersgrove.unit;

import nz.ac.canterbury.seng302.gardenersgrove.dto.LiveStreamDTO;
import nz.ac.canterbury.seng302.gardenersgrove.dto.ValidationExceptionDTO;
import nz.ac.canterbury.seng302.gardenersgrove.validation.StreamValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

class StreamValidatorTest {

    LiveStreamDTO streamDTO;
    StreamValidator streamValidator;
    MultipartFile mockImageValidContentType;
    MultipartFile mockImageInvalidContentType;

    // Set up mock images
    static String svgValidFilePath = "src/test/resources/test-images/svg_valid.svg";
    static Path svgValidPath = Paths.get(svgValidFilePath);
    static String svgValidName = "svg_valid";
    static String svgValidOriginalFileName = "svg_valid.svg";
    static String svgInvalidContentType = "svvggg";

    static byte[] svgValidContent;
    static {
        try {
            svgValidContent = Files.readAllBytes(svgValidPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//    private static MultipartFile mockSvgInvalidType = new MockMultipartFile(
//            svgValidName,
//            svgValidOriginalFileName,
//            svgInvalidContentType,
//            svgValidContent);

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

    // Image Valid Size OK

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

    @BeforeEach
    void setUp() {
        streamDTO = new LiveStreamDTO("Title", "Description");
        streamValidator = new StreamValidator();
    }

    @Test
    void allFieldsValid_ReturnsNoExceptions() {
        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @Test
    void emptyTitle_ReturnsTitleLengthException() {
        String expectedField = "title";
        String expectedCode = "401";
        String expectedMessage = "Title must be 256 characters or less and contain some text";

        streamDTO.setTitle("");

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
                );
    }

    @Test
    void titleTooLong_ReturnsTitleLengthException() {
        String expectedField = "title";
        String expectedCode = "401";
        String expectedMessage = "Title must be 256 characters or less and contain some text";

        streamDTO.setTitle("a".repeat(257));

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
                );
    }

    @Test
    void titleLength1_ReturnsNoErrors() {
        streamDTO.setTitle("a");

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @Test
    void titleLength256_ReturnsNoErrors() {
        streamDTO.setTitle("a".repeat(256));

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"#Dab", "'hello'", "\"right", "Plants: The sequel", "Yo!", "Sup, man", "Bro...", "Ca$h", "abc123"})
    void titleValidSpecialChars_ReturnsNoErrors(String title) {
        streamDTO.setTitle(title);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Māori", "Müller"})
    void titleValidMacrons_ReturnsNoErrors(String title) {
        streamDTO.setTitle(title);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a\uD83D\uDE2D", "a\uD83E\uDD4C", "a\uD83C\uDDE6\uD83C\uDDF6", "a\uD83D\uDE00", "a\uD83D\uDE02", "a❤", "a\uD83C\uDF89", "a\uD83C\uDF0D"})
    void titleValidWithEmojis_ReturnsNoErrors(String title) {
        streamDTO.setTitle(title);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"#", "''", "\"", ":", "!", ",", "...", "$", "123"})
    void titleOnlySpecialChars_ReturnsTitleLengthException(String title) {
        String expectedField = "title";
        String expectedCode = "401";
        String expectedMessage = "Title must be 256 characters or less and contain some text";

        streamDTO.setTitle(title);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"\uD83D\uDE2D", "\uD83E\uDD4C", "\uD83C\uDDE6\uD83C\uDDF6", "\uD83D\uDE00", "\uD83D\uDE02", "❤", "\uD83C\uDF89", "\uD83C\uDF0D"})
    void titleOnlyWithEmojis_ReturnsTitleLengthException(String title) {
        String expectedField = "title";
        String expectedCode = "401";
        String expectedMessage = "Title must be 256 characters or less and contain some text";

        streamDTO.setTitle(title);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }
    @ParameterizedTest
    @ValueSource(strings = {" ", "\t", "\n"})
    void titleBlank_ReturnsTitleLengthException(String title) {
        String expectedField = "title";
        String expectedCode = "401";
        String expectedMessage = "Title must be 256 characters or less and contain some text";

        streamDTO.setTitle(title);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"@aaa", "%ff", "a=b", "&me", "(aside)", "e/", "~ e"})
    void titleContainsInvalidSpecialChars_ReturnsTitleInvalidException(String title) {
        String expectedField = "title";
        String expectedCode = "401";
        String expectedMessage = "Title must only include alphanumeric characters, spaces, emojis and #, ', \", :, !, ,, ., $, ?, -";

        streamDTO.setTitle(title);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }

    // Description

    @Test
    void emptyDescription_ReturnsDescriptionLengthException() {
        String expectedField = "description";
        String expectedCode = "401";
        String expectedMessage = "Description must be 512 characters or less and contain some text";

        streamDTO.setDescription("");

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
                );
    }

    @Test
    void descriptionTooLong_ReturnsDescriptionLengthException() {
        String expectedField = "description";
        String expectedCode = "401";
        String expectedMessage = "Description must be 512 characters or less and contain some text";

        streamDTO.setDescription("a".repeat(513));

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
                );
    }

    @Test
    void descriptionLength1_ReturnsNoErrors() {
        streamDTO.setDescription("a");

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @Test
    void descriptionLength512_ReturnsNoErrors() {
        streamDTO.setDescription("a".repeat(512));

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"#Dab", "'hello'", "\"right", "Plants: The sequel", "Yo!", "Sup, man", "Bro...", "Ca$h", "abc123"})
    void descriptionValidSpecialChars_ReturnsNoErrors(String description) {
        streamDTO.setDescription(description);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Māori", "Müller"})
    void descriptionValidMacrons_ReturnsNoErrors(String description) {
        streamDTO.setDescription(description);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a\uD83D\uDE2D", "a\uD83E\uDD4C", "a\uD83C\uDDE6\uD83C\uDDF6", "a\uD83D\uDE00", "a\uD83D\uDE02", "a❤", "a\uD83C\uDF89", "a\uD83C\uDF0D"})
    void descriptionValidWithEmojis_ReturnsNoErrors(String description) {
        streamDTO.setDescription(description);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"#", "''", "\"", ":", "!", ",", "...", "$", "123"})
    void descriptionOnlySpecialChars_ReturnsDescriptionLengthException(String description) {
        String expectedField = "description";
        String expectedCode = "401";
        String expectedMessage = "Description must be 512 characters or less and contain some text";

        streamDTO.setDescription(description);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"\uD83D\uDE2D", "\uD83E\uDD4C", "\uD83C\uDDE6\uD83C\uDDF6", "\uD83D\uDE00", "\uD83D\uDE02", "❤", "\uD83C\uDF89", "\uD83C\uDF0D"})
    void descriptionOnlyWithEmojis_ReturnsDescriptionLengthException(String description) {
        String expectedField = "description";
        String expectedCode = "401";
        String expectedMessage = "Description must be 512 characters or less and contain some text";

        streamDTO.setDescription(description);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }
    @ParameterizedTest
    @ValueSource(strings = {" ", "\t", "\n"})
    void descriptionBlank_ReturnsDescriptionLengthException(String description) {
        String expectedField = "description";
        String expectedCode = "401";
        String expectedMessage = "Description must be 512 characters or less and contain some text";

        streamDTO.setDescription(description);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"@aaa", "%ff", "a=b", "&me", "(aside)", "e/", "~ e"})
    void descriptionContainsInvalidSpecialChars_ReturnsDescriptionInvalidException(String description) {
        String expectedField = "description";
        String expectedCode = "401";
        String expectedMessage = "Description must only include alphanumeric characters, spaces, emojis and #, ', \", :, !, ,, ., $, ?, -";

        streamDTO.setDescription(description);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);
        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }

    @Test
    void thumbnailImgIsInvalidSize_ImgTooBig_returnsImageSizeInvalidException() {
        String expectedField = "image";
        String expectedCode = "401";
        String expectedMessage = "Image must be smaller than 10MB";

        streamDTO.setImage(mockSvgTooBig);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }
    @Test
    void thumbnailImgIsValidSize_ImgSizeValid_returnsNoExceptions() {
        streamDTO.setImage(mockPngValid);
        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);

        Assertions.assertTrue(exceptions.isEmpty());
    }

    @Test
    void thumbnailImgIsValidSize_ImgSizeEdgeCaseValid_returnsNoExceptions() {
        String edgeCaseValidFilePath = "src/test/resources/test-images/png_9MB.png";
        Path edgeCaseValidPath = Paths.get(edgeCaseValidFilePath);
        String edgeCaseValidName = "png_9MB";
        String edgeCaseValidOriginalFileName = "png_9MB.png";
        String edgeCaseValidContentType = "image/png";
        byte[] edgeCaseValidContent = new byte[0];
        {
            try {
                pngValidContent = Files.readAllBytes(edgeCaseValidPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        MultipartFile mockEdgeCaseValid = new MockMultipartFile(edgeCaseValidName,
                edgeCaseValidOriginalFileName,
                edgeCaseValidContentType,
                edgeCaseValidContent);

        streamDTO.setImage(mockEdgeCaseValid);
        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);

        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"png", "jpg", "jpeg", "svg"})
    void thumbnailImgValid_contentTypeValid_returnsNoExceptions(String validContentType) {

         mockImageValidContentType = new MockMultipartFile(
                svgValidName,
                svgValidOriginalFileName,
                validContentType,
                svgValidContent);

        streamDTO.setImage(mockImageValidContentType);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);

        Assertions.assertTrue(exceptions.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"pdf", "xml", "notValid", "!"})
    void thumbnailImgInvalid_contentTypeInvalid_returnsImageTypeException(String invalidContentType) {
        String expectedField = "image";
        String expectedCode = "401";
        String expectedMessage = "Image must be of type png, jpg, jpeg or svg";

        mockImageInvalidContentType = new MockMultipartFile(
                svgValidName,
                svgValidOriginalFileName,
                invalidContentType,
                svgValidContent);

        streamDTO.setImage(mockImageInvalidContentType);

        List<ValidationExceptionDTO> exceptions = streamValidator.validateStream(streamDTO);

        Assertions.assertAll(
                () -> Assertions.assertEquals(1, exceptions.size()),
                () -> Assertions.assertEquals(expectedField, exceptions.getFirst().getField()),
                () -> Assertions.assertEquals(expectedCode, exceptions.getFirst().getErrorCode()),
                () -> Assertions.assertEquals(expectedMessage, exceptions.getFirst().getMessage())
        );
    }


}
