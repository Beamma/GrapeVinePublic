function debounce(func, wait) {

    /*
    Code is copy-pasted from https://davidwalsh.name/javascript-debounce-function
     */
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

function addressAutocomplete(containerElement, callback, options) {

    /*
    Code is copy-pasted from https://jsfiddle.net/Geoapify/akzrtm26/
     */
    var inputElement = containerElement.querySelector('input')

    // add input field clear button
    var clearButton = document.createElement("div");
    clearButton.classList.add("clear-button");
    addIcon(clearButton);
    clearButton.addEventListener("click", (e) => {
        e.stopPropagation();
        inputElement.value = '';
        callback(null);
        clearButton.classList.remove("visible");
        closeDropDownList();
    });
    containerElement.appendChild(clearButton);

    /* Current autocomplete items data (GeoJSON.Feature) */
    var currentItems;

    /* Active request promise reject function. To be able to cancel the promise when a new request comes */
    var currentPromiseReject;

    /* Focused item in the autocomplete list. This variable is used to navigate with buttons */
    var focusedItemIndex;

    /* Execute a function when someone writes in the text field: */

    var debouncedInput = debounce(function () {
        var currentValue = this.value;

        /* Close any already open dropdown list */
        closeDropDownList();

        // Cancel previous request promise
        if (currentPromiseReject) {
            currentPromiseReject({
                canceled: true
            });
        }

        if (!currentValue) {
            clearButton.classList.remove("visible");
            return false;
        }

        // Show clearButton when there is a text
        clearButton.classList.add("visible");

        /* Create a new promise and send geocoding request */
        var promise = new Promise((resolve, reject) => {
            currentPromiseReject = reject;

            let baseUrl = window.location.pathname;

            // Set the base URL based on the environment
            if (baseUrl.startsWith("/prod")) {
                baseUrl = "/prod";
            } else if (baseUrl.startsWith("/test")) {
                baseUrl = "/test";
            } else {
                baseUrl = "";
            }
            var url = baseUrl + `/user/autocomplete?input=${encodeURIComponent(currentValue)}`;
            if (options.type) {
                url += `&type=${options.type}`;
            }

            fetch(url)
                .then(response => {
                    // check if the call was successful
                    if (response.ok) {
                        response.json().then(data => resolve(data));
                    } else {
                        response.json().then(data => reject(data));
                    }
                });
        });

        promise.then((data) => {
            currentItems = data.features;
            let isFocused = (document.activeElement === this); // https://stackoverflow.com/a/59022882

            if (isFocused) {
                /*create a DIV element that will contain the items (values):*/
                var autocompleteItemsElement = document.createElement("div");
                autocompleteItemsElement.setAttribute("class", "autocomplete-items");
                containerElement.appendChild(autocompleteItemsElement);

                /* For each item in the results */
                data.features.forEach((feature, index) => {
                    /* Create a DIV element for each element: */
                    var itemElement = document.createElement("DIV");
                    /* Set formatted address as item value */
                    itemElement.innerHTML = feature.properties.formatted;

                    /* Set the value for the autocomplete text field and notify: */
                    itemElement.addEventListener("click", function (e) {
                        inputElement.value = currentItems[index].properties.formatted;

                        callback(currentItems[index]);

                        /* Close the list of autocompleted values: */
                        closeDropDownList();
                    });

                    autocompleteItemsElement.appendChild(itemElement);
                });
                if (data.features.length === 0) {
                    var itemElement = document.createElement("DIV");
                    itemElement.innerHTML = "No matching location found, location-based services may not work";
                    itemElement.addEventListener("click", function (e) {
                        e.stopPropagation()
                    });
                    autocompleteItemsElement.appendChild(itemElement);
                }
            }
        }, (err) => {
            if (!err.canceled) {
                console.log(err);
            }
        });
    }.bind(inputElement), 500);

    inputElement.addEventListener("input", debouncedInput);

    /* Add support for keyboard navigation */
    inputElement.addEventListener("keydown", function (e) {
        var autocompleteItemsElement = containerElement.querySelector(".autocomplete-items");
        if (autocompleteItemsElement) {
            var itemElements = autocompleteItemsElement.getElementsByTagName("div");
            if (e.keyCode == 40) {
                e.preventDefault();
                /*If the arrow DOWN key is pressed, increase the focusedItemIndex variable:*/
                focusedItemIndex = focusedItemIndex !== itemElements.length - 1 ? focusedItemIndex + 1 : 0;
                /*and make the current item more visible:*/
                setActive(itemElements, focusedItemIndex);
            } else if (e.keyCode == 38) {
                e.preventDefault();

                /*If the arrow UP key is pressed, decrease the focusedItemIndex variable:*/
                focusedItemIndex = focusedItemIndex !== 0 ? focusedItemIndex - 1 : focusedItemIndex = (itemElements.length - 1);
                /*and and make the current item more visible:*/
                setActive(itemElements, focusedItemIndex);
            } else if (e.keyCode == 13) {
                /* If the ENTER key is pressed and value as selected, close the list*/
                e.preventDefault();
                if (focusedItemIndex > -1) {
                    closeDropDownList();
                }
            }
        } else {
            if (e.keyCode == 40) {
                /* Open dropdown list again */
                var event = document.createEvent('Event');
                event.initEvent('input', true, true);
                inputElement.dispatchEvent(event);
            }
        }
    });

    function setActive(items, index) {
        if (!items || !items.length) return false;

        for (var i = 0; i < items.length; i++) {
            items[i].classList.remove("autocomplete-active");
        }

        /* Add class "autocomplete-active" to the active element*/
        items[index].classList.add("autocomplete-active");

        // Change input value and notify
        inputElement.value = currentItems[index].properties.formatted;
        callback(currentItems[index]);
    }

    function closeDropDownList() {
        var autocompleteItemsElement = containerElement.querySelector(".autocomplete-items");
        if (autocompleteItemsElement) {
            containerElement.removeChild(autocompleteItemsElement);
        }

        focusedItemIndex = -1;
    }

    function addIcon(buttonElement) {
        var svgElement = document.createElementNS("http://www.w3.org/2000/svg", 'svg');
        svgElement.setAttribute('viewBox', "0 0 24 24");
        svgElement.setAttribute('height', "24");

        var iconElement = document.createElementNS("http://www.w3.org/2000/svg", 'path');
        iconElement.setAttribute("d", "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z");
        iconElement.setAttribute('fill', 'currentColor');
        svgElement.appendChild(iconElement);
        buttonElement.appendChild(svgElement);
    }

    /* Close the autocomplete dropdown when the document is clicked.
      Skip, when a user clicks on the input field */
    document.addEventListener("click", function (e) {
        if (e.target !== inputElement) {
            closeDropDownList();
        } else if (!containerElement.querySelector(".autocomplete-items")) {
            // open dropdown list again
            var event = document.createEvent('Event');
            event.initEvent('input', true, true);
            inputElement.dispatchEvent(event);
        }
    });

}

// With much inspiration from https://www.geoapify.com/tutorial/address-autofill-form

function updateClearButtons() {
    document.querySelectorAll('.autocomplete-container').forEach(
        c => {
            clearButton = c.querySelector('.clear-button');
            if (!clearButton) return
            input = c.querySelector('input')
            if (!input.value) {
                clearButton.classList.remove("visible");
            } else {
                clearButton.classList.add("visible");
            }
        }
    )
}

function clearAllFields() {
    var streetInput = document.getElementById("autocomplete-container-street-input")
    var suburbInput = document.getElementById("autocomplete-container-suburb-input")
    var postcodeInput = document.getElementById("autocomplete-container-postcode-input")
    var cityInput = document.getElementById("autocomplete-container-city-input")
    var countryInput = document.getElementById("autocomplete-container-country-input")
    var longitudeInput = document.getElementById("autocomplete-container-longitude-input")
    var latitudeInput = document.getElementById("autocomplete-container-latitude-input")

    streetInput.value = '';
    suburbInput.value = '';
    postcodeInput.value = '';
    cityInput.value = '';
    countryInput.value = '';
    longitudeInput.value = '';
    latitudeInput.value = '';
}

const cityInput = document.getElementById("autocomplete-container-city-input")
const countryInput = document.getElementById("autocomplete-container-country-input")
const longitudeInput = document.getElementById("autocomplete-container-longitude-input");
const latitudeInput = document.getElementById("autocomplete-container-latitude-input");
let storeCountry = countryInput.value;
let storeCity = cityInput.value;

function updateCoordinates(data) {
    var long = document.getElementById("autocomplete-container-longitude-input")
    var lat = document.getElementById("autocomplete-container-latitude-input")

    if (data && data.properties.lon) {
        long.value = data.properties.lon;
    }
    if (data && data.properties.lat) {
        lat.value = data.properties.lat;

    }
    storeCountry = countryInput.value;
    storeCity = cityInput.value;
}

addressAutocomplete(document.getElementById("autocomplete-container-street"), (street) => {
    var streetInput = document.getElementById("autocomplete-container-street-input")
    var suburbInput = document.getElementById("autocomplete-container-suburb-input")
    var postcodeInput = document.getElementById("autocomplete-container-postcode-input")
    var cityInput = document.getElementById("autocomplete-container-city-input")
    var countryInput = document.getElementById("autocomplete-container-country-input")

    if (street) clearAllFields();

    if (street && street.properties.street) {
        streetInput.value = street.properties.address_line1 || '';
    }
    if (street && street.properties.suburb) {
        suburbInput.value = street.properties.suburb;
    }
    if (street && street.properties.postcode) {
        postcodeInput.value = street.properties.postcode;
    }
    if (street && street.properties.city) {
        cityInput.value = street.properties.city;
    }
    if (street && street.properties.country) {
        countryInput.value = street.properties.country;
    }
    updateCoordinates(street);
    updateClearButtons();
}, {});


addressAutocomplete(document.getElementById("autocomplete-container-country"), (country) => {
    var countryElement = document.getElementById("autocomplete-container-country-input")

    if (country) clearAllFields();

    if (country && country.properties.country) {
        countryElement.value = country.properties.country;
    }
    updateCoordinates(country);
    updateClearButtons();
}, {
    type: "country"
});

addressAutocomplete(document.getElementById("autocomplete-container-city"), (city) => {
    var cityInput = document.getElementById("autocomplete-container-city-input")
    var countryInput = document.getElementById("autocomplete-container-country-input")

    if (city) clearAllFields();

    if (city && city.properties.city) {
        cityInput.value = city.properties.city;
    }
    if (city && city.properties.country) {
        countryInput.value = city.properties.country;
    }
    updateCoordinates(city);
    updateClearButtons();
}, {
    type: 'city'
});

addressAutocomplete(document.getElementById("autocomplete-container-suburb"), (suburb) => {
    var suburbInput = document.getElementById("autocomplete-container-suburb-input")
    var postcodeInput = document.getElementById("autocomplete-container-postcode-input")
    var cityInput = document.getElementById("autocomplete-container-city-input")
    var countryInput = document.getElementById("autocomplete-container-country-input")

    if (suburb) clearAllFields();

    if (suburb && suburb.properties.suburb) {
        suburbInput.value = suburb.properties.suburb;
    }
    if (suburb && suburb.properties.postcode) {
        postcodeInput.value = suburb.properties.postcode;
    }
    if (suburb && suburb.properties.city) {
        cityInput.value = suburb.properties.city;
    }
    if (suburb && suburb.properties.country) {
        countryInput.value = suburb.properties.country;
    }
    updateCoordinates(suburb);
    updateClearButtons();
}, {});

addressAutocomplete(document.getElementById("autocomplete-container-postcode"), (postcode) => {
    var suburbInput = document.getElementById("autocomplete-container-suburb-input")
    var postcodeInput = document.getElementById("autocomplete-container-postcode-input")
    var cityInput = document.getElementById("autocomplete-container-city-input")
    var countryInput = document.getElementById("autocomplete-container-country-input")

    if (postcode) clearAllFields();

    if (postcode && postcode.properties.suburb) {
        suburbInput.value = postcode.properties.suburb;
    }
    if (postcode && postcode.properties.postcode) {
        postcodeInput.value = postcode.properties.postcode;
    }
    if (postcode && postcode.properties.city) {
        cityInput.value = postcode.properties.city;
    }
    if (postcode && postcode.properties.country) {
        countryInput.value = postcode.properties.country;
    }
    updateCoordinates(postcode);
    updateClearButtons();
}, {
    type: "postcode"
});

// From ChatGPT: Ensures the clear buttons appear when a page refreshes/when the form is repopulated after errors
document.addEventListener("DOMContentLoaded", function () {
    updateClearButtons();
});

/** Code to clear the long and lat whenever they are filled and then country or city fields are messed with**/
function checkCords() {
    if (cityInput.value !== storeCity || countryInput.value !== storeCountry) {
        longitudeInput.value = null;
        latitudeInput.value = null;
    }
}

cityInput.addEventListener("input", checkCords)
countryInput.addEventListener("input", checkCords)