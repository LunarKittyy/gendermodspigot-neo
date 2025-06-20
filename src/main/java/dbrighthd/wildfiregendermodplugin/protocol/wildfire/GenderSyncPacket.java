package dbrighthd.wildfiregendermodplugin.protocol.wildfire;

import dbrighthd.wildfiregendermodplugin.gender.GenderData;
import dbrighthd.wildfiregendermodplugin.protocol.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.protocol.minecraft.CraftOutputStream;

import java.io.IOException;

public interface GenderSyncPacket {
    int getVersion();

    String getModRange();

    GenderData read(CraftInputStream input) throws IOException;

    void write(GenderData data, CraftOutputStream output) throws IOException;
}
