/*
If we are coming back from a page with the same URL as we are currently on, we want to go back to the first different
path (see goPrev docstring). We do this via recursion, but as the javascript is reloaded each time we go back a page
we need to use sessionStorage to know if we are in the recursion or not.
 */
if (sessionStorage.isRecursing) {
    goPrev();
}

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
 *  Check if the users previous page was within the application and redirect to that page if so
 *  (Special case: If the user is coming back from a page with the same URL, they likely want to go back to the most
 *  recent page they were on with a different URL (i.e., after submitting a form with errors, when you hit cancel, you
 *  want to go back to the page you were on before accessing the form, not the form with errors you were just on
 *  before submitting).
 *  Otherwise redirect to the Home page by default
 */
function goPrev() {
    if (window.history && window.history.back && isInternalPage()) {
        if (window.location.href === document.referrer) {   // Previous page is the same as current
            sessionStorage.setItem("isRecursing", "true");  // Use session storage so we can access this variable after the javascript is reloaded
            window.history.back()
        }
        else {
            sessionStorage.clear()
            window.history.back()
        }
    } else {

        // go to the home page by default
        window.location.href = baseUrl + "/user/home";
    }
}

/**
 *  Check the URL of the previous page using document referrer
 * @returns {boolean} true if the previous page URL exists and is an internal page, false otherwise
 */
function isInternalPage() {
    // referrer is set to URL of previous page
    // check the referrer includes the hostname and port number to make sure internal page
    return document.referrer !== "" && document.referrer.includes(window.location.host);
}
