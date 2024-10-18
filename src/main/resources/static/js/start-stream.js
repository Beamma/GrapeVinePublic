const startStreamOverlay = document.getElementById("start-stream-overlay")
const okButton = document.getElementById("confirm-ok")
openModalConfirmation()
/**
 * Opens confirmation modal for starting a stream
 */
function openModalConfirmation() {
    startStreamOverlay.style.display = "flex";
    okButton.focus();
}

/**
 * Closes the confirmation modal for starting stream
 */
function  closeModal() {
    startStreamOverlay.style.display = "none";
}