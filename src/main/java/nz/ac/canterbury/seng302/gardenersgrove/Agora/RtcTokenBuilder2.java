package nz.ac.canterbury.seng302.gardenersgrove.Agora;

/**
 * Class used from Agora.
 * https://docs.agora.io/en/interactive-live-streaming/get-started/get-started-sdk?platform=web
 * https://github.com/AgoraIO
 */
public class RtcTokenBuilder2 {
    public enum Role {
        /**
         * RECOMMENDED. Use this role for a voice/video call or a live broadcast, if
         * your scenario does not require authentication for
         * [Co-host](https://docs.agora.io/en/video-calling/get-started/authentication-workflow?#co-host-token-authentication).
         */
        ROLE_PUBLISHER(1),
        /**
         * Only use this role if your scenario require authentication for
         * [Co-host](https://docs.agora.io/en/video-calling/get-started/authentication-workflow?#co-host-token-authentication).
         *
         * @note In order for this role to take effect, please contact our support team
         * to enable authentication for Hosting-in for you. Otherwise, Role_Subscriber
         * still has the same privileges as Role_Publisher.
         */
        ROLE_SUBSCRIBER(2),;

        public int initValue;

        Role(int initValue) {
            this.initValue = initValue;
        }
    }

    /**
     * Build the RTC token with uid.
     *
     * @param appId:            The App ID issued to you by Agora. Apply for a new App ID from
     *                          Agora Dashboard if it is missing from your kit. See Get an App ID.
     * @param appCertificate:   Certificate of the application that you registered in
     *                          the Agora Dashboard. See Get an App Certificate.
     * @param channelName:      Unique channel name for the AgoraRTC session in the string format
     * @param uid:              User ID. A 32-bit unsigned integer with a value ranging from 1 to (2^32-1).
     *                          uid must be unique.
     * @param role:             ROLE_PUBLISHER: A broadcaster/host in a live-broadcast profile.
     *                          ROLE_SUBSCRIBER: An audience(default) in a live-broadcast profile.
     * @param tokenExpire:      represented by the number of seconds elapsed since now. If, for example,
     *                          you want to access the Agora Service within 10 minutes after the token is generated,
     *                          set tokenExpire as 600(seconds).
     * @param privilegeExpire: represented by the number of seconds elapsed since now. If, for example,
     *                          you want to enable your privilege for 10 minutes, set privilegeExpire as 600(seconds).
     * @return The RTC token.
     */
    public String buildTokenWithUid(String appId, String appCertificate, String channelName, int uid, Role role, int tokenExpire, int privilegeExpire) {
        return buildTokenWithUserAccount(appId, appCertificate, channelName, AccessToken2.getUidStr(uid), role, tokenExpire, privilegeExpire);
    }

    /**
     * Build the RTC token with account.
     *
     * @param appId:            The App ID issued to you by Agora. Apply for a new App ID from
     *                          Agora Dashboard if it is missing from your kit. See Get an App ID.
     * @param appCertificate:   Certificate of the application that you registered in
     *                          the Agora Dashboard. See Get an App Certificate.
     * @param channelName:      Unique channel name for the AgoraRTC session in the string format
     * @param account:          The user's account, max length is 255 Bytes.
     * @param role:             ROLE_PUBLISHER: A broadcaster/host in a live-broadcast profile.
     *                          ROLE_SUBSCRIBER: An audience(default) in a live-broadcast profile.
     * @param tokenExpire:      represented by the number of seconds elapsed since now. If, for example,
     *                          you want to access the Agora Service within 10 minutes after the token is generated,
     *                          set tokenExpire as 600(seconds).
     * @param privilegeExpire:  represented by the number of seconds elapsed since now. If, for example,
     *                          you want to enable your privilege for 10 minutes, set privilegeExpire as 600(seconds).
     * @return The RTC token.
     */
    public String buildTokenWithUserAccount(String appId, String appCertificate, String channelName, String account, Role role, int tokenExpire,
                                            int privilegeExpire) {
        AccessToken2 accessToken = new AccessToken2(appId, appCertificate, tokenExpire);
        AccessToken2.Service serviceRtc = new AccessToken2.ServiceRtc(channelName, account);

        serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_JOIN_CHANNEL, privilegeExpire);
        if (role == Role.ROLE_PUBLISHER) {
            serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_AUDIO_STREAM, privilegeExpire);
            serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_VIDEO_STREAM, privilegeExpire);
            serviceRtc.addPrivilegeRtc(AccessToken2.PrivilegeRtc.PRIVILEGE_PUBLISH_DATA_STREAM, privilegeExpire);
        }
        accessToken.addService(serviceRtc);

        try {
            return accessToken.build();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

}
