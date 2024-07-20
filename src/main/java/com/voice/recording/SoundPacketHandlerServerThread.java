package com.voice.recording;

import com.voice.group.GroupChat;
import com.voice.log.VoiceLogger;
import com.voice.network.ClientPacketHandler;
import com.voice.network.NetworkSender;
import com.voice.network.PointerFree;
import com.voice.players.ServerPlayerManager;
import com.voice.privatechat.PrivateChat;
import net.minecraft.server.level.ServerPlayer;

import java.util.concurrent.LinkedBlockingQueue;

import static com.voice.recording.SoundDataCenter.isSingleGame;


public class SoundPacketHandlerServerThread implements Runnable {
    LinkedBlockingQueue<ServerPlayer> serverPlayerList = ServerPlayerManager.getServerPlayerList();

    @Override
    public void run() {
        try {
            if (!SoundDataCenter.serverPacketHandlersServerSide.isEmpty()) {
                int size = serverPlayerList.size();
                ServerPlayer[] serverPlayers = serverPlayerList.toArray(new ServerPlayer[0]);
                ServerPlayer sender = null;
                ClientPacketHandler curPacket = SoundDataCenter.serverPacketHandlersServerSide.poll();
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
                        if (isSingleGame || !sender.equals(player)) {
                            NetworkSender.sendPacketToPlayers(curPacket, player);
                        }
                    }
                } else if ((player = privateChat.getPrivateChatPlayer(sender)) != null) {
                    NetworkSender.sendPacketToPlayers(curPacket, player);
                } else {
                    for (int i = 0; i < size; i++) {
                        player = serverPlayers[i];
                        if (player != null) {
                            if (isSingleGame || !sender.equals(player)) {
                                if (ServerPlayerManager.canSendVoice(sender, player)) {
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
