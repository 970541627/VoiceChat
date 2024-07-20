package com.voice.network;

import com.voice.recording.SoundDataCenter;
import com.voice.snative.StreamAudio;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.io.IOException;
import java.nio.charset.Charset;

public class ServerPacketHandler {
    private StreamAudio sendPacket;
    private String playerId;
    private int uncompressSize;

    public ServerPacketHandler(StreamAudio sendPacket, String playerId) {
        this.sendPacket = sendPacket;
        this.playerId = playerId;
    }


    public ServerPacketHandler(FriendlyByteBuf friendlyByteBuf) {
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

    public static void encode(ServerPacketHandler serverPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        Pointer buffer = serverPacketHandler.sendPacket.x;
        int length = serverPacketHandler.sendPacket.y;
        long id = serverPacketHandler.sendPacket.id.longValue();
        byte[] bytes = new byte[length];
        buffer.read(0, bytes, 0, length);
        try {
            bytes = SoundDataCenter.compress(bytes);
            friendlyByteBuf.writeByteArray(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        friendlyByteBuf.writeInt(length);
        friendlyByteBuf.writeInt(bytes.length);
        friendlyByteBuf.writeLong(id);
        friendlyByteBuf.writeByteArray(serverPacketHandler.playerId.getBytes());
    }

    public static void handle(ServerPacketHandler serverPacketHandler,
                              CustomPayloadEvent.Context context) {
        if (serverPacketHandler.sendPacket != null) {
            if (context.isServerSide()) {
                SoundDataCenter.serverPacketHandlersServerSide.add(new ClientPacketHandler(serverPacketHandler.sendPacket, serverPacketHandler.playerId, serverPacketHandler.uncompressSize));
            }
        }
    }

}
