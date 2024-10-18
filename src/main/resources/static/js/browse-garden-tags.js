/**
 * Script for browseGarden page that manages tags to be added and removed from the search field before the form
 * is submitted.
 * @type {HTMLElement}
 */

let submittedTagsElement = document.getElementById('tags');
let addTagButton = document.getElementById("tagInputButton");
let tagInput = document.getElementById("tagInput");
let tagList = fetchServerTags();
let autofillContainer = document.getElementById("tag-autofill-suggestions-container")
let displayTagsElement = document.getElementById('displayTags');

addTagButton.addEventListener("click", () => addTag())
tagInput.addEventListener("keydown", function (e) {
    if (e.key === 'Enter') {
        e.preventDefault()
        addTag()
        document.getElementById("tag-autofill-suggestions-container").replaceChildren();
    }
    if (e.key === "ArrowDown") {
        e.preventDefault()
        navigateDropdown(e.key);
    }
})

tagInput.addEventListener('input', function (e) {
    attemptAutocomplete(tagInput.value)
})

renderTags();

/**
 * Function for getting the returned tags from the server side. This renders on the load of the page
 * Returns a list of tags from server side
 */
function fetchServerTags() {
    let tagValues = document.getElementById('tags').value;
    if (tagValues.trim() === "") {
        return []
    }
    return tagValues.split(',')
}



/**
 * Add a tag, taking the value from the tag input field if no tag is provided (from autofill suggestions)
 * Checks for duplicates tags.
 * Adds the tags, to the tag list as well as the hidden input tag field
 */
function addTag(tag=null) {
    if (tag == null) {
        tag = tagInput.value.trim();
    }
    if (tagList.includes(tag)) {
        document.getElementById("tagError").innerText = "Already filtering by that tag";
        return
    }
    if(tag === "") {
        document.getElementById("tagError").innerText = "Tag cannot be empty";
        return
    }

    // Check the added tag
    checkAddedTag(tag).then(isValid => {
        if (isValid) {
            // Add tag
            tagList.push(tag);
            submittedTagsElement.value = tagList.join(","); // Adds all the tags to the hidden input tag field
            renderTags();
            tagInput.value = "";
            document.getElementById("tagError").innerText = "";
        } else {
            // Add error message
            document.getElementById("tagError").innerText = "No tag matching " + tag;
        }
    });
}

/**
 * Remove a tag by the name of the tag
 * Then re-render the tags for the html
 * @param tagName
 */
function removeTag(tagName) {
    tagList = tagList.filter(tag => tag !== tagName)
    submittedTagsElement.value = tagList.join(",")
    renderTags();
}

/**
 * This function is used to update the displayTags div. Upon being called, it checks all the
 * current tags in the tagList and re-renders them.
 */
function renderTags() {
    displayTagsElement.innerHTML = ''; // Clears the currently displayed tags

    tagList.forEach(tag => {
        // Create a new div element to represent the tag
        let tagDiv = document.createElement('div');
        tagDiv.className = 'tag';
        tagDiv.className = 'chip';

        // Create a new span element to display the tag name, and contain the remove button
        let tagSpan = document.createElement('span');
        tagSpan.textContent = tag;

        // Create a new button element for deleting the tag
        let deleteButton = document.createElement('button');
        deleteButton.textContent = 'X';
        deleteButton.className = 'delete-tag-button';

        // Add an event listener to the delete button
        deleteButton.addEventListener('click', function() {
            removeTag(tag);
        });

        // Append the span and button to the div
        tagSpan.appendChild(deleteButton);
        tagDiv.appendChild(tagSpan);

        // Append the div to the displayTags element
        displayTagsElement.appendChild(tagDiv);
    });
}

/**
 * When a character is entered into the tag input text box, the database is queried for any tags whose start matches the
 * input
 * @param tagQuery the (start of a) tag to scan the database for.
 */
function attemptAutocomplete(tagQuery) {
    autofillContainer.replaceChildren();    // https://stackoverflow.com/a/65413839
    if (tagQuery.length > 2) {
        let url = `../tags?input=${tagQuery}`
        fetch(url)
            .then(response => {
                if (response.ok) {
                    response.json().then(tags => populateAutofill(tags));
                } else {
                    response.json().then(displayErrorMessage);
                }
            });
    }
}

/**
 * Given tag suggestions have been found, add a dropdown with the top 5 returned tag suggestions. Tags that have
 * already been added to the search query are not included here.
 * @param tags The list of tags matching the input, returned from the attemptAutocomplete method
 */
function populateAutofill(tags) {
    let filteredTags = tags.filter(tag => !tagList.includes(tag.name))
    for (const tag of filteredTags.slice(0, 5)) {
        autofillContainer.appendChild(createSuggestion(tag.name))
    }
}

/**
 * Creates a span element with a suggested tag autofill
 * @param tagName the suggested tag
 * @returns {HTMLSpanElement} the span element corresponding to that autofill suggestion
 */
function createSuggestion(tagName) {
    let suggestion = document.createElement('span');
    suggestion.textContent = tagName;
    suggestion.classList.add("autocomplete-tag-button")
    suggestion.tabIndex = 0
    suggestion.addEventListener("keydown", (e) => {
        e.preventDefault()
        navigateDropdown(e.key)
    })
    suggestion.addEventListener("click", (e) => {
        e.preventDefault()
        addTag(document.activeElement.textContent)
        document.getElementById("tag-autofill-suggestions-container").replaceChildren();
        tagInput.focus()
    })
    return suggestion
}

/**
 * Takes arrow-key input and navigates the dropdown
 * @param keyInput a string (the key property of the keydown event), which is "ArrowDown", "ArrowUp" or "Enter"
 */
function navigateDropdown(keyInput) {
    let focusedElement = document.activeElement

    if (autofillContainer.children.length === 0) {}  // No autofill suggestions to navigate to

    else if (focusedElement === tagInput && keyInput === "ArrowDown") {
        autofillContainer.firstElementChild.focus()
    }
    else if (keyInput === "ArrowUp" && focusedElement === autofillContainer.firstElementChild) {
        tagInput.focus()
    }
    else if (keyInput === "ArrowDown" && focusedElement !== autofillContainer.lastElementChild) {
        focusedElement.nextElementSibling.focus()
    }
    else if (keyInput === "ArrowUp" && focusedElement !== autofillContainer.firstElementChild) {
        focusedElement.previousElementSibling.focus()
    }
    else if (keyInput === "Enter") {    // Add the tag which is in focus to the search query
        addTag(focusedElement.textContent)
        document.getElementById("tag-autofill-suggestions-container").replaceChildren();
        tagInput.focus()
    }
}

/**
 * If the tag autocomplete endpoint does not return a 200 status code, inform the user that the tags could not be
 * autofilled
 */
function displayErrorMessage() {
    let errorMessage = document.createElement('span');
    errorMessage.textContent = "Tag suggestions unavailable.";
    errorMessage.classList.add("autocomplete-tag-button")
    errorMessage.style.color = "red"
    autofillContainer.appendChild(errorMessage)
}
/**
 * Checks the tag endpoint to see if a similar tag exists.
 *
 * @param tag the (start of a) tag to scan the database for.
 * @return True if the tag is valid, else false.
 */
function checkAddedTag(tag) {
    let url = `../tags/check?tag=${tag}`
    return fetch(url)
        .then(response => response.json())
        .then(result => result) // Return the result
        .catch(error => {
            return false;
        });
}