function checkPreviewFile(oldFile) {
    const preview = document.getElementById('previewImage');
    const file = document.getElementById('profileImageEdit').files[0];
    const reader = new FileReader();
    const errorText = document.getElementById('jsImageErrorText');
    const successText = document.getElementById('imageSuccessText');
    const submitButton = document.getElementById('submitEdit');
    let imageInput = document.getElementById('profileImageEdit');

// Validation checks
    let isValid = true;

    if (file) {
        if (file.size >= 10*1024*1024) {
            errorText.textContent = "Image must be less than 10MB";
            isValid = false;
        }
        let imageTypeRegex = /^image\/(jpeg|jpg|svg\+xml|png)$/i;
        if (!imageTypeRegex.test(file.type)) {
            errorText.textContent = "Image must be JPG, JPEG, SVG, or PNG";
            isValid = false;
        }
    }

    reader.onloadend = function() {
        preview.src = reader.result;
    };

    if (isValid && file) {
        reader.readAsDataURL(file);
        errorText.textContent = "";
        submitButton.disabled = false;
    } else if (file) {
        submitButton.disabled = true;
        preview.src = oldFile;
        successText.textContent = "";
        imageInput.value = "";
    } else {
        preview.src = oldFile;
        errorText.textContent = "";
        successText.textContent = "";
        submitButton.disabled = false;
    }
}


