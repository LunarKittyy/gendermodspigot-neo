package dbrighthd.wildfiregendermodplugin.utilities;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * <p>Methods to decode data sent by the client.</p>
 * <p>Methods shamelessly stolen from <a href="https://wiki.vg/Data_types">https://wiki.vg/Data_types</a>.</p>
 *
 * @author <a href="https://github.com/winnpixie/">winnpixie</a>
 */
public class MCDecoder {
    private static final int SEGMENT_BITS = 0x7F;
    private static final int CONTINUE_BIT = 0x80;

    private final byte[] data;
    private final DataInputStream reader;

    public MCDecoder(byte[] data) {
        this.data = data;
        this.reader = new DataInputStream(new ByteArrayInputStream(data));
    }

    public byte[] getData() {
        return data;
    }

    public DataInputStream getReader() {
        return reader;
    }

    public void finish() throws IOException {
        reader.close();
    }

    public int readVarInt() throws IOException {
        int value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = reader.readByte();

            value |= (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;
            if (position >= 32) throw new IOException("VarInt is too big");
        }

        return value;
    }

    public long readVarLong() throws IOException {
        long value = 0;
        int position = 0;
        byte currentByte;

        while (true) {
            currentByte = reader.readByte();
            value |= (long) (currentByte & SEGMENT_BITS) << position;

            if ((currentByte & CONTINUE_BIT) == 0) break;

            position += 7;
            if (position >= 64) throw new IOException("VarLong is too big");
        }

        return value;
    }

    public UUID readUUID() throws IOException {
        return new UUID(reader.readLong(), reader.readLong());
    }

    public <T extends Enum<T>> T readEnum(Class<T> enumCls) throws IOException {
        return enumCls.getEnumConstants()[readVarInt()];
    }
}
