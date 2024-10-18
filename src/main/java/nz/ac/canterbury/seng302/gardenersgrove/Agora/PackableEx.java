package nz.ac.canterbury.seng302.gardenersgrove.Agora;

/**
 * Class used from Agora.
 * https://docs.agora.io/en/interactive-live-streaming/get-started/get-started-sdk?platform=web
 * https://github.com/AgoraIO
 */
public interface PackableEx extends Packable {
    void unmarshal(ByteBuf in);
}
