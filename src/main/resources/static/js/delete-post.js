const overlay = document.getElementById("confirm-overlay");
const confirmNoButton = document.getElementById("confirm-no");
const alertPopup = document.getElementById("delete-alert-popup");
const alertSpan = document.getElementById("delete-alert-span");
let current_id = 0;

function confirmNo() {
    hideConfirmModal()
}

function confirmYes() {
    overlay.style.display = "none";
    deletePost(current_id)
}

/**
 * id of post is passed through here and set for other functions
 * to access.
 * @param id the id of the post
 */
function showConfirmModal(id) {
    overlay.style.display = "flex";
    confirmNoButton.focus();
    current_id = id;
}

function hideConfirmModal() {
    overlay.style.display = "none";
}

/**
 * Function to call post delete endpoint
 * @param id id of post to delete
 */
function deletePost(id) {
    fetch(`../post/${id}`, {
        method: 'DELETE',
        redirect: 'follow',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
        .then(response => {
            if (response.ok) {
                // Copilot help for flag after reload
                localStorage.setItem('postDeleted', 'true');
                //Trigger reload on client side, only to reflect changes, no security issue
                window.location.reload()
            } else {
                console.error('Failed to delete the post')
                alertSpan.textContent = "Error deleting post!"
                alertPopup.style.backgroundColor = "red";
                showDeleteAlert();
            }
        })
        .catch(error => console.error('Error:', error))
}

/**
 * (Help from Copilot)
 * postDeleted flag persists after page is refreshed,
 * if present then show the alert.
 */
function checkPostDeletedFlag() {
    if (localStorage.getItem('postDeleted') === 'true') {
        showDeleteAlert();
        localStorage.removeItem('postDeleted');
    }
}

window.onload = function () {
    checkPostDeletedFlag();
    checkCommentNotFound();
};

function dismissDeleteAlert() {
    alertPopup.style.display = "none";
}

function showDeleteAlert() {
    alertPopup.style.display = "flex";
}
