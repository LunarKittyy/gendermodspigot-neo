package dbrighthd.wildfiregendermodplugin.utilities;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * <p>Methods to encode data to be sent to a client.</p>
 * <p>Methods shamelessly stolen from <a href="https://wiki.vg/Data_types">https://wiki.vg/Data_types</a>.</p>
 *
 * @author <a href="https://github.com/winnpixie/">winnpixie</a>
 */
public class MCEncoder {
    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    private final DataOutputStream writer = new DataOutputStream(byteStream);

    public byte[] getData() {
        return byteStream.toByteArray();
    }

    public DataOutputStream getWriter() {
        return writer;
    }

    public void finish() throws IOException {
        writer.flush();
        writer.close();
    }

    public MCEncoder writeVarInt(int value) throws IOException {
        while (true) {
            if ((value & ~SEGMENT_BITS) == 0) {
                writer.writeByte(value);
                break;
            }

            writer.writeByte((value & SEGMENT_BITS) | CONTINUE_BIT);

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }

        return this;
    }

    public MCEncoder writeVarLong(long value) throws IOException {
        while (true) {
            if ((value & ~((long) SEGMENT_BITS)) == 0) {
                writer.writeByte((int) value);
                break;
            }

            writer.writeByte((int) ((value & SEGMENT_BITS) | CONTINUE_BIT));

            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
        }

        return this;
    }

    public MCEncoder writeUUID(UUID uuid) throws IOException {
        writer.writeLong(uuid.getMostSignificantBits());
        writer.writeLong(uuid.getLeastSignificantBits());

        return this;
    }

    public <T extends Enum<T>> MCEncoder writeEnum(T enumObj) throws IOException {
        writeVarInt(enumObj.ordinal());

        return this;
    }
}
