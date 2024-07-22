package com.voice.basslib;

import com.voice.log.VoiceLogger;
import com.sun.jna.Native;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class BASSNative {
    public static CLib INSTANCE;
    public static File dll;
    public static File bass;

    public static CLib getInstance() {
        if (INSTANCE == null) {
            try {
                URL resource = Thread.currentThread().getContextClassLoader().getResource("");
                if (!resource.getProtocol().equals("jar")) {
                    INSTANCE = Native.load("DLL1.dll", CLib.class);
                    return INSTANCE;
                }
                File dllSorce = Native.extractFromResourcePath("DLL1.dll");
                dll = new File(dllSorce.getParent() + "\\DLL1.dll");
                if (dll.exists()) {
                    dll.delete();
                }
                dllSorce.renameTo(dll);
                File bassSource = Native.extractFromResourcePath("bass.dll");
                bass = new File(bassSource.getParent() + "\\bass.dll");
                if (bass.exists()) {
                    bass.delete();
                }
                bassSource.renameTo(bass);
                INSTANCE = (CLib) Native.load(dll.getPath(), CLib.class);
                VoiceLogger.info("The native lib has loaded Successfully: 本地库已加载完毕");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return INSTANCE;
    }
}