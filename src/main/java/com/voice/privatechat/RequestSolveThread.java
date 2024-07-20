package com.voice.privatechat;

import com.voice.command.CommandConst;
import com.voice.group.GroupChat;
import com.voice.network.GroupPacketHandler;
import com.voice.network.NetworkSender;
import com.voice.network.PrivateChatPacketHandler;
import com.voice.players.ServerPlayerManager;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RequestSolveThread implements Runnable {

    private Map<ServerPlayer, Queue<Pair<Long, CommandContext<CommandSourceStack>>>> playerRequestMap = new ConcurrentHashMap<>();
    private Map<ServerPlayer, Queue<ServerPlayer>> groupRequestMap = new ConcurrentHashMap<>();
    private long timeout = 5000;


    public void addTask(ServerPlayer dest, CommandContext<CommandSourceStack> requestCommand) {
        if (!playerRequestMap.containsKey(dest)) {
            ArrayDeque<Pair<Long, CommandContext<CommandSourceStack>>> queue = new ArrayDeque<>();
            playerRequestMap.put(dest, queue);
        }
        playerRequestMap.get(dest).add(new ImmutablePair<>(System.currentTimeMillis(), requestCommand));
    }

    public void addGroupRequestQueue(ServerPlayer owner, ServerPlayer joiner) {
        if (!groupRequestMap.containsKey(owner)) {
            groupRequestMap.put(owner, new ArrayDeque<>());
        }
        groupRequestMap.get(owner).add(joiner);
    }

    public int getReqSize(ServerPlayer sender) {
        if (playerRequestMap.containsKey(sender)) {
            return playerRequestMap.get(sender).size();
        }
        return 0;
    }

    public void respTask(ServerPlayer sender, String resp, @Nullable ServerPlayer target) {
        PrivateChat privateChat = PrivateChat.getInstance();
        GroupChat groupChat = GroupChat.getInstance();
        if (playerRequestMap.containsKey(sender)) {
            if (resp.equalsIgnoreCase("y")) {
                Pair<Long, CommandContext<CommandSourceStack>> pairData = playerRequestMap.get(sender).poll();
                CommandContext<CommandSourceStack> context = pairData.getRight();
                String commandsLine = context.getInput();
                if (commandsLine.startsWith(CommandConst.JOIN_GROUP) && privateChat.getPrivateChatPlayer(sender) == null) {
                    if (groupRequestMap.containsKey(sender)) {
                        ServerPlayer joiner = groupRequestMap.get(sender).poll();
                        if (joiner != null) {
                            groupChat.joinGroup(joiner, context.getArgument("id", String.class));
                            ServerPlayer[] players = groupChat.getServerPlayers(sender);
                            ServerPlayerManager.updateGroupList(groupChat.getServerPlayersName(sender));
                            GroupPacketHandler groupPacketHandler = new GroupPacketHandler(ServerPlayerManager.getGroupPlayers());
                            NetworkSender.sendGroupPlayersMembersToClient(groupPacketHandler, getGroupServerPlayers(sender));
                            sender.sendSystemMessage(CommandConst.groupJoinRequest(sender.getDisplayName().getString()));
                            for (ServerPlayer player : players) {
                                player.sendSystemMessage(Component.literal("玩家").withColor(0xFFFF0000)
                                        .append(joiner.getDisplayName().getString()).withColor(0xFFFFFF00)
                                        .append("加入了群聊\n").withColor(0xFFFF0000));
                            }
                        }

                    }

                } else if (commandsLine.startsWith(CommandConst.SET_CREATE_TALK) && !groupChat.isInGroup(sender)) {
                    EntitySelector player = context.getArgument("player", EntitySelector.class);
                    if (groupRequestMap.containsKey(sender)) {
                        Queue<ServerPlayer> players = groupRequestMap.get(sender);
                        if (players.size()!=0) {
                            ServerPlayer targetPlayer = players.poll();
                            privateChat.createPrivateChat(sender, targetPlayer);
                            ServerPlayer[] serverPlayers = new ServerPlayer[2];
                            serverPlayers[0] = sender;
                            serverPlayers[1] = targetPlayer;

                            List<String> playerList = new ArrayList<>();
                            playerList.add(sender.getDisplayName().getString());
                            playerList.add(targetPlayer.getDisplayName().getString());

                            NetworkSender.sendPrivateChatPacketToClient(new PrivateChatPacketHandler(playerList), serverPlayers);
                            targetPlayer.sendSystemMessage(Component.literal("已建立私聊频道").withColor(0xFFFF0000));
                            sender.sendSystemMessage(Component.literal("已建立私聊频道").withColor(0xFFFF0000));
                        }
                    }
                }

            }
        }
    }

    private ServerPlayer[] getGroupServerPlayers(ServerPlayer member) {
        ServerPlayer[] serverPlayers = GroupChat.getInstance().getServerPlayers(member);
        return serverPlayers;
    }


    @Override
    public void run() {
        Set<ServerPlayer> players = playerRequestMap.keySet();
        long cur = System.currentTimeMillis();
        for (ServerPlayer sender : players) {
            if (playerRequestMap.containsKey(sender)) {
                Queue<Pair<Long, CommandContext<CommandSourceStack>>> pairDataQueue = playerRequestMap.get(sender);
                Iterator<Pair<Long, CommandContext<CommandSourceStack>>> it = pairDataQueue.iterator();
                while (it.hasNext()) {
                    var pair = it.next();
                    if (cur - pair.getLeft() >= timeout) {
                        if (pair.getRight().getInput().startsWith(CommandConst.JOIN_GROUP)) {
                            if (groupRequestMap.containsKey(sender)) {
                                groupRequestMap.get(sender).poll();
                            }
                            it.remove();
                        } else if (pair.getRight().getInput().startsWith(CommandConst.SET_CANCEL_TALK)) {
                            if (groupRequestMap.containsKey(sender)) {
                                groupRequestMap.get(sender).poll();
                            }
                            it.remove();
                        }
                    }
                }
            }
        }
    }
}
