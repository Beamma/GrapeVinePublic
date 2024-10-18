// https://dev.to/jaymeeu/how-to-create-a-custom-image-uploader-with-image-preview-36lb

/**
 * Activates the hidden file input to open the file selector popup
 */
function upload() {
    document.getElementById('add-post-image').click();
}


const input_file = document.getElementById('add-post-image');
const imagePreview = document.getElementById('image-preview');
const addImageButtonContainer = document.getElementsByClassName('add-image-button-container')[0];

/**
 * Converts image to Base64 for the preview
 * https://dev.to/jaymeeu/how-to-create-a-custom-image-uploader-with-image-preview-36lb
 *
 * @param file the file being converted to base64
 * @returns {Promise} the Base64 encoded image
 */
const convert_to_base64 = file => new Promise((response) => {
    const file_reader = new FileReader();
    file_reader.readAsDataURL(file);
    file_reader.onload = () => response(file_reader.result);
});

/**
 * Checks if the given filetype is to be accepted
 * @param filetype the type of the file being uploaded
 * @returns {boolean} true if the filetype is valid, false otherwise
 */
function isValidFiletype(filetype) {
    let validFileExtensions = ["image/png", "image/jpeg", "image/jpg", "image/svg+xml"]
    return validFileExtensions.includes(filetype)
}

const BYTES_IN_MEGABYTE = 1048576;

/**
 * If the file is the correct size and type, it previews the image.
 * If the image is invalid, it clears the file input so the invalid image cannot be submitted
 * @param file the file being validated/previewed
 */
async function validateAndPreviewFile(file) {

    if (file.size > 10 * BYTES_IN_MEGABYTE) {
        document.getElementById('client-side-img-val').style.display = "block";
        document.getElementById('client-side-img-val').innerText = "Image must be smaller than 10MB";
        document.getElementById('image-drop-zone').style.borderColor = "#ff0000"
        input_file.value = '';
        removeImage();
    } else if (!isValidFiletype(file.type)) {
        document.getElementById('client-side-img-val').style.display = "block";
        document.getElementById('client-side-img-val').innerText = "Image must be of type png, jpg or svg";
        document.getElementById('image-drop-zone').style.borderColor = "#ff0000"
        input_file.value = '';
        removeImage();
    } else {
        document.getElementById('client-side-img-val').style.display = "none";
        document.getElementById('image-drop-zone').style.borderColor = "var(--text-box-outline)"
        const image = await convert_to_base64(file);
        imagePreview.src = image.toString();
        imagePreview.parentElement.style.display = 'flex';
        addImageButtonContainer.style.display = 'none';
    }
}

/**
 * When a file is uploaded, it removes the image validation error message from the server-side validation
 * (if one exists) and sends it to be validated and previewed
 */
input_file.addEventListener('change', async function () {
    // As a new file has been uploaded, we can get rid of the server-side validation error from the last file
    let serverSideImageErrorMessage = document.getElementById('post-image-field-error');
    if (serverSideImageErrorMessage) {
        serverSideImageErrorMessage.style.display = "none"
    }

    const file = document.querySelector('#add-post-image').files[0];
    await validateAndPreviewFile(file)
})

/**
 * When the image is deleted, this resets the preview and reinstates the add image button
 */
function removeImage() {
    document.querySelector('#add-post-image').value = ""
    imagePreview.src = ""
    imagePreview.parentElement.style.display = 'none'
    addImageButtonContainer.style.display = 'flex'
}

// ===================== Drag and drop handling =====================

/**
 * Prevents browser from opening the image
 * @param ev the drag event
 */
function dragOverHandler(ev) {
    ev.preventDefault();
}

/**
 * Adds a given file to the file input. Required as the drag and drop does not do this automatically.
 * Inspired by: https://stackoverflow.com/a/74087012
 *
 * @param file the file to be added to the form input
 */
function addFileToInput(file) {
    const dataTransfer = new DataTransfer();
    dataTransfer.items.add(file);
    input_file.files = dataTransfer.files;
}

/**
 * As there are two HTML defined drag and drop APIs, this processes both DataTransfer interface and the DataTransferItem
 * and DataTransferItemList interfaces.
 *
 * Handles the attempted upload of multiple files.
 *
 * With much inspiration from https://developer.mozilla.org/en-US/docs/Web/API/HTML_Drag_and_Drop_API/File_drag_and_drop
 *
 * @param ev the drop event
 */
async function dropHandler(ev) {
    ev.preventDefault();

    // As a new file has been uploaded, we can get rid of the server-side validation error from the last file
    let serverSideImageErrorMessage = document.getElementById('post-image-field-error');
    if (serverSideImageErrorMessage) {
        serverSideImageErrorMessage.style.display = "none"
    }

    if (ev.dataTransfer.items) {
        // Use DataTransferItemList interface to access the file(s)
        if (ev.dataTransfer.items.length > 1) {
            document.getElementById('client-side-img-val').style.display = "block"
            document.getElementById('client-side-img-val').innerText = "Only one image can be uploaded"
        } else if (ev.dataTransfer.items[0].kind === "file") {
            let file = ev.dataTransfer.items[0].getAsFile()
            addFileToInput(file)
            await validateAndPreviewFile(file)
        }
    } else {
        // Use DataTransfer interface to access the file(s)
        if (ev.dataTransfer.files.length > 1) {
            document.getElementById('client-side-img-val').style.display = "block"
            document.getElementById('client-side-img-val').innerText = "Only one image can be uploaded"
        } else {
            let file = ev.dataTransfer.files[0]
            addFileToInput(file)
            await validateAndPreviewFile(file)
        }
    }
}


// ===================== Discard Image Confirmation handling =====================


const discardImageOverlay = document.getElementById("confirm-image-discard-overlay")
const cancelDiscardImageButton = document.getElementById("confirm-no-image")

/**
 * Opens confirmation modal for discarding an image
 */
function openImageDiscardConfirmation() {
    discardImageOverlay.style.display = "flex";
    cancelDiscardImageButton.focus();
}

/**
 * Discards image from form
 */
function confirmDiscardImage() {
    discardImageOverlay.style.display = "none";
    removeImage()
}

/**
 * Closes the confirmation modal for discarding an image
 */
function  cancelDiscardImage() {
    discardImageOverlay.style.display = "none";
}