const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

let roomId;
let chatName;
let currentTime;
let messageInput;

const livestreamData = document.getElementById('livestream-data');
const userId = parseInt(livestreamData.getAttribute('data-user-id'));
const livestreamId = livestreamData.getAttribute('data-livestream-id');
const isHost = livestreamData.getAttribute('data-is-host') === 'true';
const hostId = parseInt(livestreamData.getAttribute('data-host-id'));
let stompBaseUrl = window.location.pathname;

// Set the base URL based on the environment
if (stompBaseUrl.startsWith("/prod")) {
    stompBaseUrl = "/prod";
} else if (stompBaseUrl.startsWith("/test")) {
    stompBaseUrl = "/test";
} else {
    stompBaseUrl = "";
}

const stompClient = new StompJs.Client({
    brokerURL: stompBaseUrl + '/grapevine/'
});

function setRoomId(id) {
    console.log('Setting roomId to ' + id)
    roomId = id;
}

/**
 * Subscribes to the chat for a stream
 */
stompClient.onConnect = (frame) => {
    setConnected(true);
    console.log('Connected: ' + frame);
    stompClient.subscribe('/topic/' + roomId, (response) => {
        let responseJson = JSON.parse(response.body)
        showMessage(responseJson);
    });
    addLazyLoader()
};

stompClient.onWebSocketError = (error) => {
    console.error('Error with WebSocket', error);
};

stompClient.onStompError = (frame) => {
    console.error('Broker reported error: ' + frame.headers['message']);
    console.error('Additional details: ' + frame.body);
};

function setConnected(connected) {
    // document.getElementById('connect').disabled = connected;
    // document.getElementById('disconnect').disabled = !connected;
    document.getElementById('conversation').style.display = connected ? 'block' : 'none';
    document.getElementById('greetings').innerHTML = "";
}

/**
 * Connects to the chat for the current stream
 */
function connect() {
    const submitButton = document.getElementById('send-message-button');
    const loader = document.getElementById('send-message-loader');
    const submitCommentIcon = document.getElementById('send-message-icon');
    stompClient.activate();

    //connection state checking where stompClient state: 2 - disconnected | state: 0 = connected from stomp.umd.js
    const stateCheckInterval = setInterval(() => {
        if (stompClient.state === 2) {
            submitButton.disabled = true;
            loader.style.display = "block";
            submitCommentIcon.style.display = "none";
        } else if (stompClient.state === 0) {
            submitButton.disabled = false;
            loader.style.display = "none";
            submitCommentIcon.style.display = "block";
            clearInterval(stateCheckInterval);
        }
    }, 100);
}

/**
 * Disconnects from the current stream's chat
 */
function disconnect() {
    stompClient.deactivate();
    setConnected(false);
    console.log('Disconnected');
}

/**
 * Sends a message via stomp
 * @param roomId the ID of the stream/chatroom the user is in
 */
function sendMessage(roomId) {
    currentTime = new Date();
    messageInput = document.getElementById('message-input').value;
    stompClient.publish({
        destination: '/app/chat/' + roomId,
        body: JSON.stringify({'message': messageInput, 'streamId': roomId, 'name': chatName, 'timePosted': currentTime, 'userId': userId})
    });
}

/**
 * Displays a message in the chat
 * @param message the message to display (including sender etc)
 * @param insertAtTop whether the message should be inserted at the top (is a lazily-loaded old message)
 */
function showMessage(message, insertAtTop = false) {
    const greetings = document.getElementById('greetings');
    const row = document.createElement('tr');
    row.className = "chat-message"
    row.id = "chat-" + message.chatId
    const timePosted = document.createElement('td');
    const name = document.createElement('td');
    const content = document.createElement('td');
    console.warn(message)

    //new zealand time
    timePosted.textContent = new Date(message.timePosted + 'Z').toLocaleTimeString('en-NZ', {hour: '2-digit', minute: '2-digit'})
    timePosted.className = "chat-time";

    const messageUserId = message.userId == null ? message.owner.id : message.userId;

    name.addEventListener('click', () => {
        window.open(`${stompBaseUrl}/user/profile/${messageUserId}`, '_blank');
    });
    console.warn(`messageUserId: ${typeof messageUserId}, userId: ${typeof userId}, hostId: ${typeof hostId}`)
    if (messageUserId === userId) {
        name.style.fontWeight = "bold";
    }
    if (hostId === messageUserId) {
        name.style.fontWeight = "bold";
        name.style.color = "black";
    }
    name.textContent = message.name == null ? message.owner.firstName + " " + message.owner.lastName : message.name;
    name.className = "chat-name-chip";
    name.style.marginTop ="5px"
    name.style.cursor = "pointer";
    name.addEventListener('mouseover', () => {
        name.style.textDecoration = "underline";
        console.log(messageUserId)
    });
    name.addEventListener('mouseout', () => {
        name.style.textDecoration = "none";
    });
    content.textContent = message.message;
    content.className = "chat-text";
    row.appendChild(timePosted)
    row.appendChild(name);
    row.appendChild(content);
    row.dataset.timePosted = message.timePosted;
    if (insertAtTop) {
        greetings.insertBefore(row, greetings.firstChild);
    } else {
        greetings.appendChild(row);
        row.scrollIntoView()
    }
    document.getElementById('message-input').value = "";
}

/**
 * Establishes connection to chat when joining a stream
 */
document.addEventListener('DOMContentLoaded', () => {
    connect();
    // const connectButton = document.getElementById('connect');
    // const disconnectButton = document.getElementById('disconnect');
    const submitButton = document.getElementById('send-message-button');
    chatName = document.getElementById('chat-name-label').innerText; //doing something

    // Prevent form submit behavior
    document.querySelectorAll('form').forEach(form => {
        form.addEventListener('submit', (e) => e.preventDefault());
    });

    const roomIdFromLabel = document.getElementById('room-id-hidden-label');
    if (roomIdFromLabel) {
        setRoomId(roomIdFromLabel.innerText);
    }


    // connectButton.addEventListener('click', connect);
    // disconnectButton.addEventListener('click', disconnect);
    submitButton.addEventListener('click', () => addMessage());
});

/**
 * sends a post request to the sever to validate the users chat comment
 */
function addMessage() {
    const message = document.getElementById('message-input').value;
    const errorField = document.getElementById('input-error-message');
    const submitButton = document.getElementById('send-message-button');
    const loader = document.getElementById('send-message-loader');
    const submitCommentIcon = document.getElementById('send-message-icon');

    const userId = document.getElementById('livestream-data').userId;

    currentTime = new Date()

    submitButton.disabled = true;
    loader.style.display = "block";
    submitCommentIcon.style.display = "none";

    fetch(`${stompBaseUrl}/chat/add`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        },
        body: JSON.stringify({
            "message": message,
            "streamId": roomId,
            "name": chatName,
            "timePosted": currentTime.toISOString().slice(0, -1),
            "userId": userId
        })
    })
        .then(async response => {
            let data = await response
            if (response.status === 200) {
                sendMessage(roomId)
                errorField.style.display = "none";
                submitButton.disabled = false;
                loader.style.display = "none";
                submitCommentIcon.style.display = "block";

            } else if (response.status === 400) {
                errorField.textContent = await response.text();
                errorField.style.display = "block";
                submitButton.disabled = false;
                loader.style.display = "none";
                submitCommentIcon.style.display = "block";
            } else {
                errorField.textContent = "Error sending chat";
                errorField.style.display = "block";
                submitButton.disabled = false;
                loader.style.display = "none";
                submitCommentIcon.style.display = "block";
            }

        })
        .catch(error => {
            errorField.textContent = "Error sending chat";
            errorField.style.display = "block";
            submitButton.disabled = false;
            loader.style.display = "none";
            submitCommentIcon.style.display = "block";
        });
}

/**
 * Fetches the (up to) ten most recent chats before the oldest message currently displayed
 */
function getOlderMessages() {
    let oldestChat = document.getElementById('greetings').querySelector(".chat-message");
    let earliestCommentTime;
    if (oldestChat) {
        earliestCommentTime = oldestChat.dataset.timePosted;
    } else {
        earliestCommentTime = new Date().toISOString().slice(0, -1) // removes the Z at the end of the string
    }

    fetch(`${stompBaseUrl}/chat/${roomId}/get-old-comments?olderThanString=${earliestCommentTime}`, {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
        .then(async response => {
            let data = await response.json()
            data.chats.forEach(chat => {
                showMessage(chat, true)
            })
            if (data.chats && data.chats.length > 0) {
                document.getElementById("chat-" + data.chats[0].chatId).scrollIntoView()
                addLazyLoader()
            } else {    // No need to have a lazy-loader at the top if we already have all older messages
                let existingLazyLoader = document.getElementById("lazy-loader")
                if (existingLazyLoader) {
                    existingLazyLoader.remove()
                }
            }
        }).catch(error => {
        const errorField = document.getElementById('input-error-message');
        const loader = document.getElementById('send-message-loader');
        const submitCommentIcon = document.getElementById('send-message-icon');

        errorField.textContent = "Error sending chat";
        errorField.style.display = "block";
        submitButton.disabled = false;
        loader.style.display = "none";
        submitCommentIcon.style.display = "block";
    });
}

/**
 * Adds an event listener equivalent to see when an HTML element enters view
 * https://stackoverflow.com/a/66394121
 * @param element the element for which we are trying to determine visibility
 * @param callback what to do if the element is visible
 */
function onVisible(element, callback) {
    new IntersectionObserver((entries, observer) => {
        entries.forEach(entry => {
            if(entry.intersectionRatio > 0) {
                callback(element);
            }
        });
    }).observe(element);
    if(!callback) return new Promise(r => callback=r);
}

/**
 * Adds an invisible element to the top of the chat so that, if a user scrolls to the top, the element enters view and
 * triggers a lazy load of more old chats. Shows spinner while chat is loading
 */
function addLazyLoader() {

    // Remove existing lazy loader if there is one, to avoid duplicates
    let existingLazyLoader = document.getElementById("lazy-loader")
    if (existingLazyLoader) {
        existingLazyLoader.remove()
    }

    const chats = document.getElementById('greetings');

    let lazyLoaderRow = chats.insertRow(0);
    lazyLoaderRow.id = "lazy-loader"
    lazyLoaderRow.style.display = "flex";
    lazyLoaderRow.style.width = "100%";
    lazyLoaderRow.style.justifyContent = "center";
    let lazyLoader = lazyLoaderRow.insertCell(0)
    let spinner = document.createElement("div")
    spinner.className = "loader"
    spinner.id = "fetch-comments-loader"
    lazyLoader.appendChild(spinner)

    onVisible(lazyLoaderRow, async () => {
        spinner.style.display = "block"
        await new Promise(r => setTimeout(r, 1000));    // Makes it feel more impressive
        getOlderMessages();
        spinner.style.display = "none"
    })
}