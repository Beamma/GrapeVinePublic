likedHeartUrl = "../img/liked-heart.svg";
unlikedHeartUrl = "../img/unliked-heart.svg";


/**
 * This function sends a PUT request to the server to like a post
 * @param postId the id of the post to like
 * @returns {Promise<void>} unused
 */
async function likePost(postId) {
    const url = `../post/${postId}/like`;
    try {
        const response = await fetch(url, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        });
        if (response.ok) {
            const updatedLikeCount = await response.json();
            document.querySelector(`#like-count-${postId}`).textContent = `${updatedLikeCount}`;

        }
    } catch (error) {
        console.error('Error:', error);
    }
}

/**
 * This function sends a DELETE request to the server to unlike a post
 * @param postId the id of the post to unlike
 * @returns {Promise<void>} unused
 */
async function unlikePost(postId) {
    const url = `../post/${postId}/like`;
    try {
        const response = await fetch(url, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        });
        if (response.ok) {
            const updatedLikeCount = await response.json();
            document.querySelector(`#like-count-${postId}`).textContent = `${updatedLikeCount}`;
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

/**
 * This function sends a GET request to the server to load the like information of a post
 * Fills the like count and sets the heart based on if the current user has liked the post
 * @param postId the id of the post to load the like information for
 * @returns {Promise<void>} unused
 */

async function loadPostLikeInformation(postId) {
    const url = `../post/${postId}/like`;
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
            let likeCount = responseJson.likeCount;
            let likeExists = responseJson.likeExists;
            if (likeExists === true) {
                document.querySelector(`#heart-icon-${postId}`).src = likedHeartUrl;
            } else {
                document.querySelector(`#heart-icon-${postId}`).src = unlikedHeartUrl;
            }

            document.querySelector(`#like-count-${postId}`).textContent = `${likeCount}`;
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

/**
 * This function is called when the like button heart is clicked
 * @param postId id
 */

function postLikeButtonClicked(postId) {
    const heartIcon = document.querySelector(`#heart-icon-${postId}`);
    heartIcon.style.opacity = 0;
    heartIcon.classList.add('heart-fade'); // Add the animation class
    setTimeout(() => {
        if (heartIcon.src.endsWith(likedHeartUrl.slice(2))) {
            heartIcon.src = unlikedHeartUrl;
            unlikePost(postId);
        } else {
            heartIcon.src = likedHeartUrl;
            likePost(postId);
        }
        heartIcon.style.opacity = 1;
        setTimeout(() => {
            heartIcon.classList.remove('heart-fade');
        }, 500);
    }, 50);
}