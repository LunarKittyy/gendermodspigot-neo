package dbrighthd.wildfiregendermodplugin.networking.wildfire;

import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream;
import dbrighthd.wildfiregendermodplugin.wildfire.ModUser;

import java.io.IOException;

/**
 * @author winnpixie
 */
public class ModSyncPacketV1 implements ModSyncPacket { // TODO: Find and implement the first packet format for FGM
    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public String getModRange() {
        return "0.0.1 - 2.8.0";
    }

    @Override
    public ModUser read(CraftInputStream input) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }

    @Override
    public void write(ModUser user, CraftOutputStream output) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }
}
