/**
 * JS Script for loading the sidebars, and top and bottom bars of images,
 * and matching the color to the color of the image
 */
document.addEventListener('DOMContentLoaded', function() {
    function applyAverageColourToImages(selector) {
        const images = document.querySelectorAll(selector);

        images.forEach((img) => {
            function applyAverageColor() {
                img.parentElement.style.backgroundColor = getAverageColor(img);
            }

            if (img.complete) {
                applyAverageColor(); // Apply color if image is already loaded
            } else {
                img.addEventListener('load', applyAverageColor); // Wait for image to load
            }
        });
    }
    //For the Feed page and the browse live streams page
    applyAverageColourToImages('.post-image')
    applyAverageColourToImages('.thumbnail-image')

    function getAverageColor(img) {
        const canvas = document.createElement('canvas');
        const context = canvas.getContext('2d');
        canvas.width = img.naturalWidth;
        canvas.height = img.naturalHeight;
        context.drawImage(img, 0, 0, canvas.width, canvas.height);

        const imageData = context.getImageData(0, 0, canvas.width, canvas.height);
        const data = imageData.data;

        let r = 0, g = 0, b = 0;
        let pixelCount = 0;
        let hasTransparency = false;

        for (let i = 0; i < data.length; i += 4) {
            const alpha = data[i + 3]; // Get alpha value

            if (alpha < 255) {
                hasTransparency = true;
                break; // Stop if transparency is found
            }

            r += data[i];
            g += data[i + 1];
            b += data[i + 2];
            pixelCount++;
        }

        if (hasTransparency) {
            return 'rgb(255,255,255)'; // Return white if transparency is found
        }

        r = Math.floor(r / pixelCount);
        g = Math.floor(g / pixelCount);
        b = Math.floor(b / pixelCount);

        return `rgb(${r},${g},${b})`;
    }
});