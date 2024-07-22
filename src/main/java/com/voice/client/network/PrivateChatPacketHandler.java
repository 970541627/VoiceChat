package com.voice.client.network;

import com.voice.client.service.GroupClientService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class PrivateChatPacketHandler {
    List<String> talkPlayers;

    public PrivateChatPacketHandler(List<String> talkPlayers) {
        this.talkPlayers = talkPlayers;
    }

    public PrivateChatPacketHandler(FriendlyByteBuf friendlyByteBuf) {
        int size=friendlyByteBuf.readInt();
        this.talkPlayers=new LinkedList<>();
        for (int i = 0; i <size ; i++) {
            String groupPlayer = new String(friendlyByteBuf.readByteArray(friendlyByteBuf.readInt()), Charset.defaultCharset());
            this.talkPlayers.add(groupPlayer);
        }
    }

    public static void encode(PrivateChatPacketHandler privateChatPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        int size = privateChatPacketHandler.talkPlayers.size();
        friendlyByteBuf.writeInt(size);
        for (int i = 0; i < size; i++) {
            byte[] bytes = privateChatPacketHandler.talkPlayers.get(i).getBytes();
            friendlyByteBuf.writeInt(bytes.length);
            friendlyByteBuf.writeByteArray(bytes);
        }
    }

    public static void handle(PrivateChatPacketHandler privateChatPacketHandler, CustomPayloadEvent.Context context) {
        if (context.isClientSide()){
            GroupClientService.getInstance().updateTalkList(privateChatPacketHandler.talkPlayers);
        }
    }
}
