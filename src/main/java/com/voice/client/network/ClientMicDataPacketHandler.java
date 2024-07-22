package com.voice.client.network;

import com.sun.jna.Memory;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.voice.client.service.GroupClientService;
import com.voice.client.service.MicClientService;
import com.voice.memory.PointerFree;
import com.voice.basslib.BASSNative;
import com.voice.basslib.StreamAudio;
import com.voice.util.CompressUtils;
import com.voice.util.DenoiserUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.nio.charset.Charset;

public class ClientMicDataPacketHandler {
    private StreamAudio sendPacket;
    private String playerId;
    private int uncompressSize;
    private MicClientService micClientService = MicClientService.getInstance();

    public ClientMicDataPacketHandler(StreamAudio sendPacket, String playerId, int uncompressSize) {
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

    public ClientMicDataPacketHandler(FriendlyByteBuf friendlyByteBuf) {
        byte[] bytes = new byte[0];
        try {
            bytes = CompressUtils.uncompress(friendlyByteBuf.readByteArray());
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

    public static void encode(ClientMicDataPacketHandler clientMicDataPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        Pointer buffer = clientMicDataPacketHandler.sendPacket.x;
        int length = clientMicDataPacketHandler.sendPacket.y;
        long id = clientMicDataPacketHandler.sendPacket.id.longValue();

        int uncompressSize = clientMicDataPacketHandler.uncompressSize;
        byte[] bytes = new byte[uncompressSize];
        buffer.read(0, bytes, 0, uncompressSize);
        friendlyByteBuf.writeByteArray(bytes);
        friendlyByteBuf.writeInt(length);
        friendlyByteBuf.writeLong(id);
        friendlyByteBuf.writeByteArray(clientMicDataPacketHandler.playerId.getBytes());
    }

    public static void handle(ClientMicDataPacketHandler clientMicDataPacketHandler, CustomPayloadEvent.Context context) {
        if (clientMicDataPacketHandler.sendPacket != null) {
            if (context.isClientSide()) {
                GroupClientService.getInstance().setWhoIsTalking(clientMicDataPacketHandler.playerId);
                Pointer buffer = clientMicDataPacketHandler.sendPacket.x;
                int y = clientMicDataPacketHandler.sendPacket.y;
                byte[] denoise = new byte[y];
                buffer.read(0, denoise, 0, y);
                short[] shorts = DenoiserUtils.bytesToShort(denoise);
                short[] denoiseBuffer = DenoiserUtils.getSingleDenoiser().denoise(shorts);
                byte[] denoiseBytes = DenoiserUtils.shortToBytes(denoiseBuffer);
                buffer.write(0, denoiseBytes, 0, denoiseBytes.length);
                BASSNative.INSTANCE.put_stream_data(clientMicDataPacketHandler.sendPacket.x, clientMicDataPacketHandler.sendPacket.y, clientMicDataPacketHandler.sendPacket.id);
                PointerFree.freeClientPointer();
            }
        }
    }
}
