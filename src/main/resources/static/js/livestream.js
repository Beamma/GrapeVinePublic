import AgoraRTC from "agora-rtc-sdk-ng";

let baseUrl = window.location.pathname;

// Set the base URL based on the environment
if (baseUrl.startsWith("/prod")) {
    baseUrl = "/prod";
} else if (baseUrl.startsWith("/test")) {
    baseUrl = "/test";
} else {
    baseUrl = "";
}

document.addEventListener("DOMContentLoaded", function () {
    // Get variables
    const livestreamData = document.getElementById('livestream-data');
    const userId = livestreamData.getAttribute('data-user-id');
    const livestreamId = livestreamData.getAttribute('data-livestream-id');
    const isHost = livestreamData.getAttribute('data-is-host') === 'true';

    const playingIcon = document.getElementById('playIcon');
    const pausedIcon = document.getElementById('pauseIcon');
    const mutedIcon = document.getElementById('muteIcon');
    const unmutedIcon = document.getElementById('unmuteIcon');
    const fullscreenButton = document.getElementById('fullscreenBtn');
    const playPauseButton = document.getElementById('playPauseBtn');
    const muteUnmuteButton = document.getElementById('muteBtn');
    const volumeSlider = document.getElementById('volumeSlider');

    let muted = false;
    let paused = false;

    // Log variables
    console.log("Is Host: ", isHost);
    console.log("Livestream ID: ", livestreamId);

    // Set rtc
    let rtc = {
        localAudioTrack: null,
        localVideoTrack: null,
        client: null,
    };

    // User options
    let options = {
        appId: null,
        channel: livestreamId,
        token: null,
        uid: userId,
    };

    // Fetch the token from the backend
    async function fetchToken(channelName, uid) {
        const response = await fetch(baseUrl + `/agora/rtcToken?channelName=${channelName}&userId=${uid}`);
        return await response.text();
    }

    async function fetchAppId() {
        const response = await fetch(baseUrl + `/agora/appId`);
        return await response.text();
    }

    function showLivestreamEndedMessage() {
        const videoContainer = document.getElementById("video-container");
        videoContainer.innerHTML = `
            <div>
                This livestream has ended.
                <a href="../browse" id="termsLink">Browse Other Livestreams</a>
            </div>`;
        document.getElementById("video-controls").style.display = "none";
    }


    // Initialize the Agora client and join the livestream
    async function initializeAgora() {
        rtc.client = AgoraRTC.createClient({ mode: "live", codec: "vp8" });
        options.appId = await  fetchAppId();
        let remoteAudioTrack = null;
        let video = null;

        if (isHost) {
            rtc.client.setClientRole("host");
            options.token = await fetchToken(options.channel, options.uid);

            await rtc.client.join(options.appId, options.channel, options.token, options.uid);
            rtc.localAudioTrack = await AgoraRTC.createMicrophoneAudioTrack();
            rtc.localVideoTrack = await AgoraRTC.createCameraVideoTrack();
            await rtc.client.publish([rtc.localAudioTrack, rtc.localVideoTrack]);

            const localPlayerContainer = document.createElement("div");
            localPlayerContainer.id = options.uid;
            localPlayerContainer.style.width = "640px";
            localPlayerContainer.style.height = "480px";
            document.getElementById("video-container").removeChild(document.getElementById("fetching-video"));
            document.getElementById("video-container").append(localPlayerContainer);
            // PUT THE viewer count chip in the top left corner of the video

            rtc.localVideoTrack.play(localPlayerContainer, {mirror: false});

        } else {
            rtc.client.setClientRole("audience");
            options.token = await fetchToken(options.channel, options.uid);

            await rtc.client.join(options.appId, options.channel, options.token, options.uid);

            // Create timeout
            let videoPublished = false;
            const videoTimeout = setTimeout(() => {
                if (!videoPublished) {
                    document.getElementById("video-container").removeChild(document.getElementById("fetching-video"));
                    showLivestreamEndedMessage()
                }
            }, 10000)

            rtc.client.on("user-published", async (user, mediaType) => {
                await rtc.client.subscribe(user, mediaType);
                if (mediaType === "video") {
                    videoPublished = true;
                    clearTimeout(videoTimeout);
                    const remoteVideoTrack = user.videoTrack;
                    const remotePlayerContainer = document.createElement("div");
                    remotePlayerContainer.id = user.uid.toString();
                    remotePlayerContainer.style.width = "640px";
                    remotePlayerContainer.style.height = "480px";
                    document.getElementById("video-container").removeChild(document.getElementById("fetching-video"));
                    document.getElementById("video-container").append(remotePlayerContainer);
                    remoteVideoTrack.play(remotePlayerContainer);
                    video = remotePlayerContainer.getElementsByTagName('video')[0];
                    video.addEventListener('pause', (e) => {
                        e.stopImmediatePropagation();
                        e.stopPropagation();
                    }, true);
                    document.getElementById("video-controls").style.display = "flex";
                }

                if (mediaType === "audio") {
                    remoteAudioTrack = user.audioTrack;
                    remoteAudioTrack.play();
                }
            });

            // Stream ended
            rtc.client.on("user-unpublished", async () => {
                showLivestreamEndedMessage();
            });

        }
        if (!isHost) {
            playPauseButton.addEventListener('click', function () {
                if (paused) {
                    remoteAudioTrack.setVolume(volumeSlider.value * 100);
                    video.play();
                    paused = false;
                } else {
                    remoteAudioTrack.setVolume(0);
                    paused = true;
                    video.pause();
                }
                playingIcon.style.display = paused ? 'inline' : 'none';
                pausedIcon.style.display = paused ? 'none' : 'inline';
            })

            muteUnmuteButton.addEventListener('click', function () {
                if (muted) {
                    muted = false;
                    if (!paused) {
                        remoteAudioTrack.setVolume(volumeSlider.value * 100);
                    }
                } else {
                    muted = true;
                    remoteAudioTrack.setVolume(0);
                }
                mutedIcon.style.display = muted ? 'inline' : 'none';
                unmutedIcon.style.display = muted ? 'none' : 'inline';
            })

            volumeSlider.addEventListener('input', function () {
                if (!paused) {
                    remoteAudioTrack.setVolume(volumeSlider.value * 100);
                }
                muted = volumeSlider.value === 0;
                mutedIcon.style.display = muted ? 'inline' : 'none';
                unmutedIcon.style.display = muted ? 'none' : 'inline';

            })

            fullscreenButton.addEventListener('click', function () {
                video.requestFullscreen();
            })
        }


    }

    initializeAgora();
    getViewerCount(livestreamId)
});

/**
 * This function sends a GET request to the agora api to get the viewer count of a livestream
 * @param livestreamId the id of the livestream to get the viewer count for
 */
async function getViewerCount(livestreamId) {
    const url = `${baseUrl}/agora/${livestreamId}/viewer-count`;
    try {
        const response = await fetch(url, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });
        if (response.ok) {
            const viewerCount = await response.text()
            const viewerCountText = document.getElementById('viewer-count-text');
            viewerCountText.innerText = viewerCount;
        }
    } catch (error) {
        console.error('Error:', error);
    }
    setTimeout(() => getViewerCount(livestreamId), 5000);
}
