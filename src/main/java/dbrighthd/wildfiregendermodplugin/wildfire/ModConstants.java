package dbrighthd.wildfiregendermodplugin.wildfire;

/**
 * Utility class for interoperability with Wildfire's Female Gender Mod.
 *
 * @author winnpixie
 */
public final class ModConstants {
    /**
     * The payload namespace for Wildfire's Female Gender Mod up to 5.0.0-Beta.4 (protocols 2-5).
     */
    public static final String MOD_ID = "wildfire_gender";

    /**
     * Payload channel used by mod users to send their bodily configuration to the
     * server.
     */
    public static final String SEND_GENDER_INFO = MOD_ID + ":send_gender_info";

    /**
     * Functions similarly to {@link ModConstants#SEND_GENDER_INFO}, but used by
     * Forge users.
     */
    public static final String FORGE = MOD_ID + ":main_channel";

    /**
     * Payload channel used the server to send mod users others' bodily
     * configurations.
     */
    public static final String SYNC = MOD_ID + ":sync";

    /**
     * Payload channel used by mod users to identify their sync protocol version.
     */
    public static final String HELLO_SERVERBOUND = MOD_ID + ":serverbound/hello";

    /**
     * Payload channel used by the server to identify its sync protocol version.
     */
    public static final String HELLO_CLIENTBOUND = MOD_ID + ":clientbound/hello";

    /**
     * The play-phase hello handshake version spoken by protocol 5.
     */
    public static final int HELLO_PROTOCOL_VERSION = 1;

    /**
     * The payload namespace used from mod 5.0.0-Beta.5 onwards (protocol 6).
     */
    public static final String V6_MOD_ID = "female_gender_mod";

    /**
     * Protocol 6: mod users send their own configuration to the server.
     */
    public static final String V6_SYNC_SERVERBOUND = V6_MOD_ID + ":serverbound/sync";

    /**
     * Protocol 6: the server sends mod users others' configurations.
     */
    public static final String V6_SYNC_CLIENTBOUND = V6_MOD_ID + ":clientbound/sync";

    /**
     * Protocol 6: the server lists its supported handshake versions during the configuration phase.
     */
    public static final String V6_HELLO_CLIENTBOUND = V6_MOD_ID + ":clientbound/hello";

    /**
     * Protocol 6: the client answers with the handshake version it speaks.
     */
    public static final String V6_HELLO_SERVERBOUND = V6_MOD_ID + ":serverbound/hello";

    /**
     * The configuration-phase handshake version spoken by protocol 6 ({@code SyncHelloPacket.VERSION} in the mod).
     */
    public static final int V6_HELLO_VERSION = 2;

    private ModConstants() {
    }
}
