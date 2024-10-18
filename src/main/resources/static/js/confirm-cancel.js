/**
 * JS code to control a cancel confirm popup on the add post page.
 */
const titleInput = document.querySelector("input[name='title']");
const contentInput = document.querySelector("textarea[name='content']");
const overlay = document.getElementById("confirm-overlay");
const confirmNoButton = document.getElementById("confirm-no");

function cancelAction() {
    if (titleInput.value.trim() !== "" || contentInput.value.trim() !== "") {
        overlay.style.display = "flex";
        confirmNoButton.focus();
    } else {
        goPrev();
    }
}

function confirmYes() {
    overlay.style.display = "none";
    goPrev();
}

function  confirmNo() {
    overlay.style.display = "none";
}