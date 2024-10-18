const newCommentIdList = []

/**
 * Submitting the comment on the feed page
 * @param postId the post that the user is commenting on
 */
function submitComment(postId) {
    const comment = document.getElementById('comment-input' + postId).value;
    const commentInput = document.getElementById('comment-input' + postId);
    const errorField = document.getElementById('comment-error-message' + postId);
    const submitButton = document.getElementById('submit-comment-' + postId);
    const loader = document.getElementById('submit-comment-' + postId + '-loader');
    const submitCommentIcon = document.getElementById('submit-comment-'+ postId + '-icon');
    const newCommentIdInput = document.getElementById('new-comment-ids')

    submitButton.disabled = true;
    loader.style.display = "block";
    submitCommentIcon.style.display = "none";

    fetch(`../post/${postId}/comment`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({"comment": comment})
    })
        .then(async response => {
            let data = await response
            if (response.status === 201) {
                // Comment contains no errors, insert it into the comment list without refresh
                let commentId = parseInt(await response.text());
                newCommentIdList.push(commentId);
                newCommentIdInput.value = newCommentIdList.join(",");
                commentInput.value = "";
                errorField.style.display = "none";
                await addCommentToPost(postId, commentId);
                submitButton.disabled = false;
                loader.style.display = "none";
                submitCommentIcon.style.display = "block";
            } else if (response.status === 400) {
                // Comment has errors
                errorField.textContent = await response.text();
                errorField.style.display = "block";
                submitButton.disabled = false;
                loader.style.display = "none";
                submitCommentIcon.style.display = "block";
                if (response.headers.get("Fifth-Inappropriate-Submission") === "true") {
                    await showInappropriateModal()
                }
            } else {
                // If the comment NOT created successfully,
                // and the comment is NOT erroneous,
                // then the user must be blocked so redirect to login page.
                const json = await data.json();
                if (json.redirected) {
                    window.location.href = json.url;
                }

            }

        }).catch(error => {
        submitButton.disabled = false;
    });
}

/**
 * Adding a comment to the post without reloading the page - we used CHATGPT for the basics of this function
 * @param postId the post that the user is commenting on
 * @param commentId id of the newly made comment
 */
async function addCommentToPost(postId, commentId) {
    const commentsList = document.getElementById('post-card-comments-' + postId);
    const submitButton = document.getElementById('submit-comment-' + postId);

    //Endpoint returns html text of the comment fragment generated with thymeleaf values
    let response = fetch(`../post/${postId}/comment/${commentId}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
    let comment = await response;
    let commentHtml = await comment.text();
    // This uses regex to extract the body content of the comment, which is the div containing the comment
    let bodyContent = commentHtml.match(/<body[^>]*>([\s\S]*?)<\/body>/i)[1];

    if (bodyContent && bodyContent.trim().startsWith('<div')) {
        //Parses the html by creating a div and setting the innerHTML
        let appendingComment = document.createElement('div');
        appendingComment.innerHTML = bodyContent;
        commentsList.prepend(appendingComment);
    }
}

/**
 * If the user's inappropriate submission count has reached five, this fetches and shows the inappropriate comment model
 */
async function showInappropriateModal() {
    // Endpoint returns warning modal
    let response = fetch('../warning-modal', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
    let modalHTML = await (await response).text();

    // This uses regex to extract the body content of the comment, which is the div containing the comment
    let bodyContent = modalHTML.match(/<body[^>]*>([\s\S]*?)<\/body>/i)[1];

    if (bodyContent) {
        //Parses the html by creating a div and setting the innerHTML
        let modal = document.createElement('div');
        modal.innerHTML = bodyContent;
        document.body.appendChild(modal)
    }
}

/**
 * Close modal function. Does not import properly, so need to add it separately
 */
function hideFifthInappropriateSubmissionModal() {
    document.getElementsByClassName('blocked-modal')[0].style.display = 'none';
}

/**
 * This toggles the add comment form as hidden/unhidden.
 */
function showCommentForm(postId) {
    const form = document.getElementById(`comment-form-` + postId);
    if (form.style.display !== "none") {
        form.style.display = "none"
    } else {
        form.style.display = "block"
    }
}

