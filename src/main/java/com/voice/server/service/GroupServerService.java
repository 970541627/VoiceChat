package com.voice.server.service;

import com.voice.config.Config;
import com.voice.server.thread.RequestSolveThread;
import com.voice.util.TimerUtils;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Method;
import java.util.concurrent.LinkedBlockingQueue;

public class GroupServerService {
    private  LinkedBlockingQueue<ServerPlayer> serverPlayerList = new LinkedBlockingQueue<ServerPlayer>();
    private  ServerPlayer player;
    private Config config = Config.getConfigInstance();
    private  RequestSolveThread requestSolveThread;

    private static GroupServerService groupServerService;
    public GroupServerService(){
        requestSolveThread=new RequestSolveThread();
        TimerUtils.startTimer(0,10,requestSolveThread);
    }
    public static GroupServerService getInstance(){
        return groupServerService==null?groupServerService=new GroupServerService():groupServerService;
    }
    public void addRespRequestTask(ServerPlayer dest, Method method, Object classInstance, Object args) {
        requestSolveThread.addTask(dest, method, classInstance, args);
    }

    public void addGroupRequestTask(ServerPlayer dest, Method method, Object classInstance, ServerPlayer joiner, Object args) {
        addRespRequestTask(dest, method, classInstance, args);
        requestSolveThread.addGroupRequestQueue(dest, joiner);
    }

    public int getGroupRequestSize(ServerPlayer sender) {
        return requestSolveThread.getReqSize(sender);
    }

    public void respReq(ServerPlayer sender, String resp) {
        requestSolveThread.respTask(sender, resp, null);
    }


    public  void setServerPlayer(ServerPlayer initPlayer) {
        player = initPlayer;
    }

    public  void setServerPlayer() {
        player = getServerPlayerList().peek();
    }

    public  ServerPlayer getPlayer() {
        return player;
    }



    public LinkedBlockingQueue<ServerPlayer> getServerPlayerList() {
        return serverPlayerList;
    }
}
