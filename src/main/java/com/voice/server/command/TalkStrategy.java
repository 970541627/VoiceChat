package com.voice.server.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.voice.client.network.NetworkSender;
import com.voice.client.network.PrivateChatPacketHandler;
import com.voice.log.VoiceLogger;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TalkStrategy implements CommandStrategy {
    private PrivateChat privateChat = PrivateChat.getInstance();
    private GroupChat groupChat = GroupChat.getInstance();
    private Map<String, Method> methodMap = new HashMap<>();
    private GroupServerService groupService = GroupServerService.getInstance();
    private String respTalkCreate = "respTalkCreate";

    public TalkStrategy(){
        String methodName = "";
        TalkStrategy talkStrategy = (TalkStrategy) ReflectUtils.registerReflectClass(this);
        Map<String, String> commandWithCallbackNames = CommandConst.commandWithCallbackName;
        Map<String, Pair<String, Pair<String, ArgumentType>>> commandTypeMap = CommandConst.commandTypeMap;
        for (String command : commandWithCallbackNames.keySet()) {
            if (commandTypeMap.containsKey(command) && commandTypeMap.get(command).getLeft().equals(getType())) {
                try {
                    methodName = commandWithCallbackNames.get(command);
                    Method method = talkStrategy.getClass().getDeclaredMethod(methodName, ServerPlayer.class, String.class, Object.class);
                    method.setAccessible(true);
                    methodMap.put(methodName, method);
                } catch (NoSuchMethodException e) {
                    VoiceLogger.error("cannot found the method: " + methodName);
                    continue;
                }
            }
        }

        try {
            methodName=respTalkCreate;
            Method method = this.getClass().getDeclaredMethod(respTalkCreate, ServerPlayer.class, ServerPlayer.class, Object.class);
            method.setAccessible(true);
            methodMap.put(respTalkCreate, method);
        }catch (NoSuchMethodException e) {
            VoiceLogger.error("cannot found the method: " + methodName);
        }
        VoiceLogger.info("{0} :already loaded method instance amount {0}",this.getClass().getName(),methodMap.size());
    }

    @Override
    public void executor(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        try {
            methodMap.get(CommandConst.commandWithCallbackName.get(commandWithNoArgs)).invoke(this, sender, commandWithNoArgs, arg);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getType() {
        return CommandConst.TALK_TYPE;
    }

    private void setCancelTalk(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        privateChat.cancelPrivateChat(sender);
    }

    private void setCreateTalk(ServerPlayer sender, String commandWithNoArgs, @Nullable Object arg) {
        if (groupChat.isInGroup(sender)) {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你目前在群聊频道，无法建立私聊频道"));
            return;
        } else if (privateChat.getPrivateChatPlayer(sender) != null) {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你目前在已在私聊频道，无法建立私聊频道"));
            return;
        }
        ServerPlayer targetPlayer = (ServerPlayer) arg;
        if (groupChat.isInGroup(targetPlayer)) {
            sender.sendSystemMessage(ComponentUtils.playerInfo(targetPlayer.getDisplayName().getString(), "目前在群聊中，无法建立私聊频道"));
            return;
        } else if (privateChat.getPrivateChatPlayer(targetPlayer) != null) {
            sender.sendSystemMessage(ComponentUtils.playerInfo(targetPlayer.getDisplayName().getString(), "目前已在私聊中，无法建立私聊频道"));
            return;
        }

        if (targetPlayer.equals(sender)) {
            sender.sendSystemMessage(ComponentUtils.defaultMessage("你不能与自己私聊"));
        } else {
            Method method = methodMap.get(respTalkCreate);
            groupService.addGroupRequestTask(targetPlayer, method, this, sender, null);
            targetPlayer.sendSystemMessage(ComponentUtils.joinInfo(targetPlayer.getDisplayName().getString(), "请求与你私聊"));
        }
    }

    private void respTalkCreate(ServerPlayer sender, ServerPlayer targetPlayer, @Nullable Object arg) {
        if (!groupChat.isInGroup(sender)) {
            privateChat.createPrivateChat(sender, targetPlayer);
            ServerPlayer[] serverPlayers = new ServerPlayer[2];
            serverPlayers[0] = sender;
            serverPlayers[1] = targetPlayer;

            List<String> playerList = new ArrayList<>();
            playerList.add(sender.getDisplayName().getString());
            playerList.add(targetPlayer.getDisplayName().getString());

            NetworkSender.sendPrivateChatPacketToClient(new PrivateChatPacketHandler(playerList), serverPlayers);
            targetPlayer.sendSystemMessage(ComponentUtils.defaultMessage("已建立私聊频道"));
            sender.sendSystemMessage(ComponentUtils.defaultMessage("已建立私聊频道"));
        }
    }
}
