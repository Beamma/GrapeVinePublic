let currentPageMap = new Map();

//Prevent comment ids from being excluded after page is reloaded
document.addEventListener('DOMContentLoaded', (e) => {
    document.getElementById('new-comment-ids').value = '';
});

/**
 * Makes a get request to retrieve comments and adds them to the post which called it
 * @param loadMoreBtn references the button that called it
 * @param postId id of the post
 */
function fetchComments(loadMoreBtn, postId) {
    let currentPage = currentPageMap.get(postId);
    let excludedIds = document.getElementById('new-comment-ids').value.split(",")

    fetch(`../post/${postId}/comments?page=${currentPage}`, {
    method: 'GET',
    headers: {
    'Content-Type': 'application/json',
    'excludedIdsHeader': excludedIds,
    [csrfHeader]: csrfToken
    }
    })
    .then(response => response.json())
    .then(newComments => {
        if (newComments.message === "No comments found") {
            loadMoreBtn.style.display = 'none';
            return;
        }
        let comments = newComments.comments;
        comments.forEach(comment => {
            const newComment = document.createElement('div');
            newComment.classList.add('comment');
            newComment.innerHTML = `
                                <a href="/user/profile/${comment.user.id}">
                                    <img src="${comment.user.profileImageBase64}" class="small-profile-image" onerror="this.onerror=null; this.src='../img/profileicon.png';">
                                </a>
                                <div class="comment-content">
                                    <div class="comment-heading">
                                        <span>${comment.user.firstName} ${comment.user.noLastName ? '' : ' ' + comment.user.lastName}</span>
                                        <span>${comment.timeSincePosted}</span>
                                    </div>
                                    <div class="comment-body">
                                        <span>${comment.text}</span>
                                        <div class="comment-like-and-reply-buttons-container">
                                            <span id="comment-like-count-${comment.id}" class="like-count"></span>
                                            <img src="/img/unliked-heart.svg" onclick="commentLikeButtonClicked(${comment.id})" id="comment-heart-icon-${comment.id}" style="margin-left: 10px; cursor: pointer ">
                                        </div>
                                    </div>
                                </div>`;

            const postCard = loadMoreBtn.closest('.post-card');
            const commentsContainer = postCard.querySelector('.post-card-comments');
            commentsContainer.appendChild(newComment);
            loadCommentLikeInformation(comment.id)

            if (comments.length < 10) {
            loadMoreBtn.style.display = 'none';
            }
        });
        currentPage++;
        currentPageMap.set(postId, currentPage);
    });
}

    //with help from chatGPT to handle response
    document.querySelectorAll(".loadMoreComments").forEach(loadMoreBtn => {
    const postId = loadMoreBtn.getAttribute('data-post-id');
    currentPageMap.set(postId, 0);

    loadMoreBtn.addEventListener(
    'click', () => {
    fetchComments(loadMoreBtn, postId);
})
});


