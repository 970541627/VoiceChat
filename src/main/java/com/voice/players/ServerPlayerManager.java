package com.voice.players;

import com.voice.config.Config;
import com.voice.config.DistanceConfig;
import com.voice.log.VoiceLogger;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class ServerPlayerManager {
    private static LinkedBlockingQueue<ServerPlayer> serverPlayerList = new LinkedBlockingQueue<ServerPlayer>();
    private static Map<String, Double> distanceLimitMap = new HashMap<>();
    private static ServerPlayer player;
    private static Config config = Config.getConfigInstance();
    private static List<String> groupPlayers;
    private static List<String> talkPlayers;
    private static String whoIsTalking="";

    public static String getWhoIsTalking() {
        return whoIsTalking;
    }

    public static void setWhoIsTalking(String whoIsTalking) {
        ServerPlayerManager.whoIsTalking = whoIsTalking;
    }

    public static void updateGroupList(List<String> newList) {
        groupPlayers = newList;
    }

    public static void updateTalkList(List<String> newList) {
        talkPlayers = newList;
    }

    public static List<String> getGroupPlayers() {
        return groupPlayers;
    }

    public static List<String> getTalkPlayers() {
        return talkPlayers;
    }

    public static boolean groupIsNotNull() {
        return groupPlayers != null && !groupPlayers.isEmpty();
    }

    public static boolean talkIsNotNull() {
        return talkPlayers != null && !talkPlayers.isEmpty();
    }

    public static void setServerPlayer(ServerPlayer initPlayer) {
        player = initPlayer;
    }

    public static void setServerPlayer() {
        player = getServerPlayerList().peek();
    }

    public static ServerPlayer getPlayer() {
        return player;
    }

    public static void setDistanceLimit(String playerName, double limit) {
        distanceLimitMap.put(playerName, limit);
    }

    public static void removeDistanceLimit(String playerName) {
        Map<String, Object> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(playerName, distanceLimitMap.get(playerName));
        DistanceConfig.getConfigInstance().saveVoiceSettings(stringIntegerHashMap, playerName, ".set");
        distanceLimitMap.remove(playerName);
        VoiceLogger.info("玩家{0}退出游戏，他的声音距离配置文件已保存", playerName);
    }

    public static LinkedBlockingQueue<ServerPlayer> getServerPlayerList() {
        return serverPlayerList;
    }


    public static boolean canSendVoice(ServerPlayer player, ServerPlayer target) {
        if (!player.level().dimension().equals(target.level().dimension())) {
            return false;
        }
        double distanceLimit = 100.00;
        String name = target.getDisplayName().getString();
        if (distanceLimitMap.containsKey(name)) {
            distanceLimit = distanceLimitMap.get(name);
        }
        double distancePow = calculatePow(player.getX() - target.getX())
                + calculatePow(player.getY() - target.getY())
                + calculatePow(player.getZ() - target.getZ());
        return Math.sqrt(distancePow) <= distanceLimit && Math.sqrt(distancePow) >= 0;
    }

    private static double calculatePow(double source) {
        return Math.pow(source, 2);
    }
}
