/*
Script for create livestream form to check that user has an audio and video device.
Controls a popup modal that appears if the user attempts to create a stream without
either a valid camera or a valid mic being found.
 */

window.onload = function() {
    validateAudioVideo();
}

const modal = document.getElementById("confirm-audio-video-discard-overlay");
const cancelButton = document.getElementById("cancel-audio-video");
const cameraText = document.getElementById("camera-text");
const audioText = document.getElementById("mic-text");
const cameraBox = document.getElementById("camera-box");
const audioBox = document.getElementById("audio-box");
const rescanButton = document.getElementById("rescan-audio-video");

const green = getComputedStyle(document.documentElement)
    .getPropertyValue('--submit-button-bg')
    .trim();

const red = getComputedStyle(document.documentElement)
    .getPropertyValue('--red-button-bg')
    .trim();

//Free Use SVGs from iconmonstr.com
const checkboxYes = `
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
    <path fill="${green}" d="M0 0v24h24v-24h-24zm10.041 17l-4.5-4.319 1.395-1.435 3.08 2.937 7.021-7.183 1.422 1.409-8.418 8.591z"/>
</svg>`;

const checkboxNo = `
<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">
    <path fill="${red}" d="M0 0v24h24v-24h-24zm16.597 17.954l-4.591-4.55-4.555 4.596-1.405-1.405 
    4.547-4.592-4.593-4.552 1.405-1.405 4.588 4.543 4.545-4.589 1.416 1.403-4.546 4.587 4.592 4.548-1.403 1.416z"/>
</svg>`;

let audioAvailable = false;
let videoAvailable = false;


document.getElementById("livestream-form").addEventListener("submit", function(event) {
    if (!audioAvailable || !videoAvailable) {
        //If mic or camera not present, show modal.
        event.preventDefault();
        updateTexts();
        modal.style.display = "flex";
        cancelButton.focus();
    }
})

function hideModal() {
    modal.style.display = "none";
}

async function rescanAudioVideo() {
    //Check for camera and mic again, change rescan button to "Continue" if all green.
    if (rescanButton.className === "submit") {
        hideModal();
    }
    await validateAudioVideo();
    if (audioAvailable && videoAvailable) {
        rescanButton.className = "submit";
        document.getElementById("rescan-text").textContent = "Continue";
    }
}

async function validateAudioVideo() {
    //Function that validates the browser has access to video and audio tracks.
    //Check for audio tracks
    try {
        let audioStream = await navigator.mediaDevices.getUserMedia({audio: true});
        if (audioStream != null && audioStream.getAudioTracks().length > 0) {
            audioAvailable = true;
            audioStream.getTracks().forEach(track => {
                track.stop();
            })
            audioStream = null;
        }
    } catch (error){}
    updateTexts();

    //Check for video tracks
    try {
        let videoStream = await navigator.mediaDevices.getUserMedia({video: true})
        if (videoStream != null && videoStream.getVideoTracks().length > 0) {
            videoAvailable = true;
            videoStream.getTracks().forEach(track => {
                track.stop();
            })
            videoStream = null;
        }
    } catch (error){}

    updateTexts();
}

function updateTexts() {
    //Change colours and icon to indicate whether camera and mic are available.
    cameraText.style.color = videoAvailable ? green : red;
    audioText.style.color = audioAvailable ? green : red;
    cameraBox.innerHTML = videoAvailable ? checkboxYes : checkboxNo;
    audioBox.innerHTML = audioAvailable ? checkboxYes : checkboxNo;
}