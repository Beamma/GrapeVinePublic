package nz.ac.canterbury.seng302.gardenersgrove.validation;


import org.springframework.web.multipart.MultipartFile;

public final class ImageValidator {

    public static int BYTES_IN_MB = 1048576;

    /**
     * Returns true if the image size is less than 10MB
     * @param image MultiPartFile of uploaded image
     * @return boolean
     */
    public static boolean isImageSizeValid(MultipartFile image) {
        return (image.getSize() < (10L * BYTES_IN_MB));
    }

    /**
     * Returns true if the image file is png, jpeg, jpg, or svg
     * @param image MultiPartFile of the image
     * @return boolean
     */
    public static boolean isImageTypeCorrect(MultipartFile image) {
        String contentType = image.getContentType();
        if (contentType == null) {
            return false;
        }
        return contentType.contains("png") || contentType.contains("jpeg") || contentType.contains("svg") || contentType.contains("jpg");
    }
}
