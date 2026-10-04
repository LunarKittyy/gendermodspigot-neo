package dbrighthd.wildfiregendermodplugin.networking.wildfire;

import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream;
import dbrighthd.wildfiregendermodplugin.wildfire.ModUser;
import dbrighthd.wildfiregendermodplugin.wildfire.setup.*;

import java.io.IOException;
import java.util.UUID;

/**
 * Sync format introduced in mod 5.0.0-Beta.5 ({@code AvatarConfig.COMPACT_STREAM_CODEC}).
 * <p>
 * Serverbound payloads carry only the configuration; clientbound payloads prefix it with
 * the UUID of the player it belongs to. The configuration itself starts with a boolean:
 * {@code false} means "male with default settings" and nothing else follows.
 */
public class ModSyncPacketV6 implements ModSyncPacket {
    @Override
    public int getVersion() {
        return 6;
    }

    @Override
    public String getModRange() {
        return "5.0.0-Beta.5 - ?.?.?";
    }

    /**
     * Reads the clientbound form (UUID followed by the configuration).
     */
    @Override
    public ModUser read(CraftInputStream input) throws IOException {
        UUID userId = input.readUUID();
        return new ModUser(userId, readConfiguration(input));
    }

    /**
     * Reads the serverbound form, which has no UUID.
     */
    @Override
    public ModUser readFromClient(UUID senderId, CraftInputStream input) throws IOException {
        return new ModUser(senderId, readConfiguration(input));
    }

    /**
     * Writes the clientbound form (UUID followed by the configuration).
     */
    @Override
    public void write(ModUser user, CraftOutputStream output) throws IOException {
        output.writeUUID(user.userId());
        writeConfiguration(user.configuration(), output);
    }

    public static ModConfiguration readConfiguration(CraftInputStream input) throws IOException {
        GeneralOptions.Builder generalBuilder = new GeneralOptions.Builder();
        PhysicsOptions.Builder physicsBuilder = new PhysicsOptions.Builder();
        BreastOptions.Builder breastBuilder = new BreastOptions.Builder();

        if (!input.readBoolean()) {
            return new ModConfiguration(
                    generalBuilder.create(),
                    physicsBuilder.create(),
                    breastBuilder.create(),
                    UVLayouts.defaultLayouts());
        }

        generalBuilder.setGenderIdentity(input.readEnum(GenderIdentities.class));

        // Breasts
        breastBuilder.setXOffset(input.readFloat());
        breastBuilder.setYOffset(input.readFloat());
        breastBuilder.setZOffset(input.readFloat());
        breastBuilder.setBustSize(input.readFloat());
        breastBuilder.setCleavage(input.readFloat());

        // Breasts.Physics, whose remaining fields are only sent when physics is enabled
        boolean physics = input.readBoolean();
        physicsBuilder.setBreastPhysics(physics);
        if (physics) {
            breastBuilder.setUniBoob(input.readBoolean());
            physicsBuilder.setBuoyancy(input.readFloat()); // bounceMultiplier
            physicsBuilder.setFloppiness(input.readFloat());
        }

        UVLayouts uvLayouts = ModSyncPacketV5.readUVLayouts(input);

        // Sounds
        generalBuilder.setHurtSounds(input.readBoolean());
        generalBuilder.setVoicePitch(input.readFloat());

        boolean showInArmor = input.readBoolean();
        generalBuilder.setShowInArmor(showInArmor);
        physicsBuilder.setArmorPhysics(showInArmor);

        return new ModConfiguration(
                generalBuilder.create(),
                physicsBuilder.create(),
                breastBuilder.create(),
                uvLayouts);
    }

    public static void writeConfiguration(ModConfiguration configuration, CraftOutputStream output) throws IOException {
        GeneralOptions general = configuration.generalOptions();
        PhysicsOptions physics = configuration.physicsOptions();
        BreastOptions breast = configuration.breastOptions();

        if (general.genderIdentity() == GenderIdentities.MALE) {
            output.writeBoolean(false);
            return;
        }
        output.writeBoolean(true);

        output.writeEnum(general.genderIdentity());

        output.writeFloat(breast.xOffset());
        output.writeFloat(breast.yOffset());
        output.writeFloat(breast.zOffset());
        output.writeFloat(breast.bustSize());
        output.writeFloat(breast.cleavage());

        output.writeBoolean(physics.breastPhysics());
        if (physics.breastPhysics()) {
            output.writeBoolean(breast.uniBoob());
            output.writeFloat(physics.buoyancy());
            output.writeFloat(physics.floppiness());
        }

        ModSyncPacketV5.writeUVLayouts(configuration.uvLayouts(), output);

        output.writeBoolean(general.hurtSounds());
        output.writeFloat(general.voicePitch());

        output.writeBoolean(general.showInArmor());
    }
}
