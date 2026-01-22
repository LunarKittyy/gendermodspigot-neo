package dbrighthd.wildfiregendermodplugin.networking;

import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftInputStream;
import dbrighthd.wildfiregendermodplugin.networking.wildfire.ModSyncPacketV5;
import dbrighthd.wildfiregendermodplugin.wildfire.ModUser;
import dbrighthd.wildfiregendermodplugin.wildfire.setup.GenderIdentities;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolTest {
        @Test
        public void testV5Parsing() throws IOException {
                // Sample V5 packet captured from research:
                // UUID: 00000000-0000-0000-0000-000000000000
                // Gender: FEMALE
                // Bust: 0.5f
                // HurtSounds: true
                // VoicePitch: 1.0f
                // Physics: (true, true, 1.0f, 1.0f)
                // Breasts: (0.0f, 0.0f, 0.0f, false, 0.0f)
                // UV Layouts: Empty layers (0 items in map)

                byte[] data = new byte[] {
                                // UUID (2 longs = 16 bytes)
                                0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                // Gender (FEMALE = index 0)
                                0x00,
                                // BustSize (0.5f)
                                0x3F, 0x00, 0x00, 0x00,
                                // HurtSounds (true)
                                0x01,
                                // VoicePitch (1.0f)
                                0x3F, (byte) 0x80, 0x00, 0x00,
                                // Physics (true, true, 1.0f, 1.0f)
                                0x01, 0x01,
                                0x3F, (byte) 0x80, 0x00, 0x00,
                                0x3F, (byte) 0x80, 0x00, 0x00,
                                // Breasts (0.0f, 0.0f, 0.0f, false, 0.0f)
                                0x00, 0x00, 0x00, 0x00,
                                0x00, 0x00, 0x00, 0x00,
                                0x00, 0x00, 0x00, 0x00,
                                0x00,
                                0x00, 0x00, 0x00, 0x00,
                                // UV Layouts (Skin: Left count=0, Right count=0; Overlay: Left count=0, Right
                                // count=0)
                                0x00, 0x00, 0x00, 0x00
                };

                ModSyncPacketV5 packet = new ModSyncPacketV5();
                try (CraftInputStream input = CraftInputStream.ofBytes(data)) {
                        ModUser user = packet.read(input);
                        assertNotNull(user);
                        assertEquals(new UUID(0, 0), user.userId());
                        assertEquals(GenderIdentities.FEMALE, user.configuration().generalOptions().genderIdentity());
                        assertEquals(0.5f, user.configuration().breastOptions().bustSize());
                        assertTrue(user.configuration().generalOptions().hurtSounds());
                        assertEquals(1.0f, user.configuration().generalOptions().voicePitch());
                        assertTrue(user.configuration().physicsOptions().breastPhysics());
                        assertTrue(user.configuration().generalOptions().showInArmor());
                        assertEquals(1.0f, user.configuration().physicsOptions().buoyancy());
                        assertEquals(1.0f, user.configuration().physicsOptions().floppiness());
                        assertNotNull(user.configuration().uvLayouts());
                }
        }

        @Test
        public void testV5RoundTrip() throws IOException {
                java.util.Map<dbrighthd.wildfiregendermodplugin.wildfire.setup.UVDirection, dbrighthd.wildfiregendermodplugin.wildfire.setup.UVQuad> quads = new java.util.EnumMap<>(
                                dbrighthd.wildfiregendermodplugin.wildfire.setup.UVDirection.class);
                quads.put(dbrighthd.wildfiregendermodplugin.wildfire.setup.UVDirection.NORTH,
                                new dbrighthd.wildfiregendermodplugin.wildfire.setup.UVQuad(1, 2, 3, 4));

                dbrighthd.wildfiregendermodplugin.wildfire.setup.UVLayout layout = new dbrighthd.wildfiregendermodplugin.wildfire.setup.UVLayout(
                                quads);
                dbrighthd.wildfiregendermodplugin.wildfire.setup.UVLayouts.Layer layer = new dbrighthd.wildfiregendermodplugin.wildfire.setup.UVLayouts.Layer(
                                layout, layout);
                dbrighthd.wildfiregendermodplugin.wildfire.setup.UVLayouts uvLayouts = new dbrighthd.wildfiregendermodplugin.wildfire.setup.UVLayouts(
                                layer, layer);

                dbrighthd.wildfiregendermodplugin.wildfire.setup.ModConfiguration config = new dbrighthd.wildfiregendermodplugin.wildfire.setup.ModConfiguration(
                                new dbrighthd.wildfiregendermodplugin.wildfire.setup.GeneralOptions(
                                                GenderIdentities.MALE, true, 1.2f,
                                                true),
                                new dbrighthd.wildfiregendermodplugin.wildfire.setup.PhysicsOptions(true, true, 0.8f,
                                                0.9f),
                                new dbrighthd.wildfiregendermodplugin.wildfire.setup.BreastOptions(0.7f, 0.1f, 0.2f,
                                                0.3f, true, 0.4f),
                                uvLayouts);

                UUID userId = UUID.randomUUID();
                ModUser user = new ModUser(userId, config);
                ModSyncPacketV5 packet = new ModSyncPacketV5();

                byte[] serialized;
                try (java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                                dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream out = new dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream(
                                                bytes)) {
                        packet.write(user, out);
                        serialized = bytes.toByteArray();
                }

                ModUser deserialized;
                try (CraftInputStream in = CraftInputStream.ofBytes(serialized)) {
                        deserialized = packet.read(in);
                }

                assertEquals(userId, deserialized.userId());
                assertEquals(GenderIdentities.MALE, deserialized.configuration().generalOptions().genderIdentity());
                assertEquals(0.7f, deserialized.configuration().breastOptions().bustSize());
                assertEquals(0.8f, deserialized.configuration().physicsOptions().buoyancy());

                assertNotNull(deserialized.configuration().uvLayouts());
                dbrighthd.wildfiregendermodplugin.wildfire.setup.UVQuad quad = deserialized.configuration().uvLayouts()
                                .skin()
                                .left().getQuads()
                                .get(dbrighthd.wildfiregendermodplugin.wildfire.setup.UVDirection.NORTH);
                assertNotNull(quad);
                assertEquals(1, quad.x1());
                assertEquals(2, quad.y1());
                assertEquals(3, quad.x2());
                assertEquals(4, quad.y2());
        }
}
