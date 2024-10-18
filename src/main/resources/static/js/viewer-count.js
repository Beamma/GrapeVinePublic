let baseUrl = window.location.pathname;

// Set the base URL based on the environment
if (baseUrl.startsWith("/prod")) {
    baseUrl = "/prod";
} else if (baseUrl.startsWith("/test")) {
    baseUrl = "/test";
} else {
    baseUrl = "";
}

/**
 * Update the viewer count on the browse livestreams page on load
 */
window.onload = function() {
    // Select all span elements with the class "viewer-count-text"
    const spans = document.querySelectorAll('.viewer-count-text');

    // Iterate through each span and update its text content
    spans.forEach(span => {
    // Get the value from the "data-value" attribute
    const value = span.getAttribute('data-value');
    getViewerCount(value);
});
};

/**
 * Method duplicated from livestream.js
 * This function sends a GET request to the agora api to get the viewer count of a livestream
 * @param livestreamId the id of the livestream to get the viewer count for
 */
async function getViewerCount(livestreamId) {
    const url = baseUrl + `/agora/${livestreamId}/viewer-count`;
    try {
    const response = await fetch(url, {
    method: 'GET',
    headers: {
    'Content-Type': 'application/json',
    }
    });
    if (response.ok) {
    const viewerCount = await response.text()
    const viewerCountText = document.getElementById('viewer-count-text-' + livestreamId);
    viewerCountText.innerText = viewerCount;
    }
    } catch (error) {
        console.error('Error:', error);
    }
        setTimeout(() => getViewerCount(livestreamId), 5000);
}