/**
 * Script for detecting when a user is attempting to close a window or tab or navigate away from a page
 * Ends the stream when they do
 */

// const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
// const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
// const livestreamData = document.getElementById("livestream-data");
// const livestreamId = livestreamData.getAttribute('data-livestream-id');

///// End Livestream Modal /////

const endLiveStreamOverlay = document.getElementById("confirm-end-livestream");
const stayButton = document.getElementById("confirm-stay");
let streamEnded = false;

/**
 * Open modal
 */
function openConfirmation() {
    endLiveStreamOverlay.style.display = "flex";
    stayButton.focus();
}

/**
 * End Livestream
 */
function confirmEndLivestream() {
    endLiveStreamOverlay.style.display = "none";
    deleteLivestream();
    streamEnded = true;
}

/**
 * Continue Livestream
 */
function cancelEndLivestream() {
    endLiveStreamOverlay.style.display = "none";
}

/**
 * Deletes livestream
 */
function deleteLivestream(listener = false) {
    const url = `../end/${livestreamId}`;

    if (!streamEnded) {
        fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            },
            body: JSON.stringify({})
        }).then(response => response.text())
        .then(response => {
            if (response === "success") {
                console.log("Successfully Ended Stream")
                if (!listener) {
                    window.location.href = "../browse"
                }
            } else {
                console.error("Failed to end livestream")
            }
        }).catch(error => {
            console.error('Error ending livestream:', error);
        });
    }
}

///// Livestream Ended Modal ////
const livestreamEnded = document.getElementById("livestream-ended");
const okRedirect = document.getElementById("ok-redirect");

/**
 * Open modal
 */
function openStreamEnded() {
    livestreamEnded.style.display = "flex";
    okRedirect.focus();
}

function redirectUser() {
    streamEnded = true;
    window.location.href = "../browse"
}

///// Page Listeners /////

window.addEventListener("beforeunload", (event) => {
    if (!streamEnded) {
        event.preventDefault();
        deleteLivestream(true)
        openStreamEnded()
    }
});