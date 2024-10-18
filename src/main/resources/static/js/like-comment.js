
likedHeartUrl = "../img/liked-heart.svg";
unlikedHeartUrl = "../img/unliked-heart.svg";

const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

/**
 * Sends the like of a comment to the server, and updates the like count for the comment
 * @param commentId the ID of the comment being liked
 * @returns {Promise<void>} unused
 */
async function likeComment(commentId) {
    const url = `../comment/${commentId}/like`;
    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        });
        if (response.ok) {
            document.getElementById(`comment-like-count-${commentId}`).innerText = await response.json();
        } else if (response.status === 400 && await response.text() === "Comment not found") {
            localStorage.setItem('commentNotFound', 'true');
            window.location.reload();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

/**
 * Sends the unlike of a comment to the server, and updates the like count for the comment
 * @param commentId the ID of the comment being unliked
 * @returns {Promise<void>} unused
 */
async function unlikeComment(commentId) {
    const url = `../comment/${commentId}/like`;
    try {
        const response = await fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        });
        if (response.ok) {
            document.getElementById(`comment-like-count-${commentId}`).innerText = await response.json();
        } else if (response.status === 400 && response.statusText === "Comment not found") {
            localStorage.setItem('commentNotFound', 'true');
            window.location.reload();
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

/**
 * Checks whether the user has already liked a comment
 * @param commentId the ID of the comment
 * @returns {Promise<void>} unused
 */
async function loadCommentLikeInformation(commentId) {
    const url = `../comment/${commentId}/like`;
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        });
        if (response.ok) {
            const responseJson = await response.json()
            checkCommentNotFound();
            document.querySelector(`#comment-heart-icon-${commentId}`).src = responseJson.likeExists ? likedHeartUrl : unlikedHeartUrl
            document.getElementById(`comment-like-count-${commentId}`).innerText = responseJson.likeCount;
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

/**
 * Handler for the like button for a comment being clicked (to like or unlike the comment)
 * @param commentId the id of the comment of which the like button has been clicked
 */
function commentLikeButtonClicked(commentId) {
    const heartIcon = document.querySelector(`#comment-heart-icon-${commentId}`);
    heartIcon.style.opacity = 0;
    heartIcon.classList.add('heart-fade'); // Add the animation class

    setTimeout(() => {
        if (heartIcon.src.endsWith(likedHeartUrl.slice(2))) {
            heartIcon.src = unlikedHeartUrl;
            unlikeComment(commentId);
        } else {
            heartIcon.src = likedHeartUrl;
            likeComment(commentId);
        }
        heartIcon.style.opacity = 1;
        setTimeout(() => {
            heartIcon.classList.remove('heart-fade');
        }, 500);
    }, 50);
}

/**
 * Checks if commentNotFound flag is set and shows an alert if it is
 */
function checkCommentNotFound() {
    if (localStorage.getItem('commentNotFound')) {
        localStorage.removeItem('commentNotFound');
        alertSpan.textContent = "Comment not found, post has been deleted";
        showDeleteAlert();
    }
}
