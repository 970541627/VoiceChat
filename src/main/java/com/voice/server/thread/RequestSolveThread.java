package com.voice.server.thread;

import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RequestSolveThread implements Runnable {

    private Map<ServerPlayer, Queue<Pair<Long, Object[]>>> playerRequestMap = new ConcurrentHashMap<>();
    private Map<ServerPlayer, Queue<ServerPlayer>> groupRequestMap = new ConcurrentHashMap<>();
    private long timeout = 5000;


    public void addTask(ServerPlayer dest, Method method, Object classInstance, Object arg) {
        if (!playerRequestMap.containsKey(dest)) {
            ArrayDeque<Pair<Long, Object[]>> queue = new ArrayDeque<>();
            playerRequestMap.put(dest, queue);
        }
        playerRequestMap.get(dest).add(new ImmutablePair<>(System.currentTimeMillis(), new Object[]{method, classInstance, arg}));
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
        if (resp.equalsIgnoreCase("y")) {
            if (playerRequestMap.containsKey(sender)) {
                ServerPlayer joiner = groupRequestMap.get(sender).poll();
                Queue<Pair<Long, Object[]>> queue = playerRequestMap.get(sender);
                Pair<Long, Object[]> task = queue.poll();
                Object[] argObjects = task.getRight();
                Method method = (Method) argObjects[0];
                Object instance = argObjects[1];
                Object arg = argObjects[2];
                try {
                    method.invoke(instance, sender, joiner, arg);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        Set<ServerPlayer> players = playerRequestMap.keySet();
        long cur = System.currentTimeMillis();
        for (ServerPlayer sender : players) {
            if (playerRequestMap.containsKey(sender)) {
                Queue<Pair<Long, Object[]>> pairDataQueue = playerRequestMap.get(sender);
                Iterator<Pair<Long, Object[]>> it = pairDataQueue.iterator();
                while (it.hasNext()) {
                    var pair = it.next();
                    if (cur - pair.getLeft() >= timeout) {
                        it.remove();
                    }
                }
            }
        }
    }
}
