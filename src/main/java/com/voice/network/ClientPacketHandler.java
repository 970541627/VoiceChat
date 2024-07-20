package com.voice.network;

import com.voice.players.ServerPlayerManager;
import com.voice.recording.SoundDataCenter;
import com.voice.snative.BASSNative;
import com.voice.snative.StreamAudio;
import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.nio.charset.Charset;

public class ClientPacketHandler {
    private StreamAudio sendPacket;
    private String playerId;
    private int uncompressSize;

    public ClientPacketHandler(StreamAudio sendPacket, String playerId, int uncompressSize) {
        this.sendPacket = sendPacket;
        this.playerId = playerId;
        this.uncompressSize = uncompressSize;
    }

    public StreamAudio getSendPacket() {
        return sendPacket;
    }

    public String getPlayerId() {
        return playerId;
    }

    public ClientPacketHandler(FriendlyByteBuf friendlyByteBuf) {
        byte[] bytes = new byte[0];
        try {
            bytes = SoundDataCenter.uncompress(friendlyByteBuf.readByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        int size = friendlyByteBuf.readInt();
        long id = friendlyByteBuf.readLong();
        String playerId = new String(friendlyByteBuf.readByteArray(), Charset.defaultCharset());
        Pointer pointer = new Memory(size);
        pointer.write(0, bytes, 0, size);
        NativeLong nativeLong = new NativeLong();
        nativeLong.setValue(id);
        this.sendPacket = new StreamAudio(pointer, size, nativeLong);
        this.playerId = playerId;
        PointerFree.addClientPointer(pointer);
    }

    public static void encode(ClientPacketHandler clientPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        Pointer buffer = clientPacketHandler.sendPacket.x;
        int length = clientPacketHandler.sendPacket.y;
        long id = clientPacketHandler.sendPacket.id.longValue();

        int uncompressSize = clientPacketHandler.uncompressSize;
        byte[] bytes = new byte[uncompressSize];
        buffer.read(0, bytes, 0, uncompressSize);
        friendlyByteBuf.writeByteArray(bytes);
        friendlyByteBuf.writeInt(length);
        friendlyByteBuf.writeLong(id);
        friendlyByteBuf.writeByteArray(clientPacketHandler.playerId.getBytes());
    }

    public static void handle(ClientPacketHandler clientPacketHandler, CustomPayloadEvent.Context context) {
        if (clientPacketHandler.sendPacket != null) {
            if (context.isClientSide()) {
                ServerPlayerManager.setWhoIsTalking(clientPacketHandler.playerId);
                Pointer buffer = clientPacketHandler.sendPacket.x;
                int y = clientPacketHandler.sendPacket.y;
                byte[] denoise = new byte[y];
                buffer.read(0, denoise, 0, y);
                short[] shorts = SoundDataCenter.bytesToShort(denoise);
                short[] denoiseBuffer = SoundDataCenter.getSingleDenoiser().denoise(shorts);
                byte[] denoiseBytes = SoundDataCenter.shortToBytes(denoiseBuffer);
                buffer.write(0, denoiseBytes, 0, denoiseBytes.length);
                BASSNative.INSTANCE.put_stream_data(clientPacketHandler.sendPacket.x, clientPacketHandler.sendPacket.y, clientPacketHandler.sendPacket.id);
                PointerFree.freeClientPointer();
            }
        }
    }
}
