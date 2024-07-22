package com.voice.server.service;

import com.voice.client.network.ClientMicDataPacketHandler;
import com.voice.config.DistanceConfig;
import com.voice.log.VoiceLogger;
import com.voice.server.thread.SoundPacketHandlerServerThread;
import com.voice.util.TimerUtils;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class MicServerService {
    private Map<String, Double> distanceLimitMap = new HashMap<>();
    private volatile ConcurrentLinkedQueue<ClientMicDataPacketHandler> serverPacketHandlersServerSide = new ConcurrentLinkedQueue<>();
    private static MicServerService serverService;
    private SoundPacketHandlerServerThread soundPacketHandlerServerThread;
    private boolean isSingleGame=false;
    private LinkedBlockingQueue<ServerPlayer> serverPlayerList=new LinkedBlockingQueue<>();


    public void removeServerPlayer(ServerPlayer player){
        serverPlayerList.remove(player);
    }

    public void addServerPlayer(ServerPlayer player){
        serverPlayerList.add(player);
    }

    public LinkedBlockingQueue<ServerPlayer> getServerPlayerList() {
        return serverPlayerList;
    }

    public void setServerPlayerList(LinkedBlockingQueue<ServerPlayer> serverPlayerList) {
        this.serverPlayerList = serverPlayerList;
    }

    public boolean isSingleGame() {
        return isSingleGame;
    }

    public void setSingleGame(boolean singleGame) {
        isSingleGame = singleGame;
    }

    public boolean serverPacketHandlersServerSideIsEmpty(){
        return serverPacketHandlersServerSide.isEmpty();
    }

    public ClientMicDataPacketHandler pollPacket(){
        return serverPacketHandlersServerSide.poll();
    }

    public void clearServerPacketHandlers() {
        serverPacketHandlersServerSide.clear();
    }

    public void addClientPacket(ClientMicDataPacketHandler packet) {
        serverPacketHandlersServerSide.add(packet);
    }
    public static MicServerService getInstance() {
        return serverService == null ? serverService = new MicServerService() : serverService;
    }

    public void startNetworkServerThread() {
        if (soundPacketHandlerServerThread == null) {
            soundPacketHandlerServerThread = new SoundPacketHandlerServerThread();
        }
        TimerUtils.startTimer(0, 10, soundPacketHandlerServerThread);
        TimerUtils.startTimer(0, 10, soundPacketHandlerServerThread);
    }
    public void setDistanceLimit(String playerName, double limit) {
        distanceLimitMap.put(playerName, limit);
    }

    public void removeDistanceLimit(String playerName) {
        Map<String, Object> stringIntegerHashMap = new HashMap<>();
        stringIntegerHashMap.put(playerName, distanceLimitMap.get(playerName));
        DistanceConfig.getConfigInstance().saveVoiceSettings(stringIntegerHashMap, playerName, ".set");
        distanceLimitMap.remove(playerName);
        VoiceLogger.info("玩家{0}退出游戏，他的声音距离配置文件已保存", playerName);
    }

    private double calculatePow(double source) {
        return Math.pow(source, 2);
    }

    public boolean canSendVoice(ServerPlayer player, ServerPlayer target) {
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
}
