package dbrighthd.wildfiregendermodplugin.wildfire;

/**
 * Utility class for interoperability with Wildfire's Female Gender Mod.
 *
 * @author winnpixie
 */
public final class ModConstants {
    /**
     * The payload namespace for Wildfire's Female Gender Mod.
     */
    public static final String MOD_ID = "wildfire_gender";

    /**
     * Payload channel used by mod users to send their bodily configuration to the server.
     */
    public static final String SEND_GENDER_INFO = MOD_ID + ":send_gender_info";

    /**
     * Functions similarly to {@link ModConstants#SEND_GENDER_INFO}, but used by Forge users.
     */
    public static final String FORGE = MOD_ID + ":main_channel";

    /**
     * Payload channel used the server to send mod users others' bodily configurations.
     */
    public static final String SYNC = MOD_ID + ":sync";

    private ModConstants() {
    }
}
