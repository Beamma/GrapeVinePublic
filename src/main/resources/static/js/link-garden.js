const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

const errorMessage = document.getElementsByClassName("link-garden-error-message")[0]
const modal = document.getElementById("link-garden-modal");
const linkedGardenPreview = document.getElementById("linked-garden-preview")
const linkedGardenName = document.getElementById("linked-garden-name")
const linkedGardenCity = document.getElementById("linked-garden-city")
const linkedGardenIdInput = document.getElementById("linked-garden-id-input")
const linkGardenButton = document.getElementById("show-link-modal-preview-button")
const searchResults = document.getElementById("link-garden-search-results")

/**
 * The maximum number of gardens that are displayed in the modal (set in the GardenService class)
 */
const LINK_GARDEN_RESULT_LIMIT = 20;

/**
 * Shows the link garden modal if it's not already showing. Otherwise it closes it.
 * Called when the Link Garden button is clicked
 */
function showLinkGardenModal() {
    modal.style.display === "flex" ? modal.style.display = "none" : modal.style.display = "flex"
}

/**
 * Closes the link garden modal.
 * Called when the x button on the modal is clicked
 */
function hideLinkGardenModal() {
    modal.style.display = "none";
    searchResults.replaceChildren()
}

/**
 * Removes the preview of the linked garden. Also clears the input for the garden id.
 */
function removeLinkedGarden() {
    linkedGardenPreview.style.display = "none"
    linkedGardenName.innerText = name;
    linkGardenButton.style.display = "flex";

    linkedGardenIdInput.value = ''      // Clear (hidden) linked garden id input
}

/**
 * Links a garden to the post creation form. Shows a preview and sets the hidden linked garden id input.
 * Also closes the 'search for a public garden' modal and hides the button
 * @param id the garden ID of the linked garden
 * @param name the name of the linked garden
 * @param city the city of the linked garden
 */
function linkGarden(id, name, city) {
    linkedGardenName.innerText = name;
    linkedGardenCity.innerText = city;
    linkedGardenPreview.style.display = "flex"

    modal.style.display = "none";
    linkGardenButton.style.display = "none";

    linkedGardenIdInput.value = id
}

/**
 * Shows the message asking the user to refine their query (top 20 results shown)
 */
function showPleaseRefineQueryMessage() {
    document.getElementsByClassName("link-garden-results-limit-message")[0].style.display = "block";
}

/**
 * Hides the message asking the user to refine their query (top 20 results shown)
 */
function hidePleaseRefineQueryMessage() {
    document.getElementsByClassName("link-garden-results-limit-message")[0].style.display = "none";
}

/**
 * Takes the returned gardens that match the search term and shows a list of those gardens under the search bar from
 * which the user can select a garden to link
 *
 * Use of DocumentFragment from: https://stackoverflow.com/a/70841617
 *
 * @param gardens the gardens that match the user's search term
 */
function showMatchingPublicGardens(gardens) {
    let documentFragment = new DocumentFragment()

    gardens.forEach((garden, index) => {
        let result = document.createElement("li")
        result.className = "link-garden-search-result"
        result.tabIndex = index
        result.onclick = function () {
            linkGarden(garden.gardenId, garden.name, garden.location.city)
        }
        result.addEventListener("keydown", e => {
            // Tab to next result, or if is last result, tab to top
            if (e.key === "Tab") {
                e.preventDefault()
                let nextResult = result.nextElementSibling;
                if (nextResult) {
                    nextResult.focus()
                    nextResult.scrollIntoView()
                } else {
                    result.parentElement.firstElementChild.focus()
                }
            }
            // Same as clicking on the result
            if (e.key === "Enter") {
                e.preventDefault()
                linkGarden(garden.gardenId, garden.name, garden.location.city)
            }
        })

        let gardenName = document.createElement("span")
        gardenName.textContent = garden.name;

        let gardenCity = document.createElement("span")
        gardenCity.textContent = garden.location.city;

        result.appendChild(gardenName)
        result.appendChild(gardenCity)
        documentFragment.appendChild(result)
    })
    if (gardens.length >= LINK_GARDEN_RESULT_LIMIT) {
        showPleaseRefineQueryMessage()
    }
    searchResults.replaceChildren(documentFragment)
}

/**
 * Shows the error message in the link garden modal
 * @param message the error message to be displayed
 */
function showErrorMessage(message) {
    errorMessage.style.display = "flex"
    errorMessage.innerText = message
}

/**
 * Queries the database for public gardens owned by the user that match the user's search term. Shows all matching
 * gardens from which the user can choose one to link to the post
 */
async function showSearchedGardens() {
    const searchTerm = document.getElementById('search-gardens-to-link').value;
    if (searchTerm === "") {
        showErrorMessage("Please enter a garden name")
        return
    }

    const url = `../garden/public/search?name=${searchTerm}`;

    try {
        searchResults.replaceChildren() // Clears any error messages or previously searched gardens
        hidePleaseRefineQueryMessage()

        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        });

        if (response.ok) {
            let content = await response.json()

            if (content.errorMessage) {     // Error message from server (e.g. malformed query)
                showErrorMessage(content.errorMessage)
            } else if (content.length === 0) {  // No matching gardens found
                showErrorMessage("None of your public gardens match your search")
            } else {
                errorMessage.style.display = "none"
                showMatchingPublicGardens(content)
            }
        }
        else {
            showErrorMessage("Invalid search. Queries may only contain alphanumeric characters, -, ‘, dots, commas and spaces")
        }
    } catch (error) {
        console.error(error)
        showErrorMessage("Could not search your public gardens at this time.")
    }
}

/**
 * Ensures that, while typing the name of a selected garden, hitting enter will submit the search, and that users
 * cannot tab out of the modal.
 */
document.getElementById("search-gardens-to-link")
    .addEventListener("keydown", async e => {
        if (e.key === "Enter") {
            await showSearchedGardens()
        }
        // Tab to first result (if present)
        if (e.key === "Tab") {
            e.preventDefault()
            document.getElementById("link-garden-search-results").firstElementChild.focus()
        }
    });