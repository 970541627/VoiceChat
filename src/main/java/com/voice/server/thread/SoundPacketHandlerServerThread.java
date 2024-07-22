package com.voice.server.thread;

import com.voice.client.network.ClientMicDataPacketHandler;
import com.voice.client.network.NetworkSender;
import com.voice.memory.PointerFree;
import com.voice.log.VoiceLogger;
import com.voice.server.privatechat.PrivateChat;
import com.voice.server.group.GroupChat;
import com.voice.server.service.MicServerService;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.LinkedBlockingQueue;


public class SoundPacketHandlerServerThread implements Runnable {
    MicServerService serverService = MicServerService.getInstance();
    @Override
    public void run() {
        try {
            LinkedBlockingQueue<ServerPlayer> serverPlayerList = serverService.getServerPlayerList();
            if (!serverService.serverPacketHandlersServerSideIsEmpty()) {
                int size = serverPlayerList.size();
                ServerPlayer[] serverPlayers = serverPlayerList.toArray(new ServerPlayer[0]);
                ServerPlayer sender = null;
                ClientMicDataPacketHandler curPacket = serverService.pollPacket();
                if (curPacket == null) {
                    return;
                }
                PointerFree.addServerPointer(curPacket.getSendPacket().x);
                for (ServerPlayer getSender : serverPlayers) {
                    if (getSender.getDisplayName().getString().equals(curPacket.getPlayerId())) {
                        sender = getSender;
                        break;
                    }
                }
                if (sender == null) {
                    VoiceLogger.warn("The voice sender was not found: 没有找到对应的语音发送者", null);
                    return;
                }
                GroupChat chat = GroupChat.getInstance();
                PrivateChat privateChat = PrivateChat.getInstance();
                ServerPlayer player;
                if (chat.isInGroup(sender)) {
                    ServerPlayer[] players = chat.getServerPlayers(sender);
                    for (int i = 0; i < players.length; i++) {
                        player = players[i];
                        if (serverService.isSingleGame() || !sender.equals(player)) {
                            NetworkSender.sendPacketToPlayers(curPacket, player);
                        }
                    }
                } else if ((player = privateChat.getPrivateChatPlayer(sender)) != null) {
                    NetworkSender.sendPacketToPlayers(curPacket, player);
                } else {
                    for (int i = 0; i < size; i++) {
                        player = serverPlayers[i];
                        if (player != null) {
                            if (serverService.isSingleGame() || !sender.equals(player)) {
                                if (serverService.canSendVoice(sender, player)) {
                                    NetworkSender.sendPacketToPlayers(curPacket, player);
                                }
                            }
                        }
                    }
                }
                PointerFree.freeServerPointer();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
