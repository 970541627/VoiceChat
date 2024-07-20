package com.voice.network;

import com.voice.recording.SoundDataCenter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.nio.charset.StandardCharsets;

public class PlayerLogPacketHandler {
    int status = 0; //0 login  1 logout
    String playerId;

    public PlayerLogPacketHandler(int status, String playerId) {
        this.status = status;
        this.playerId = playerId;
    }

    public PlayerLogPacketHandler(FriendlyByteBuf friendlyByteBuf) {
        this.status = friendlyByteBuf.readInt();
        this.playerId = new String(friendlyByteBuf.readByteArray(), StandardCharsets.UTF_8);
    }

    public static void encode(PlayerLogPacketHandler playerLogPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeInt(playerLogPacketHandler.status);
        friendlyByteBuf.writeByteArray(playerLogPacketHandler.playerId.getBytes());
    }

    public static void handle(PlayerLogPacketHandler playerLogPacketHandler, CustomPayloadEvent.Context context) {
        if (context.isClientSide()) {
            if (playerLogPacketHandler.status == 0) {
                SoundDataCenter.playerId = playerLogPacketHandler.playerId;
                SoundDataCenter.startSoundThread();
            }
        }
    }
}
