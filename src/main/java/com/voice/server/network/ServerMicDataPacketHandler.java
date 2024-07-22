package com.voice.server.network;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.voice.client.network.ClientMicDataPacketHandler;
import com.voice.server.service.MicServerService;
import com.voice.basslib.StreamAudio;
import com.voice.util.CompressUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.io.IOException;
import java.nio.charset.Charset;

public class ServerMicDataPacketHandler {
    private StreamAudio sendPacket;
    private String playerId;
    private int uncompressSize;

    public ServerMicDataPacketHandler(StreamAudio sendPacket, String playerId) {
        this.sendPacket = sendPacket;
        this.playerId = playerId;
    }


    public ServerMicDataPacketHandler(FriendlyByteBuf friendlyByteBuf) {
        byte[] bytes;
        bytes = friendlyByteBuf.readByteArray();
        int size = friendlyByteBuf.readInt();
        int uncompressSize = friendlyByteBuf.readInt();
        long id = friendlyByteBuf.readLong();
        String playerId = new String(friendlyByteBuf.readByteArray(), Charset.defaultCharset());
        Pointer pointer = new Memory(uncompressSize);
        pointer.write(0, bytes, 0, uncompressSize);
        NativeLong nativeLong = new NativeLong();
        nativeLong.setValue(id);
        this.sendPacket = new StreamAudio(pointer, size, nativeLong);
        this.playerId = playerId;
        this.uncompressSize = uncompressSize;
    }

    public static void encode(ServerMicDataPacketHandler serverMicDataPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        Pointer buffer = serverMicDataPacketHandler.sendPacket.x;
        int length = serverMicDataPacketHandler.sendPacket.y;
        long id = serverMicDataPacketHandler.sendPacket.id.longValue();
        byte[] bytes = new byte[length];
        buffer.read(0, bytes, 0, length);
        try {
            bytes = CompressUtils.compress(bytes);
            friendlyByteBuf.writeByteArray(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        friendlyByteBuf.writeInt(length);
        friendlyByteBuf.writeInt(bytes.length);
        friendlyByteBuf.writeLong(id);
        friendlyByteBuf.writeByteArray(serverMicDataPacketHandler.playerId.getBytes());
    }

    public static void handle(ServerMicDataPacketHandler serverMicDataPacketHandler,
                              CustomPayloadEvent.Context context) {
        if (serverMicDataPacketHandler.sendPacket != null) {
            if (context.isServerSide()) {
                MicServerService serverService=MicServerService.getInstance();
                serverService.addClientPacket(new ClientMicDataPacketHandler(serverMicDataPacketHandler.sendPacket, serverMicDataPacketHandler.playerId, serverMicDataPacketHandler.uncompressSize));
            }
        }
    }
}
