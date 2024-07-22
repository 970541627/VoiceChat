package com.voice.client.network;

import com.voice.client.service.GroupClientService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

public class GroupPacketHandler {
    List<String> groupPlayers;

    public GroupPacketHandler(List<String> groupPlayers) {
        this.groupPlayers = groupPlayers;
    }

    public GroupPacketHandler(FriendlyByteBuf friendlyByteBuf) {
        int size = friendlyByteBuf.readInt();
        this.groupPlayers = new LinkedList<>();
        for (int i = 0; i < size; i++) {
            String groupPlayer = new String(friendlyByteBuf.readByteArray(friendlyByteBuf.readInt()), Charset.defaultCharset());
            this.groupPlayers.add(groupPlayer);
        }

    }

    public static void encode(GroupPacketHandler groupPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        int size = groupPacketHandler.groupPlayers.size();
        friendlyByteBuf.writeInt(size);
        for (int i = 0; i < size; i++) {
            byte[] bytes = groupPacketHandler.groupPlayers.get(i).getBytes();
            friendlyByteBuf.writeInt(bytes.length);
            friendlyByteBuf.writeByteArray(bytes);
        }
    }

    public static void handle(GroupPacketHandler groupPacketHandler, CustomPayloadEvent.Context context) {
        if (context.isClientSide()) {
            GroupClientService.getInstance().updateGroupList(groupPacketHandler.groupPlayers);
        }
    }
}
