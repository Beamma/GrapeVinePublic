/**
 * Update the size of the font depending on the length of the text
 * Used in view livestream on the title
 */
function resizeTextToFit() {
    const resizableText = document.getElementById('resizable-text');
    const parentWidth = resizableText.parentElement.clientWidth;
    let fontSize = 32; // Start with a large font size

    resizableText.style.fontSize = fontSize + 'px'; // Apply starting font size

    while (resizableText.scrollWidth > parentWidth && fontSize > 18) {
        fontSize--; // Decrease font size
        resizableText.style.fontSize = fontSize + 'px';
    }
}

window.onload = resizeTextToFit;
window.onresize = resizeTextToFit;
