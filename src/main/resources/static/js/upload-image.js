/**
 * Logic for opening and closing the upload image popup
 */
document.getElementById('openPopup').addEventListener('click', function () {
    document.getElementById('uploadImage').style.display = 'block';
});

document.getElementById('closePopup').addEventListener('click', function () {
    document.getElementById('uploadImage').style.display = 'none';
});

/**
 * Cache the image container content to be able to reset to it if the new image is invalid
 */
let imageContainerContent = "";

window.onload = function() {
    let imageContainer = document.querySelector('.image-container');

    if (imageContainer) {
        imageContainerContent = imageContainer.innerHTML;
    }
};

/**
 * Client side logic for validating the image and displaying it in the image container
 */
document.getElementById('submitImage').addEventListener('click', function () {
    let elementId = null;
    if (document.getElementById('plantImage')) {
        elementId = 'plantImage';
    } else if (document.getElementById('profileImage')) {
        elementId = 'profileImage';
    }
    if (document.getElementById(elementId).files.length === 0) {
        document.getElementById('imageValidationError').textContent = "No image selected";
        return;
    }
    const file = document.getElementById(elementId).files[0];

    // Validation checks
    let isValid = true;
    let validationError = "";
    if (file.size === 0) {
        validationError = "Image is empty";
        isValid = false;
    }
    else if (file.size > (1024*1024)*10) {
        validationError = "Image must be less than 10MB";
        isValid = false;
    }
    else {
        let imageTypeRegex = /^image\/(jpeg|jpg|svg\+xml|png)$/i;
        if (!imageTypeRegex.test(file.type)) {
            validationError = "Image must be JPG, JPEG, SVG, or PNG";
            isValid = false;
        }
    }

    // Updating page based on validation results
    if (isValid) {
        document.getElementById("selectImageP").textContent = "Edit Image";
        document.getElementById("imageValidationError").textContent = "";
        document.getElementById('uploadImage').style.display = 'none';
        document.getElementById('imagePath').textContent = "image: " + file.name.substring(0, 40);
        const imgContainer = document.querySelector('.image-container');
        if (imgContainer) {
            imgContainer.innerHTML = "";
            const img = document.createElement('img');
            img.src = URL.createObjectURL(file);
            imgContainer.appendChild(img);
        }
    } else {
        document.getElementById('imageValidationError').textContent = validationError;
        document.getElementById('imagePath').textContent = "";
        document.getElementById(elementId).value = "";
        const imgContainer = document.querySelector('.image-container');
        if (imgContainer) {
            imgContainer.innerHTML = imageContainerContent;
        }
    }
});