/*
    Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
    Copyright (C) 2023 WildfireRomeo

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU Lesser General Public
    License as published by the Free Software Foundation; either
    version 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    Lesser General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/

package dbrighthd.wildfiregendermodplugin.gender;

import dbrighthd.wildfiregendermodplugin.utilities.MCDecoder;
import dbrighthd.wildfiregendermodplugin.utilities.MCEncoder;

import java.io.IOException;
import java.util.UUID;

public class GenderData {
    public boolean shouldSync = false; // FIXME: Currently causes issue where new players receive incorrect data.

    public UUID uuid;
    public Gender gender;

    public float bustSize;
    public boolean hurtSounds;

    // Physics variables
    public boolean breastPhysics;
    public boolean showInArmor;
    public float bounceMultiplier;
    public float floppyMultiplier;

    public float xOffset;
    public float yOffset;
    public float zOffset;
    public boolean uniboob;
    public float cleavage;

    public void encode(MCEncoder encoder) {
        encoder.writeUUID(this.uuid);
        encoder.writeEnum(this.gender);

        try {
            encoder.getWriter().writeFloat(this.bustSize);
            encoder.getWriter().writeBoolean(this.hurtSounds);
            encoder.getWriter().writeBoolean(this.breastPhysics);
            encoder.getWriter().writeBoolean(this.showInArmor);
            encoder.getWriter().writeFloat(this.bounceMultiplier);
            encoder.getWriter().writeFloat(this.floppyMultiplier);

            encoder.getWriter().writeFloat(this.xOffset);
            encoder.getWriter().writeFloat(this.yOffset);
            encoder.getWriter().writeFloat(this.zOffset);
            encoder.getWriter().writeBoolean(this.uniboob);
            encoder.getWriter().writeFloat(this.cleavage);

            encoder.finish();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static GenderData decode(MCDecoder decoder) {
        GenderData data = new GenderData();

        try {
            data.uuid = decoder.readUUID();
            data.gender = decoder.readEnum(Gender.class);

            data.bustSize = decoder.getReader().readFloat();
            data.hurtSounds = decoder.getReader().readBoolean();
            data.breastPhysics = decoder.getReader().readBoolean();
            data.showInArmor = decoder.getReader().readBoolean();
            data.bounceMultiplier = decoder.getReader().readFloat();
            data.floppyMultiplier = decoder.getReader().readFloat();

            data.xOffset = decoder.getReader().readFloat();
            data.yOffset = decoder.getReader().readFloat();
            data.zOffset = decoder.getReader().readFloat();
            data.uniboob = decoder.getReader().readBoolean();
            data.cleavage = decoder.getReader().readFloat();

            decoder.finish();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        return data;
    }
}
