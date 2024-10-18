/**
 * Shows the loading spinner before a redirect
 */
function showLoadingSpinner() {
    setTimeout(() => {
        document.getElementById('registerUserForm').style.display = "none";
        document.getElementById('loadingGif').style.display = "block";
    }, 200) // Stops the loading spinner flashing up for very short periods of time
}

