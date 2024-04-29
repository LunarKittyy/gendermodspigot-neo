

package dbrighthd.wildfiregendermodplugin;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import org.bukkit.Bukkit;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public final class GenderPlugin extends JavaPlugin implements PluginMessageListener {

    public static final String MODID = "wildfire_gender";
    String genderInfo = MODID + ":send_gender_info";
    String genderSync = MODID + ":sync";
    String forgeChannel = MODID + ":main_channel";
    Map<UUID, GenderData.Gender> playerGenderList = new HashMap<>();

    @Override
    public void onEnable() {
        ConsoleCommandSender console = getServer().getConsoleSender();
        console.sendMessage("1.20.5 update with contributions by @stigstille");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, genderInfo, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, genderSync);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, forgeChannel, this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, forgeChannel);

    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this);
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        System.out.println("i got a message from " + player.getName());
        if (channel.equals(genderInfo) || channel.equals(forgeChannel)) {
            System.out.println("channel verified for " + player.getName());
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.copiedBuffer(message));
            if(channel.equals(forgeChannel))
            {
                System.out.println("confirmed forge for " + player.getName());
                buffer.readByte();
            }
            UUID uuid = buffer.readUUID();
            GenderData.Gender gender = buffer.readEnum(GenderData.Gender.class);
            System.out.println("confirmed gender for  " + player.getName() + gender);
            storePlayer(buffer, uuid, gender);
        }
        for(Map.Entry<UUID, GenderData.Gender> entry : playerGenderList.entrySet())
        {
            FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
            FriendlyByteBuf bufferForge = new FriendlyByteBuf(Unpooled.buffer());
            bufferForge.writeByte(1);
            encode(buffer,entry.getValue());
            encode(bufferForge,entry.getValue());
            System.out.println("confirmed gender for  " + entry.getValue());
            for(Player playerToReceive : Bukkit.getOnlinePlayers())
            {
                playerToReceive.sendPluginMessage(this, genderSync,buffer.array());
                playerToReceive.sendPluginMessage(this, forgeChannel,bufferForge.array());
            }

        }
    }
    public void encode(FriendlyByteBuf buffer, GenderData.Gender gender)
    {
        buffer.writeUUID(gender.uuid);
        buffer.writeEnum(gender);
        buffer.writeFloat(gender.bust_size);
        buffer.writeBoolean(gender.hurtSounds);
        buffer.writeBoolean(gender.breast_physics);
        buffer.writeBoolean(gender.show_in_armor);
        buffer.writeFloat(gender.bounceMultiplier);
        buffer.writeFloat(gender.floppyMultiplier);

        buffer.writeFloat(gender.xOffset);
        buffer.writeFloat(gender.yOffset);
        buffer.writeFloat(gender.zOffset);
        buffer.writeBoolean(gender.uniboob);
        buffer.writeFloat(gender.cleavage);
    }
    public void storePlayer(FriendlyByteBuf buffer, UUID uuid, GenderData.Gender gender)
    {
        gender.bust_size = buffer.readFloat();
        gender.hurtSounds = buffer.readBoolean();
        gender.uuid = uuid;
        gender.breast_physics = buffer.readBoolean();
        gender.show_in_armor = buffer.readBoolean();
        gender.bounceMultiplier = buffer.readFloat();
        gender.floppyMultiplier = buffer.readFloat();

        gender.xOffset = buffer.readFloat();
        gender.yOffset = buffer.readFloat();
        gender.zOffset = buffer.readFloat();
        gender.uniboob = buffer.readBoolean();
        gender.cleavage = buffer.readFloat();
        playerGenderList.put(uuid,gender);
    }
}
