package dbrighthd.wildfiregendermodplugin.networking.minecraft;

/**
 * Utility class for Minecraft-related networking.
 * <br />
 * Many values are shamelessly taken from <a href="https://minecraft.wiki/w/Java_Edition_protocol/Packets">Minecraft.Wiki</a>
 *
 * @author winnpixie
 */
public final class CraftDataConstants {
    /**
     * For use with variable-length numbers.
     */
    public static final int SEGMENT_BITS = 0x7F;

    /**
     * For use with variable-length numbers. Indicates the next bit is part of this number.
     */
    public static final int CONTINUE_BIT = 0x80;

    private CraftDataConstants() {
    }
}
