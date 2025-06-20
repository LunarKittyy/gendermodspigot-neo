package dbrighthd.wildfiregendermodplugin.protocol.wildfire;

import dbrighthd.wildfiregendermodplugin.gender.Gender;
import dbrighthd.wildfiregendermodplugin.gender.GenderData;
import dbrighthd.wildfiregendermodplugin.protocol.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.protocol.minecraft.CraftOutputStream;

import java.io.IOException;

public class GenderSyncPacketV3 implements GenderSyncPacket {
    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public String getModRange() {
        return "3.1.0 - 4.0.0";
    }

    @Override
    public GenderData read(CraftInputStream input) throws IOException {
        GenderData data = new GenderData();

        data.uuid = input.readUUID();
        data.gender = input.readEnum(Gender.class);

        data.bustSize = input.readFloat();
        data.hurtSounds = input.readBoolean();
        data.breastPhysics = input.readBoolean();
        data.showInArmor = input.readBoolean();
        data.bounceMultiplier = input.readFloat();
        data.floppyMultiplier = input.readFloat();

        data.xOffset = input.readFloat();
        data.yOffset = input.readFloat();
        data.zOffset = input.readFloat();
        data.uniboob = input.readBoolean();
        data.cleavage = input.readFloat();

        return data;
    }

    @Override
    public void write(GenderData data, CraftOutputStream output) throws IOException {
        output.writeUUID(data.uuid);
        output.writeEnum(data.gender);

        output.writeFloat(data.bustSize);
        output.writeBoolean(data.hurtSounds);
        output.writeBoolean(data.breastPhysics);
        output.writeBoolean(data.showInArmor);
        output.writeFloat(data.bounceMultiplier);
        output.writeFloat(data.floppyMultiplier);

        output.writeFloat(data.xOffset);
        output.writeFloat(data.yOffset);
        output.writeFloat(data.zOffset);
        output.writeBoolean(data.uniboob);
        output.writeFloat(data.cleavage);
    }
}
