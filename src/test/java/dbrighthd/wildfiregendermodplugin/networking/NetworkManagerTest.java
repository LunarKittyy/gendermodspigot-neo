package dbrighthd.wildfiregendermodplugin.networking;

import dbrighthd.wildfiregendermodplugin.GenderModPlugin;
import dbrighthd.wildfiregendermodplugin.logging.CustomPluginLogger;
import dbrighthd.wildfiregendermodplugin.networking.minecraft.CraftOutputStream;
import dbrighthd.wildfiregendermodplugin.networking.wildfire.ModSyncPacketV5;
import dbrighthd.wildfiregendermodplugin.networking.wildfire.ModSyncPacketV6;
import dbrighthd.wildfiregendermodplugin.wildfire.ModConstants;
import dbrighthd.wildfiregendermodplugin.wildfire.ModUser;
import dbrighthd.wildfiregendermodplugin.wildfire.UserManager;
import dbrighthd.wildfiregendermodplugin.wildfire.setup.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Instance-level NetworkManager coverage that needs a Player/GenderModPlugin.
 * Uses Mockito rather than a full Bukkit server mock (MockBukkit) since only
 * a handful of methods on each are actually touched by NetworkManager.
 */
public class NetworkManagerTest {
    private GenderModPlugin plugin;
    private UserManager userManager;

    private NetworkManager create(int protocol) {
        plugin = mock(GenderModPlugin.class);
        userManager = new UserManager();
        FileConfiguration config = mock(FileConfiguration.class);

        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getCustomLogger()).thenReturn(mock(CustomPluginLogger.class));
        when(plugin.getUserManager()).thenReturn(userManager);
        when(config.getInt("mod.protocol", -1)).thenReturn(protocol);

        NetworkManager networkManager = new NetworkManager(plugin);
        assertTrue(networkManager.init(), "init() should succeed for a valid configured protocol");
        return networkManager;
    }

    private static Player player(UUID uuid) {
        Player player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("player-" + uuid);
        return player;
    }

    private static ModConfiguration female() {
        return new ModConfiguration(
                new GeneralOptions.Builder().setGenderIdentity(GenderIdentities.FEMALE).create(),
                new PhysicsOptions.Builder().create(),
                new BreastOptions.Builder().create(),
                UVLayouts.defaultLayouts());
    }

    @Test
    public void testInitRejectsUnknownProtocol() {
        plugin = mock(GenderModPlugin.class);
        FileConfiguration config = mock(FileConfiguration.class);
        when(plugin.getConfig()).thenReturn(config);
        when(plugin.getCustomLogger()).thenReturn(mock(CustomPluginLogger.class));
        when(config.getInt("mod.protocol", -1)).thenReturn(7);

        assertFalse(new NetworkManager(plugin).init());
    }

    @Test
    public void testV6UsesNewChannels() {
        NetworkManager networkManager = create(6);
        assertArrayEquals(new String[] { ModConstants.V6_SYNC_SERVERBOUND }, networkManager.getIncomingSyncChannels());
        assertArrayEquals(new String[] { ModConstants.V6_SYNC_CLIENTBOUND }, networkManager.getOutgoingSyncChannels());
    }

    @Test
    public void testV5UsesLegacyChannels() {
        NetworkManager networkManager = create(5);
        assertArrayEquals(new String[] { ModConstants.SEND_GENDER_INFO, ModConstants.FORGE },
                networkManager.getIncomingSyncChannels());
        assertArrayEquals(new String[] { ModConstants.SYNC, ModConstants.FORGE },
                networkManager.getOutgoingSyncChannels());
    }

    @Test
    public void testV6PayloadBelongsToSender() throws IOException {
        NetworkManager networkManager = create(6);
        UUID senderId = UUID.randomUUID();

        byte[] data;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                CraftOutputStream out = new CraftOutputStream(bytes)) {
            ModSyncPacketV6.writeConfiguration(female(), out);
            data = bytes.toByteArray();
        }

        ModUser user = networkManager.deserializeUser(data, false, player(senderId));
        assertNotNull(user);
        assertEquals(senderId, user.userId());
        assertEquals(GenderIdentities.FEMALE, user.configuration().generalOptions().genderIdentity());
    }

    @Test
    public void testTrailingDataIsRejected() throws IOException {
        NetworkManager networkManager = create(6);

        byte[] data;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                CraftOutputStream out = new CraftOutputStream(bytes)) {
            ModSyncPacketV6.writeConfiguration(female(), out);
            out.writeByte(0x42);
            data = bytes.toByteArray();
        }

        assertNull(networkManager.deserializeUser(data, false, player(UUID.randomUUID())));
    }

    @Test
    public void testV5PayloadKeepsEmbeddedUuid() throws IOException {
        NetworkManager networkManager = create(5);
        UUID embedded = UUID.randomUUID();

        byte[] data;
        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                CraftOutputStream out = new CraftOutputStream(bytes)) {
            new ModSyncPacketV5().write(new ModUser(embedded, female()), out);
            data = bytes.toByteArray();
        }

        // The listener compares the embedded UUID to the sender, so it must not be replaced.
        ModUser user = networkManager.deserializeUser(data, false, player(UUID.randomUUID()));
        assertNotNull(user);
        assertEquals(embedded, user.userId());
    }

    @Test
    public void testV6SyncSkipsSelfAndUnreadyPlayers() {
        NetworkManager networkManager = create(6);
        UUID aliceId = UUID.randomUUID();
        UUID bobId = UUID.randomUUID();
        UUID carolId = UUID.randomUUID();
        Player alice = player(aliceId);
        Player bob = player(bobId);
        Player carol = player(carolId);

        userManager.getUsers().put(aliceId, new ModUser(aliceId, female()));
        userManager.setProtocolReady(aliceId);
        userManager.setProtocolReady(bobId);
        // carol never finished the handshake

        networkManager.sync(List.of(alice, bob, carol));

        verify(alice, never()).sendPluginMessage(any(), anyString(), any());
        verify(bob).sendPluginMessage(eq(plugin), eq(ModConstants.V6_SYNC_CLIENTBOUND), any());
        verify(carol, never()).sendPluginMessage(any(), anyString(), any());
    }
}
