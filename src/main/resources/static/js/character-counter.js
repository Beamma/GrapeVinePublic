const CHAT_CHARACTER_LIMIT = 255;

let chatInput = document.getElementById("message-input");
let chatLengthErrorMessage = document.getElementById("chat-length-error-message");
let charCounter = document.getElementById("char-counter")
let submitButton = document.getElementById('send-message-button');


/**
 * Updates the character counter (of the form x/charLimit). If the input is a valid length, the text is cut short and the
 * counter goes red. Pasting is accounted for, as are emojis. Shows an error message in the appropriate field
 */
function updateCounter(input, charLimit, errorMessageElement) {
    // Reset any effects from a too-long error message
    charCounter.style.color = "black"
    if (errorMessageElement) { errorMessageElement.style.display = "none"; }

    let numChars = [...input.value].length;  // https://stackoverflow.com/a/54369605
    if (numChars > charLimit) {
        // todo: make submit button disabled if its too long or unable to enter more text
        submitButton.disabled = true;
        charCounter.style.color = "red"
        input.value = [...input.value].slice(0, charLimit).join("")
        if (errorMessageElement) {
            errorMessageElement.style.display = "flex"
        }
    }
    submitButton.disabled = false;
    charCounter.innerText = `(${Math.min(numChars, charLimit)}/${charLimit})`
}

chatInput.addEventListener("input", () => updateCounter(chatInput, CHAT_CHARACTER_LIMIT, chatLengthErrorMessage))
document.addEventListener("DOMContentLoaded", () => updateCounter(chatInput, CHAT_CHARACTER_LIMIT, chatLengthErrorMessage));