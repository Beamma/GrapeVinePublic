// import { BYTES_IN_MEGABYTE, isValidFiletype } from "./upload-post-image";

/**
 * Converts a Base64 encoded image to a file, to be used to repopulate the relevant form field
 *
 * With great inspiration from:
 * https://stackoverflow.com/a/77714942
 * ChatGPT
 *
 * @param base64Image the image being repopulated (in base64 encoding)
 * @param imageType the type of the image (e.g. "image/png")
 * @param imageName the filename of the image (e.g. "my_garden.png")
 * @returns {File} the image as a file
 */
function convertToFile(base64Image, imageType, imageName) {
    const byteString = atob(base64Image);
    const mimeString = imageType.split(';')[0].split(':')[1];
    const arrayBuffer = new ArrayBuffer(byteString.length);
    const uint8Array = new Uint8Array(arrayBuffer);
    for (let i = 0; i < byteString.length; i++) {
        uint8Array[i] = byteString.charCodeAt(i);
    }
    const blob = new Blob([uint8Array], {type: mimeString});
    return new File([blob], imageName, {type: imageType});
}


/**
 * Repopulates the file input. Used to repopulate the image field when the server is trying to
 * repopulate the other (text) fields.
 *
 * (HTML elements are (for now) hardcoded, but if this is used elsewhere, this could be changed to get by class, or to
 * take their names/ids as parameters)
 *
 * Inspiration from:
 * https://stackoverflow.com/a/74087012
 *
 * @param base64Image the image being repopulated (in base64 encoding)
 * @param imageType the type of the image (e.g. "image/png"), used for cloning it
 * @param imageName the filename of the image (e.g. "my_garden.png"), used for cloning it
 */
function repopulateImageField(base64Image, imageType, imageName) {
    const imgElement = document.getElementById('image-preview');
    const fileInput = document.getElementById('add-post-image');
    const addImageButton =  document.getElementsByClassName('add-image-button-container')[0]

    // Show preview
    imgElement.src = 'data:' + imageType + ';base64,' + base64Image
    imgElement.parentElement.style.display = 'flex'

    // Remove add image button
    addImageButton.style.display = 'none'

    let file = convertToFile(base64Image, imageType, imageName)

    // Copy the files to the file input
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    fileInput.files = dataTransfer.files;
}