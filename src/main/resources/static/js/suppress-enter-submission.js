/**
 * This script prevents the enter key from submitting any forms unless the active element is one of the allowed elements.
 * Active elements can be specified by adding a data-allowed-types and/or ids attribute to the script tag in a comma separated list.
 * (no spaces)
 * Allowed elements are specified by their type attribute or their id attribute.
 *
 * e.g <script src="suppress-enter-submission.js" data-allowed-types="date,submit" data-allowed-ids="countField"></script>
 *
 * Reference: https://stackoverflow.com/questions/895171/prevent-users-from-submitting-a-form-by-hitting-enter
 */
const this_file = document.currentScript;
let allowed_types = [];
let allowed_ids = [];
let baseUrlForEnterSuppression = window.location.pathname;

// Set the base URL based on the environment
if (baseUrlForEnterSuppression.startsWith("/prod")) {
    baseUrlForEnterSuppression = "/prod";
} else if (baseUrlForEnterSuppression.startsWith("/test")) {
    baseUrlForEnterSuppression = "/test";
} else {
    baseUrlForEnterSuppression = "";
}

try {
    allowed_types = this_file.attributes.getNamedItem("data-allowed-types").value.split(",");
} catch (TypeError) {}
try {
    allowed_ids = this_file.attributes.getNamedItem("data-allowed-ids").value.split(",");
} catch (TypeError) {}
document.addEventListener('keydown', function(event) {
    if (event.key === "Enter") {
        let activeElement = document.activeElement;

        if (!allowed_types.includes(activeElement.type) && !allowed_ids.includes(activeElement.id)) {
            event.preventDefault();
            // Copilot helped with case where the href was in a span within a button
        } else if (activeElement.tagName.toLowerCase() === 'button') {
            // Get the span within the button
            let span = activeElement.querySelector('span');
            let a = activeElement.querySelector('a');
            // If the span exists and it has an href attribute, redirect to the URL
            if (span && span.hasAttribute('href')) {
                let href = span.getAttribute('href');
                if (!href.includes(baseUrlForEnterSuppression)) {
                    window.location.href = baseUrlForEnterSuppression + href
                } else {
                    window.location.href = href
                }
            } else if (a && a.hasAttribute('href')) {
                let href = a.getAttribute('href');
                if (!href.includes(baseUrlForEnterSuppression)) {
                    window.location.href = baseUrlForEnterSuppression + href
                } else {
                    window.location.href = href
                }
            }
        }
    }
});

// Because we stylistically use spans inside of buttons,
// this script will redirect the user to the URL specified in the span's href attribute
const buttons = document.querySelectorAll('button');
buttons.forEach(button => {
    button.addEventListener('click', function(event) {
        let span = button.querySelector('span');
        if (span && span.hasAttribute('href')) {
            let href = span.getAttribute('href');
            if (!href.includes(baseUrlForEnterSuppression)) {
                window.location.href = baseUrlForEnterSuppression + href
            } else {
                window.location.href = href
            }
        }
    });
});



