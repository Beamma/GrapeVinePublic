/**
 * Provides a custom message when users try to input invalid dates
 * Source: https://angelika.me/2020/02/01/custom-error-messages-for-html5-form-validation/
 */
const this_script = document.currentScript;
let dateFieldNames = [];
try {
    dateFieldNames = this_script.attributes.getNamedItem("data-date-field-names").value.split(",");
} catch (TypeError) {}

for (dateField of dateFieldNames) {
    dateInput = document.querySelector(`input[name="${dateField}"]`);
    dateInput.addEventListener('invalid', function (event) {
        if (event.target.validity.badInput) {
            event.target.setCustomValidity('Date is not in valid format, DD/MM/YYYY');
        }
    })
    dateInput.addEventListener('change', function (event) {
        event.target.setCustomValidity('');
    })
}

