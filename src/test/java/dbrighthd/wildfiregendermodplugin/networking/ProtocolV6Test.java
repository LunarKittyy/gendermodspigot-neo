package dbrighthd.wildfiregendermodplugin.networking;

import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream;
import dbrighthd.wildfiregendermodplugin.networking.wildfire.ModSyncPacketV6;
import dbrighthd.wildfiregendermodplugin.wildfire.ModUser;
import dbrighthd.wildfiregendermodplugin.wildfire.setup.*;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Protocol 6 (mod 5.0.0-Beta.5+). The expected bytes are built by hand from the
 * mod's {@code AvatarConfig.COMPACT_STREAM_CODEC} rather than with our own writer,
 * so reader and writer can't agree with each other on a wrong layout.
 */
public class ProtocolV6Test {
    private final ModSyncPacketV6 packet = new ModSyncPacketV6();

    /** Female, physics on, a few custom UV quads. */
    private static byte[] femaleWithPhysics() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeBoolean(true); // has data (not male)
        out.writeByte(0); // gender: FEMALE
        // Breasts: x, y, z offsets, size, cleavage
        out.writeFloat(0.1f);
        out.writeFloat(-0.2f);
        out.writeFloat(-0.3f);
        out.writeFloat(0.7f);
        out.writeFloat(0.05f);
        // Physics: enabled, uniboob, bounce multiplier, floppiness
        out.writeBoolean(true);
        out.writeBoolean(false);
        out.writeFloat(0.4f);
        out.writeFloat(0.5f);
        // UVs: skin left, skin right, overlay left, overlay right
        out.writeByte(1); // one quad
        out.writeByte(0); // EAST
        out.write(new byte[] { 1, 2, 3, 4 });
        out.writeByte(0);
        out.writeByte(0);
        out.writeByte(2);
        out.writeByte(3); // UP
        out.write(new byte[] { 5, 6, 7, 8 });
        out.writeByte(4); // NORTH
        out.write(new byte[] { 9, 10, 11, 12 });
        // Sounds: hurt, voice pitch
        out.writeBoolean(false);
        out.writeFloat(1.1f);
        // Show in armor
        out.writeBoolean(false);
        return bytes.toByteArray();
    }

    /** "Other", physics off: the physics fields after the flag are omitted. */
    private static byte[] otherWithoutPhysics() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeBoolean(true);
        out.writeByte(2); // gender: OTHER
        out.writeFloat(0f);
        out.writeFloat(0f);
        out.writeFloat(0f);
        out.writeFloat(0.3f);
        out.writeFloat(0f);
        out.writeBoolean(false); // physics disabled
        out.write(new byte[] { 0, 0, 0, 0 }); // four empty UV layouts
        out.writeBoolean(true);
        out.writeFloat(0.9f);
        out.writeBoolean(true);
        return bytes.toByteArray();
    }

    private static byte[] withUuid(UUID uuid, byte[] config) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bytes);
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
        out.write(config);
        return bytes.toByteArray();
    }

    private ModUser readServerbound(UUID sender, byte[] data) throws IOException {
        try (CraftInputStream in = CraftInputStream.ofBytes(data)) {
            ModUser user = packet.readFromClient(sender, in);
            assertEquals(0, in.available(), "payload must be consumed exactly");
            return user;
        }
    }

    private byte[] writeClientbound(ModUser user) throws IOException {
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                CraftOutputStream out = new CraftOutputStream(bytes)) {
            packet.write(user, out);
            return bytes.toByteArray();
        }
    }

    @Test
    public void testParsesServerboundPayload() throws IOException {
        UUID sender = UUID.randomUUID();
        ModUser user = readServerbound(sender, femaleWithPhysics());
        ModConfiguration config = user.configuration();

        assertEquals(sender, user.userId());
        assertEquals(GenderIdentities.FEMALE, config.generalOptions().genderIdentity());
        assertEquals(0.1f, config.breastOptions().xOffset());
        assertEquals(-0.2f, config.breastOptions().yOffset());
        assertEquals(-0.3f, config.breastOptions().zOffset());
        assertEquals(0.7f, config.breastOptions().bustSize());
        assertEquals(0.05f, config.breastOptions().cleavage());
        assertTrue(config.physicsOptions().breastPhysics());
        assertFalse(config.breastOptions().uniBoob());
        assertEquals(0.4f, config.physicsOptions().buoyancy());
        assertEquals(0.5f, config.physicsOptions().floppiness());
        assertFalse(config.generalOptions().hurtSounds());
        assertEquals(1.1f, config.generalOptions().voicePitch());
        assertFalse(config.generalOptions().showInArmor());

        UVLayouts uvs = config.uvLayouts();
        assertEquals(new UVQuad(1, 2, 3, 4), uvs.skin().left().getQuads().get(UVDirection.EAST));
        assertEquals(1, uvs.skin().left().getQuads().size());
        assertTrue(uvs.skin().right().getQuads().isEmpty());
        assertTrue(uvs.overlay().left().getQuads().isEmpty());
        assertEquals(new UVQuad(5, 6, 7, 8), uvs.overlay().right().getQuads().get(UVDirection.UP));
        assertEquals(new UVQuad(9, 10, 11, 12), uvs.overlay().right().getQuads().get(UVDirection.NORTH));
    }

    @Test
    public void testRelaysExactlyWhatTheClientSent() throws IOException {
        UUID sender = UUID.randomUUID();
        for (byte[] config : new byte[][] { femaleWithPhysics(), otherWithoutPhysics() }) {
            ModUser user = readServerbound(sender, config);
            assertArrayEquals(withUuid(sender, config), writeClientbound(user));
        }
    }

    @Test
    public void testPhysicsDisabledUsesDefaults() throws IOException {
        ModConfiguration config = readServerbound(UUID.randomUUID(), otherWithoutPhysics()).configuration();

        assertEquals(GenderIdentities.OTHER, config.generalOptions().genderIdentity());
        assertFalse(config.physicsOptions().breastPhysics());
        assertTrue(config.breastOptions().uniBoob());
        assertEquals(0.333f, config.physicsOptions().buoyancy());
        assertEquals(0.75f, config.physicsOptions().floppiness());
        assertEquals(0.9f, config.generalOptions().voicePitch());
        assertTrue(config.generalOptions().showInArmor());
    }

    @Test
    public void testMaleIsASingleFalseByte() throws IOException {
        UUID sender = UUID.randomUUID();
        ModUser user = readServerbound(sender, new byte[] { 0 });

        assertEquals(GenderIdentities.MALE, user.configuration().generalOptions().genderIdentity());
        assertArrayEquals(withUuid(sender, new byte[] { 0 }), writeClientbound(user));
    }

    @Test
    public void testClientboundRoundTrip() throws IOException {
        UUID uuid = UUID.randomUUID();
        byte[] clientbound = withUuid(uuid, femaleWithPhysics());

        ModUser user;
        try (CraftInputStream in = CraftInputStream.ofBytes(clientbound)) {
            user = packet.read(in);
        }
        assertEquals(uuid, user.userId());
        assertArrayEquals(clientbound, writeClientbound(user));
    }

    @Test
    public void testTruncatedPayloadIsRejected() throws IOException {
        byte[] full = femaleWithPhysics();
        byte[] truncated = Arrays.copyOf(full, full.length - 3);
        assertThrows(EOFException.class, () -> readServerbound(UUID.randomUUID(), truncated));
    }
}
