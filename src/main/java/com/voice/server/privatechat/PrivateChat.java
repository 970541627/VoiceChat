package com.voice.server.privatechat;

import com.voice.client.network.NetworkSender;
import com.voice.client.network.PrivateChatPacketHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class PrivateChat {
    private static PrivateChat privateChat;
    private Map<ServerPlayer, ServerPlayer> privateChatMap = new HashMap<>();

    public static PrivateChat getInstance() {
        return privateChat == null ? (privateChat = new PrivateChat()) : privateChat;
    }

    public void cancelPrivateChat(ServerPlayer src) {
        if (privateChatMap.containsKey(src)) {
            ServerPlayer anotherPlayer = privateChatMap.get(src);
            privateChatMap.remove(src);
            if (privateChatMap.containsKey(anotherPlayer)) {
                privateChatMap.remove(anotherPlayer);
                src.sendSystemMessage(Component.literal("你已退出私聊频道").withColor(0xFFFF0000));
                anotherPlayer.sendSystemMessage(Component.literal("玩家").withColor(0xFFFF0000)
                        .append(src.getDisplayName().getString()).withColor(0xFFFFFF00)
                        .append("退出了私聊频道\n").withColor(0xFFFF0000));
                ServerPlayer[] serverPlayers = new ServerPlayer[2];
                serverPlayers[0] = src;
                serverPlayers[1] = anotherPlayer;
                NetworkSender.sendPrivateChatPacketToClient(new PrivateChatPacketHandler(new LinkedList<>()), serverPlayers);
            }
        }else{
            src.sendSystemMessage(Component.literal("你没加入私聊频道").withColor(0xFFFF0000));
        }
    }

    public void createPrivateChat(ServerPlayer src, ServerPlayer dest) {
        if (!privateChatMap.containsKey(src) && !privateChatMap.containsKey(dest)) {
            privateChatMap.put(src, dest);
            privateChatMap.put(dest, src);
        }
    }

    public ServerPlayer getPrivateChatPlayer(ServerPlayer key) {
        return privateChatMap.get(key);
    }
}
