package com.voice.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.voice.group.Group;
import com.voice.group.GroupChat;
import com.voice.log.VoiceLogger;
import com.voice.network.CommandPacketHandler;
import com.voice.network.GroupPacketHandler;
import com.voice.network.NetworkSender;
import com.voice.players.ServerPlayerManager;
import com.voice.privatechat.PrivateChat;
import com.voice.recording.SoundDataCenter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CommandExecutor implements Command<CommandSourceStack> {
    public static Command<CommandSourceStack> instance = new CommandExecutor();
    private static GroupChat groupChat = GroupChat.getInstance();
    private static PrivateChat privateChat = PrivateChat.getInstance();


    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        String commandsLine = context.getInput();
        ServerPlayer sender = context.getSource().getPlayer();
        if (sender == null) {
            VoiceLogger.warn("Could not find the Command Sender Player: 没有在玩家列表中找到发送指令的玩家", null);
            return 0;
        } else if (commandsLine.startsWith(CommandConst.SET_MIC_POS_X)) {
            CommandPacketHandler<Integer> commandPacket = new CommandPacketHandler<Integer>(CommandConst.SET_MIC_POS_X, context.getArgument("rate", Integer.TYPE));
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
        } else if (commandsLine.startsWith(CommandConst.SET_MIC_POS_Y)) {
            CommandPacketHandler<Integer> commandPacket = new CommandPacketHandler<Integer>(CommandConst.SET_MIC_POS_Y, context.getArgument("rate", Integer.TYPE));
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
        } else if (commandsLine.startsWith(CommandConst.SET_VOLUME_LIMIT)) {
            CommandPacketHandler<Float> commandPacket = new CommandPacketHandler<Float>(CommandConst.SET_VOLUME_LIMIT, context.getArgument("rate", Float.TYPE));
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
            sender.sendSystemMessage(Component.literal("成功调整音量阈值").withColor(0xFFFF0000));
        } else if (commandsLine.startsWith(CommandConst.SET_VOLUME)) {
            CommandPacketHandler<Float> commandPacket = new CommandPacketHandler<Float>(CommandConst.SET_VOLUME, context.getArgument("rate", Float.TYPE));
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
            sender.sendSystemMessage(Component.literal("成功调整收听音量").withColor(0xFFFF0000));
        } else if (commandsLine.startsWith(CommandConst.SET_PACKET_LENGTH)) {
            CommandPacketHandler<Integer> commandPacket = new CommandPacketHandler<Integer>(CommandConst.SET_PACKET_LENGTH, context.getArgument("len", Integer.TYPE));
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
        } else if (commandsLine.startsWith(CommandConst.SET_MUTE)) {
            CommandPacketHandler<Integer> commandPacket = new CommandPacketHandler<Integer>(CommandConst.SET_MUTE, null);
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
            sender.sendSystemMessage(Component.literal("静音模式开启").withColor(0xFFFF0000));
        } else if (commandsLine.startsWith(CommandConst.SET_NO_MUTE)) {
            CommandPacketHandler<Integer> commandPacket = new CommandPacketHandler<Integer>(CommandConst.SET_NO_MUTE, null);
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
            sender.sendSystemMessage(Component.literal("静音模式关闭").withColor(0xFFFF0000));
        } else if (commandsLine.startsWith(CommandConst.GET_GET_AVG_LEVEL)) {
            CommandPacketHandler<Integer> commandPacket = new CommandPacketHandler<Integer>(CommandConst.GET_GET_AVG_LEVEL, null);
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
        } else if (commandsLine.startsWith(CommandConst.GET_HELP)) {
            CommandPacketHandler<Integer> commandPacket = new CommandPacketHandler<Integer>(CommandConst.GET_HELP, null);
            NetworkSender.sendExecuteCommandToClient(commandPacket, sender);
        } else if (commandsLine.startsWith(CommandConst.SET_DISTANCE)) {
            ServerPlayerManager.setDistanceLimit(sender.getDisplayName().getString(), (double) context.getArgument("dis", Integer.TYPE));
            sender.sendSystemMessage(Component.literal("已调整收听范围为：").withColor(0xFFFF0000)
                    .append(Component.literal(String.valueOf(context.getArgument("dis", Integer.TYPE)))).withColor(0xFFFFFF00));
        } else {
            if (commandsLine.startsWith(CommandConst.CREATE_GROUP) && privateChat.getPrivateChatPlayer(sender) == null) {
                if (groupChat.createGroup(sender, context.getArgument("max", Integer.TYPE))) {
                    ServerPlayer[] players = groupChat.getServerPlayers(sender);
                    ServerPlayerManager.updateGroupList(groupChat.getServerPlayersName(sender));
                    GroupPacketHandler groupPacketHandler = new GroupPacketHandler(ServerPlayerManager.getGroupPlayers());
                    NetworkSender.sendGroupPlayersMembersToClient(groupPacketHandler, players);
                    sender.sendSystemMessage(Component.literal("群组创建成功,群号为：" + groupChat.getGroupId(sender) + "\n").withColor(0xFFFF0000));
                } else {
                    sender.sendSystemMessage(Component.literal("群组创建失败，你可能已经在群组中\n").withColor(0xFFFF0000));
                }

            } else if (commandsLine.startsWith(CommandConst.JOIN_GROUP) && privateChat.getPrivateChatPlayer(sender) == null) {
                String id = context.getArgument("id", String.class);
                Group group = groupChat.getGroupById(id);
                if (group != null) {
                    SoundDataCenter.addGroupRequestTask(group.getOwner(), context, sender);
                    groupChat.notifyOwner(group, CommandConst.groupJoinRequest(sender.getDisplayName().getString()));
                } else {
                    sender.sendSystemMessage(Component.literal("你输入的群号" + id + "不存在").withColor(0xFFFF0000));
                }

            } else if (commandsLine.startsWith(CommandConst.GET_GROUP_ID) && privateChat.getPrivateChatPlayer(sender) == null) {
                sender.sendSystemMessage(Component.literal("群组id：").withColor(0xFFFF0000).append(GroupChat.getInstance().getGroupId(sender) + "\n").withColor(0xFFFFFF00));
            } else if (commandsLine.startsWith(CommandConst.QUIT_GROUP) && privateChat.getPrivateChatPlayer(sender) == null) {
                if (!groupChat.isInGroup(sender)) {
                    sender.sendSystemMessage(Component.literal("你并没有加入任何群组").withColor(0xFFFF0000));
                    return 0;
                }
                Group originalGroup = groupChat.getGroupById(groupChat.getGroupId(sender));
                ServerPlayer[] originalPlayers = originalGroup.groupPlayerList();
                if (originalGroup.isOwner(sender)) {
                    groupChat.removePlayer(sender, true);
                    GroupPacketHandler groupPacketHandler = new GroupPacketHandler(new ArrayList<>());
                    NetworkSender.sendGroupPlayersMembersToClient(groupPacketHandler, originalPlayers);
                } else {
                    ServerPlayer[] originalGroupPlayers = groupChat.getServerPlayers(sender);
                    Group quitGroup = groupChat.removePlayer(sender, true);
                    ServerPlayer[] restPlayers = quitGroup.groupPlayerList();
                    List<String> serverPlayerNames = new LinkedList<>();
                    for (int i = 0; i < restPlayers.length; i++) {
                        serverPlayerNames.add(restPlayers[i].getDisplayName().getString());
                    }
                    for (int i = 0; i < originalGroupPlayers.length; i++) {
                        if (originalGroupPlayers[i].getDisplayName().getString().equals(sender.getDisplayName().getString())) {

                        }
                    }
                    ServerPlayerManager.updateGroupList(serverPlayerNames);
                    GroupPacketHandler groupPacketHandler = new GroupPacketHandler(ServerPlayerManager.getGroupPlayers());
                    GroupPacketHandler quiterPacketHandler = new GroupPacketHandler(new ArrayList<>());
                    NetworkSender.sendGroupPlayersMembersToClient(groupPacketHandler, originalGroupPlayers);
                    NetworkSender.sendGroupPlayersMembersToClient(quiterPacketHandler, new ServerPlayer[]{sender});
                    String quitName = sender.getDisplayName().getString();
                    for (ServerPlayer player : restPlayers) {
                        if (!player.equals(sender)) {
                            player.sendSystemMessage(Component.literal("玩家").withColor(0xFFFF0000)
                                    .append(quitName).withColor(0xFFFFFF00)
                                    .append("退出了群聊\n").withColor(0xFFFF0000));
                        }
                    }
                }
            } else if (commandsLine.startsWith(CommandConst.INVITE_PLAYER_FROM_GROUP)) {
                try {
                    ServerPlayer invitedPlayer = context.getArgument("player", EntitySelector.class).findSinglePlayer(context.getSource());
                    if (privateChat.getPrivateChatPlayer(invitedPlayer) != null) {
                        sender.sendSystemMessage(Component.literal("玩家" + invitedPlayer.getDisplayName().getString() + "已加入了私聊,无法邀请").withColor(0xFFFF0000));
                        return 0;
                    }

                    String groupId = groupChat.getGroupId(sender);
                    if (!groupChat.getGroupById(groupId).isOwner(sender)) {
                        sender.sendSystemMessage(Component.literal("你不是房主，没权限邀请人").withColor(0xFFFF0000));
                        return 0;
                    }
                    if (groupChat.joinGroup(invitedPlayer, groupId)) {
                        sender.sendSystemMessage(Component.literal("邀请玩家" + invitedPlayer.getDisplayName().getString() + "成功").withColor(0xFFFF0000));
                        ServerPlayer[] players = groupChat.getServerPlayers(sender);
                        List<String> playersName = groupChat.getServerPlayersName(sender);
                        ServerPlayerManager.updateGroupList(playersName);
                        NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(playersName), players);
                        invitedPlayer.sendSystemMessage(Component.literal("你已被玩家" + sender.getDisplayName().getString() + "邀请至群组").withColor(0xFFFF0000));
                    } else {
                        sender.sendSystemMessage(Component.literal("邀请玩家" + invitedPlayer.getDisplayName().getString() + "失败").withColor(0xFFFF0000));
                    }
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }

            } else if (commandsLine.startsWith(CommandConst.SET_CREATE_TALK)) {
                if (groupChat.isInGroup(sender)) {
                    sender.sendSystemMessage(Component.literal("你目前在群聊频道，无法建立私聊频道").withColor(0xFFFF0000));
                    return 0;
                } else if (privateChat.getPrivateChatPlayer(sender) != null) {
                    sender.sendSystemMessage(Component.literal("你目前在已在私聊频道，无法建立私聊频道").withColor(0xFFFF0000));
                    return 0;
                }
                EntitySelector player = context.getArgument("player", EntitySelector.class);
                try {
                    ServerPlayer singlePlayer = player.findSinglePlayer(context.getSource());
                    if (groupChat.isInGroup(singlePlayer)) {
                        sender.sendSystemMessage(Component.literal("玩家" + singlePlayer.getDisplayName().getString() + "目前在群聊中，无法建立私聊频道").withColor(0xFFFF0000));
                        return 0;
                    } else if (privateChat.getPrivateChatPlayer(singlePlayer) != null) {
                        sender.sendSystemMessage(Component.literal("玩家" + singlePlayer.getDisplayName().getString() + "目前已在私聊中，无法建立私聊频道").withColor(0xFFFF0000));
                        return 0;
                    }

                    if (singlePlayer.equals(sender)) {
                        sender.sendSystemMessage(Component.literal("你不能与自己私聊").withColor(0xFFFF0000));
                    } else {
                        SoundDataCenter.addGroupRequestTask(singlePlayer, context, sender);
                        singlePlayer.sendSystemMessage(CommandConst.talkJoinRequest(singlePlayer.getDisplayName().getString()));
                    }

                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            } else if (commandsLine.startsWith(CommandConst.SET_CANCEL_TALK) && !groupChat.isInGroup(sender)) {
                privateChat.cancelPrivateChat(sender);
            } else if (commandsLine.startsWith(CommandConst.KICK_PLAYER_FROM_GROUP) && privateChat.getPrivateChatPlayer(sender) == null) {
                EntitySelector player = context.getArgument("player", EntitySelector.class);
                try {
                    ServerPlayer target = player.findSinglePlayer(context.getSource());
                    if (!sender.equals(target)) {
                        Group group = groupChat.kickPlayer(sender, target);
                        if (group != null) {
                            ServerPlayer[] restPlayers = group.groupPlayerList();
                            List<String> serverPlayerNames = new ArrayList<>();
                            for (int i = 0; i < restPlayers.length; i++) {
                                serverPlayerNames.add(restPlayers[i].getDisplayName().getString());
                            }
                            target.sendSystemMessage(Component.literal("你已被群主踢出群聊\n").withColor(0xFFFF0000));
                            NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(serverPlayerNames), restPlayers);
                            NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(new ArrayList<>()), new ServerPlayer[]{target});
                        } else {
                            target.sendSystemMessage(Component.literal("你不是群主，无法踢人\n").withColor(0xFFFF0000));
                        }
                    } else {
                        sender.sendSystemMessage(Component.literal("你不能踢你自己").withColor(0xFFFF0000));
                    }

                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }


}
