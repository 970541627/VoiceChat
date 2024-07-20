package com.voice.config;

import com.voice.group.Group;
import com.voice.group.GroupChat;
import com.voice.log.VoiceLogger;
import com.voice.network.GroupPacketHandler;
import com.voice.network.NetworkSender;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GroupConfig {
    private URL savePathURL = Thread.currentThread().getContextClassLoader().getResource("");
    private String dictionary = "voiceChat";
    private static GroupConfig config;

    private String restoreFileName = "groups";
    private String restoreFileSuffix = ".gp";
    private Map<String, Object> groupIdCache;

    private GroupConfig() {
        restoreGroup();
    }

    public static GroupConfig getConfigInstance() {
        return config == null ? config = new GroupConfig() : config;
    }


    public void saveGroupData(ServerPlayer savePlayer, String groupId) {
        String playerName = savePlayer.getDisplayName().getString();
        Map<String, Object> groupConfig = new HashMap<>();
        groupConfig.put("player", playerName);
        groupConfig.put("groupId", groupId);
        this.saveGroupSettings(groupConfig, playerName, ".group");
        VoiceLogger.info("玩家" + playerName + "群组信息已经保存");
    }

    public boolean readGroupData(ServerPlayer player) {
        String playerName = player.getDisplayName().getString();
        Map<String, Object> playerGroupDataMap = this.readGroupConfigSettings(playerName, ".group");
        String playerKey = "player";
        String groupIdKey = "groupId";
        if (playerGroupDataMap != null) {
            if (playerGroupDataMap.containsKey(playerKey) && playerGroupDataMap.containsKey(groupIdKey)) {
                String playerNameInGroup = (String) playerGroupDataMap.get(playerKey);
                String groupId = (String) playerGroupDataMap.get(groupIdKey);
                if (groupId.equals("")) {
                    return false;
                }
                GroupChat groupChat = GroupChat.getInstance();
                Group getGroup = groupChat.getGroupById(groupId);
                if (getGroup != null) {
                    groupChat.joinGroup(player, groupId);
                    NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(groupChat.getServerPlayersName(player)), getGroup.groupPlayerList());
                    VoiceLogger.info("玩家" + playerName + "已加入之前加入的群组");
                    return true;
                } else if (groupIdCache.containsKey(groupId)) {
                    Integer maxPerson = (Integer) groupIdCache.get(groupId);
                    groupChat.createGroup(player, maxPerson, groupId);
                    ServerPlayer[] players = groupChat.getGroupById(groupId).groupPlayerList();
                    NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(groupChat.getServerPlayersName(player)), players);
                    return true;
                } else {
                    player.sendSystemMessage(Component.literal("上次加入的群聊不存在或已解散，群号：" + groupId).withColor(0xFFFF0000));
                }
            }
            playerGroupDataMap.clear();
            this.saveGroupData(player, "");
        }
        return false;
    }

    public void saveGroup(String groupId, Integer maxPerson) {
        if (groupIdCache.containsKey(groupId)) {
            return;
        }
        groupIdCache.put(groupId, maxPerson);
        this.saveGroupSettings(groupIdCache, restoreFileName, restoreFileSuffix);
        VoiceLogger.info("新增了新群组:" + groupId + ",更新了当前群组文件");
    }

    public void removeGroupCache(String groupId) {
        if (groupIdCache.containsKey(groupId)) {
            groupIdCache.remove(groupId);
            this.saveGroupSettings(groupIdCache, restoreFileName, restoreFileSuffix);
            VoiceLogger.info("删除了当前群组：" + groupId + "，更新了当前群组文件");
        }
    }

    public void restoreGroup() {
        groupIdCache = this.readGroupConfigSettings(restoreFileName, restoreFileSuffix);
        if (groupIdCache == null) {
            groupIdCache = new HashMap<>();
            return;
        }
        VoiceLogger.info("已恢复群组数量：" + groupIdCache.size());
    }

    public void saveGroupSettings(Map<String, Object> serializableMap, String fileName, String suffix) {
        try {
            String savePath;
            if (savePathURL.getProtocol().equals("jar")) {
                savePath = savePathURL.getPath().substring(5);
            } else {
                savePath = savePathURL.getPath();
            }
            savePath = savePath.replaceAll("%20", " ");
            File configFile = new File(new File(savePath).getParent() + "/" + dictionary + "/" + fileName + suffix);
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            FileOutputStream stream = new FileOutputStream(configFile);
            ObjectOutputStream obout = new ObjectOutputStream(stream);
            obout.writeObject(serializableMap);
            obout.close();
            stream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> readGroupConfigSettings(String fileName, String suffix) {
        String savePath;
        if (savePathURL.getProtocol().equals("jar")) {
            savePath = savePathURL.getPath().substring(5);
        } else {
            savePath = savePathURL.getPath();
        }
        File file = new File(new File(savePath).getParent());
        savePath = file.getPath().replaceAll("%20", " ");
        File dic = new File(savePath + "/" + dictionary);
        File configFile = new File(dic.getPath() + "/" + fileName + suffix);
        if (!dic.exists()) {
            dic.mkdir();
            return null;
        } else if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
        try (FileInputStream inputStream = new FileInputStream(configFile); ObjectInputStream obin = new ObjectInputStream(inputStream)) {
            Map<String, Object> serializableMap = (Map<String, Object>) obin.readObject();
            obin.close();
            inputStream.close();
            return serializableMap;
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
