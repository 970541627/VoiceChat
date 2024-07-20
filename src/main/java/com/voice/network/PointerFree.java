package com.voice.network;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PointerFree {
    private static ConcurrentLinkedQueue<Pointer> pointerServerList = new ConcurrentLinkedQueue<>();
    private static ConcurrentLinkedQueue<Pointer> pointerClientList = new ConcurrentLinkedQueue<>();
    private static Map<Pointer, Integer> pointerServerIntegerMap = new ConcurrentHashMap<>();
    private static Map<Pointer, Integer> pointerClientIntegerMap = new ConcurrentHashMap<>();
    private static final Integer CLIENT_MAX_COUNT = 30;
    private static final Integer SERVER_MAX_COUNT = 30;

    public static void addServerPointer(Pointer pointer) {
        pointerServerList.add(pointer);
        pointerServerIntegerMap.put(pointer, 0);
    }

    public static void freeServerPointer() {
        if (pointerServerList.size() <= 0) {
            return;
        }
        Iterator<Pointer> iterator = pointerServerList.iterator();
        while (iterator.hasNext()) {
            Memory first = (Memory) iterator.next();
            if (pointerServerIntegerMap.containsKey(first)) {
                if (pointerServerIntegerMap.get(first) > SERVER_MAX_COUNT) {
                    iterator.remove();
                    pointerServerIntegerMap.remove(first);
                    if (Pointer.nativeValue(first) != 0L) {
                        first.close();
                    }
                } else {
                    pointerServerIntegerMap.replace(first, pointerServerIntegerMap.get(first) + 1);
                }
            }
        }

    }


    public static void addClientPointer(Pointer pointer) {
        pointerClientList.add(pointer);
        pointerClientIntegerMap.put(pointer, 0);
    }

    public static void freeClientPointer() {
        if (pointerClientList.size() <= 0) {
            return;
        }
        Iterator<Pointer> iterator = pointerClientList.iterator();
        while (iterator.hasNext()) {
            Memory first = (Memory) iterator.next();
            if (pointerClientIntegerMap.containsKey(first)) {
                if (pointerClientIntegerMap.get(first) > CLIENT_MAX_COUNT) {
                    iterator.remove();
                    pointerClientIntegerMap.remove(first);
                    if (Pointer.nativeValue(first) != 0L) {
                        first.close();
                    }
                } else {
                    pointerClientIntegerMap.replace(first, pointerClientIntegerMap.get(first) + 1);
                }
            }
        }

    }

    public static void freeClientAllPointer() {
        int size = pointerClientList.size();
        for (int i = 0; i < size; i++) {
            Memory poll = (Memory) pointerClientList.poll();
            poll.close();
        }
        pointerClientList.clear();
        pointerClientIntegerMap.clear();
    }
}
