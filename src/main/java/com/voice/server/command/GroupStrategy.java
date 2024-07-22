package com.voice.server.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.voice.client.network.GroupPacketHandler;
import com.voice.client.network.NetworkSender;
import com.voice.log.VoiceLogger;
import com.voice.server.group.Group;
import com.voice.server.group.GroupChat;
import com.voice.server.privatechat.PrivateChat;
import com.voice.server.service.GroupServerService;
import com.voice.util.ComponentUtils;
import com.voice.util.ReflectUtils;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class GroupStrategy implements CommandStrategy {
    private Map<String, Method> methodMap = new HashMap<>();
    private Map<String, String> commandToMethodNameMap = new HashMap<>();
    private PrivateChat privateChat = PrivateChat.getInstance();
    private GroupChat groupChat = GroupChat.getInstance();
    private GroupServerService groupService = GroupServerService.getInstance();
    private String respJoinGroup = "respJoinGroup";


    public GroupStrategy() {
        Class unsafeClass = null;
        String methodName = null;
        GroupStrategy groupStrategy = (GroupStrategy) ReflectUtils.registerReflectClass(this);
        Map<String, String> commandWithCallbackNames = CommandConst.commandWithCallbackName;
        Map<String, Pair<String, Pair<String, ArgumentType>>> commandTypeMap = CommandConst.commandTypeMap;
        for (String command : commandWithCallbackNames.keySet()) {
            if (commandTypeMap.containsKey(command) && commandTypeMap.get(command).getLeft().equals(getType())) {
                try {
                    methodName = commandWithCallbackNames.get(command);
                    Method method = groupStrategy.getClass().getDeclaredMethod(methodName, ServerPlayer.class, String.class, Object.class);
                    method.setAccessible(true);
                    methodMap.put(methodName, method);
                } catch (NoSuchMethodException e) {
                    VoiceLogger.error("cannot found the method: " + methodName);
                    continue;
                }
            }
        }
        try {
            methodName = respJoinGroup;
            Method respJoinGroupMethod = groupStrategy.getClass().getDeclaredMethod(methodName, ServerPlayer.class, ServerPlayer.class, Object.class);
            respJoinGroupMethod.setAccessible(true);
            methodMap.put(methodName, respJoinGroupMethod);
        } catch (NoSuchMethodException e) {
            VoiceLogger.error("cannot found the method: " + methodName);
        }
        VoiceLogger.info("{0} :already loaded method instance amount {0}",this.getClass().getName(),methodMap.size());
    }

    @Override
    public void executor(ServerPlayer player, String commandWithNoArgs, @Nullable Object arg) {
        Method method = methodMap.get(CommandConst.commandWithCallbackName.get(commandWithNoArgs));
        try {
            method.invoke(this, player, commandWithNoArgs, arg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    private void joinGroup(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        String id = (String) arg;
        if (groupChat.isInGroup(sender)) {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你已加入了群组，请勿重复加入"));
            return;
        }
        Group group = groupChat.getGroupById(id);
        if (group != null) {
            Method method = methodMap.get(respJoinGroup);
            groupService.addGroupRequestTask(group.getOwner(), method, this, sender, arg);
            groupChat.notifyOwner(group, ComponentUtils.joinInfo(sender.getDisplayName().getString(), "请求进入群组"));
        } else {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你输入的群号" + id + "不存在"));
        }
    }

    private void respJoinGroup(ServerPlayer sender, ServerPlayer joiner, @Nullable Object arg) {
        if (privateChat.getPrivateChatPlayer(sender) == null) {
            if (joiner != null) {
                groupChat.joinGroup(joiner, (String) arg);
                ServerPlayer[] players = groupChat.getServerPlayers(sender);
                GroupPacketHandler groupPacketHandler = new GroupPacketHandler(groupChat.getServerPlayersName(joiner));
                NetworkSender.sendGroupPlayersMembersToClient(groupPacketHandler, players);
                for (ServerPlayer player : players) {
                    player.sendSystemMessage(ComponentUtils.playerInfo(player.getDisplayName().getString(), "进入了群聊"));
                }
            }
        }
    }


    private void createGroup(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        if (privateChat.getPrivateChatPlayer(sender) == null) {
            if (groupChat.createGroup(sender, (Integer) arg)) {
                ServerPlayer[] players = groupChat.getServerPlayers(sender);
                GroupPacketHandler groupPacketHandler = new GroupPacketHandler(groupChat.getServerPlayersName(sender));
                NetworkSender.sendGroupPlayersMembersToClient(groupPacketHandler, players);
                sender.sendSystemMessage(ComponentUtils.defaultMessage("群组创建成功,群号为：", groupChat.getGroupId(sender), "\n"));
            } else {
                sender.sendSystemMessage(ComponentUtils.defaultMessage("群组创建失败，你已经在群组中"));
            }
        }
    }

    private void quitGroup(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        if (!groupChat.isInGroup(sender)) {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你并没有加入任何群组"));
            return;
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
                    originalGroupPlayers[i] = null;
                }
            }
            GroupPacketHandler groupPacketHandler = new GroupPacketHandler(serverPlayerNames);
            GroupPacketHandler quiterPacketHandler = new GroupPacketHandler(new ArrayList<>());
            NetworkSender.sendGroupPlayersMembersToClient(groupPacketHandler, originalGroupPlayers);
            NetworkSender.sendGroupPlayersMembersToClient(quiterPacketHandler, new ServerPlayer[]{sender});
            String quitName = sender.getDisplayName().getString();
            for (ServerPlayer player : restPlayers) {
                if (!player.equals(sender)) {
                    player.sendSystemMessage(ComponentUtils.playerInfo(quitName, "退出了群聊"));
                }
            }
        }
    }

    private void kickPlayerFromGroup(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        ServerPlayer target = (ServerPlayer) arg;
        if (!sender.equals(target)) {
            Group group = groupChat.kickPlayer(sender, target);
            if (group != null) {
                ServerPlayer[] restPlayers = group.groupPlayerList();
                List<String> serverPlayerNames = new ArrayList<>();
                for (int i = 0; i < restPlayers.length; i++) {
                    serverPlayerNames.add(restPlayers[i].getDisplayName().getString());
                }
                NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(serverPlayerNames), restPlayers);
                NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(new ArrayList<>()), new ServerPlayer[]{target});
                target.sendSystemMessage(ComponentUtils.defaultMessage("你已被群主踢出群聊"));
            } else {
                sender.sendSystemMessage(ComponentUtils.defaultMessage("你不是群主，无法踢人"));
            }
        } else {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你不能踢你自己"));
        }
    }


    private void invitePlayerFromGroup(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        ServerPlayer invitedPlayer = (ServerPlayer) arg;
        if(!groupChat.isInGroup(sender)){
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你未加入任何群组，无法邀请玩家"));
            return;
        }
        if (invitedPlayer.equals(sender)) {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你不能邀请你自己"));
            return;
        }
        if (privateChat.getPrivateChatPlayer(invitedPlayer) != null) {
            sender.sendSystemMessage(ComponentUtils.playerInfo(invitedPlayer.getDisplayName().getString(), " 在私聊频道,无法邀请"));
            return;
        }

        String groupId = groupChat.getGroupId(sender);
        if (!groupChat.getGroupById(groupId).isOwner(sender)) {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你不是房主，没权限邀请人"));
            return;
        }
        if (groupChat.joinGroup(invitedPlayer, groupId)) {
            ServerPlayer[] players = groupChat.getServerPlayers(sender);
            List<String> playersName = groupChat.getServerPlayersName(sender);
            NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(playersName), players);
            sender.sendSystemMessage(ComponentUtils.defaultMessage("邀请玩家", invitedPlayer.getDisplayName().getString(), "成功"));
            invitedPlayer.sendSystemMessage(ComponentUtils.defaultMessage("你已被玩家", sender.getDisplayName().getString(), "邀请至群组"));
        } else {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("邀请玩家", invitedPlayer.getDisplayName().getString(), "失败"));
        }
    }

    private void getGroupId(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        sender.sendSystemMessage(ComponentUtils.defaultMessage("群组id：", GroupChat.getInstance().getGroupId(sender), ""));
    }

    @Override
    public String getType() {
        return CommandConst.GROUP_TYPE;
    }
}
