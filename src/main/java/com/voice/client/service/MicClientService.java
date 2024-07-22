package com.voice.client.service;

import com.mojang.brigadier.arguments.ArgumentType;
import com.voice.memory.PointerFree;
import com.voice.config.Config;
import com.voice.log.VoiceLogger;
import com.voice.server.command.CommandConst;
import com.voice.server.network.ServerMicDataPacketHandler;
import com.voice.server.service.MicServerService;
import com.voice.basslib.BASSNative;
import com.voice.basslib.CLib;
import com.voice.thread.SoundPacketHandlerClientThread;
import com.voice.util.ReflectUtils;
import com.voice.util.TimerUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MicClientService {
    private volatile ConcurrentLinkedQueue<BindData> audioBuffer = new ConcurrentLinkedQueue<>();
    private volatile ConcurrentLinkedQueue<ServerMicDataPacketHandler> serverMicDataPacketHandlersClineSide = new ConcurrentLinkedQueue<>();
    private Map<String, Field> configKeyMap = new HashMap<>();
    private SoundPacketHandlerClientThread soundPacketHandlerClientThread;
    private DecimalFormat demical = new DecimalFormat("0.00");
    private Config config = new Config();
    private volatile boolean isActive = false;
    private boolean mute = false;
    private Integer DEVICE = -1;
    private Integer DEFAULT_PACKET_LENGTH = 480 * 8;
    private Float DEFAULT_AUDIO_VOLUME = 1.0f;
    private Float DEFAULT_AUDIO_VOLUME_LIMIT = 0.20f;
    private static MicClientService micClientService;
    private Map<String, Method> methodMap = new HashMap<>();
    private Integer xRate = 18;
    private Integer yRate = 87;
    private String playerId;

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public Integer getxRate() {
        return xRate;
    }

    public Integer getyRate() {
        return yRate;
    }

    public void clearClientPacketHandlers() {
        serverMicDataPacketHandlersClineSide.clear();
    }

    public MicClientService() {
        MicClientService thisClass = (MicClientService) ReflectUtils.registerReflectClass(this);
        Map<String, Pair<String, Pair<String, ArgumentType>>> commandsInfo = CommandConst.commandTypeMap;
        Set<String> commands = commandsInfo.keySet();
        for (String command : commands) {
            Pair<String, Pair<String, ArgumentType>> commandInfoPair = commandsInfo.get(command);
            String type = commandInfoPair.getLeft();
            if (type.equals(CommandConst.MIC_TYPE)) {
                if (CommandConst.commandWithCallbackName.containsKey(command)) {
                    String callbackName = CommandConst.commandWithCallbackName.get(command);
                    try {
                        Method method = thisClass.getClass().getDeclaredMethod(callbackName, Object.class);
                        method.setAccessible(true);
                        methodMap.put(command, method);
                    } catch (NoSuchMethodException e) {
                        VoiceLogger.error("cannot found method " + callbackName);
                        continue;
                    }
                }
            }
        }
        Field[] fields = thisClass.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.get(this) instanceof Number) {
                    configKeyMap.put(field.getName(), field);
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    public void invokeMethod(String command, Object args) {
        try {
            if (methodMap.containsKey(command)) {
                methodMap.get(command).invoke(this, args);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public void setVolumeLimit(Object args) {
        Float arg = (Float) args;
        DEFAULT_AUDIO_VOLUME_LIMIT = arg;
        BASSNative.INSTANCE.set_record_volume_limit(DEFAULT_AUDIO_VOLUME_LIMIT);
    }

    public void setVolume(Object args) {
        Float arg = (Float) args;
        DEFAULT_AUDIO_VOLUME = (Float) arg;
        BASSNative.getInstance().setVolume(DEFAULT_AUDIO_VOLUME);
    }

    public void setPacketLength(Object args) {
        Float arg = (Float) args;
        DEFAULT_PACKET_LENGTH = ((Float) arg).intValue();
        BASSNative.INSTANCE.setLength(DEFAULT_PACKET_LENGTH);
    }


    public void setMute(Object args) {
        mute = true;
        BASSNative.INSTANCE.set_mute(true);
    }


    public void setNoMute(Object args) {
        mute = false;
        BASSNative.INSTANCE.set_mute(false);
    }

    public void getGetAvgLevel(Object args) {
        float avg_level = BASSNative.getInstance().get_avg_level();
        avg_level = micClientService.getFloatByTwoDecimalPlaces(avg_level);
        float range = 0.05f;
        String message = new StringBuilder("你的平均音量是：").append(avg_level).append(",建议修改声音阈值的范围为")
                .append(micClientService.getFloatByTwoDecimalPlaces(avg_level - range))
                .append("~")
                .append(micClientService.getFloatByTwoDecimalPlaces(avg_level + range))
                .append(",使用指令 /siro th <阈值>  例如：/siro th ")
                .append(micClientService.getFloatByTwoDecimalPlaces(avg_level + range)).toString();
        Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
    }

    public void getHelp(Object args) {
        Minecraft.getInstance().player.sendSystemMessage(CommandConst.helpMessage);
    }

    public void setMicPosX(Object args) {
        Float arg = (Float) args;
        xRate = arg.intValue();
    }

    public void setMicPosY(Object args) {
        Float arg = (Float) args;
        yRate = arg.intValue();
    }


    public static MicClientService getInstance() {
        return micClientService == null ? micClientService = new MicClientService() : micClientService;
    }

    public float getFloatByTwoDecimalPlaces(float orignal) {
        return Float.parseFloat(demical.format(orignal));
    }

    public void setDevice(int newDevice) {
        BASSNative.INSTANCE.init_device(DEVICE);
        DEVICE = newDevice;
    }


    public void startSoundThread() throws IllegalAccessException {
        Map<String, Object> serializableMap = config.readConfigSettings();
        if (serializableMap != null) {
            for (String fieldName : serializableMap.keySet()) {
                if (configKeyMap.containsKey(fieldName)) {
                    Field curField = configKeyMap.get(fieldName);
                    Object curOb = curField.get(this);
                    if (curOb instanceof Integer) {
                        int value = ((Number) curOb).intValue();
                        curField.set(this, value);
                    } else if (curOb instanceof Float) {
                        float value = ((Number) curOb).floatValue();
                        curField.set(this, value);
                    }

                }
            }
        }
        CLib instance = BASSNative.getInstance();
        instance.setLength(DEFAULT_PACKET_LENGTH);
        instance.setVolume(DEFAULT_AUDIO_VOLUME);
        instance.set_record_volume_limit(DEFAULT_AUDIO_VOLUME_LIMIT);
        instance.init_device(-1);
        instance.startThread();
        startNetworkClientThread();
        PointerFree.freeClientAllPointer();
        if (Minecraft.getInstance().isSingleplayer()) {
            MicServerService.getInstance().startNetworkServerThread();
            MicServerService.getInstance().setSingleGame(true);
        }
        VoiceLogger.info("The real time voice system has loaded: 客户端语音已经启动");
    }

    private Object restoreValue(Object dest, String key, Map<String, Object> src) {
        if (src.containsKey(key)) {
            dest = src.get(key);
        }
        return dest;
    }

    public void startNetworkClientThread() {
        if (soundPacketHandlerClientThread == null) {
            soundPacketHandlerClientThread = new SoundPacketHandlerClientThread();
        }
        soundPacketHandlerClientThread.startThread(Minecraft.getInstance().player.getDisplayName().getString());
        TimerUtils.startTimer(0, 10, soundPacketHandlerClientThread);
    }


    public void stopSoundThread(String quitPlayerId) {
        TimerUtils.stopTimer();
        audioBuffer.clear();
        serverMicDataPacketHandlersClineSide.clear();
        BASSNative.INSTANCE.stop_record();
        Map<String, Object> serializableMap = new HashMap<>();
        for (String fieldName : configKeyMap.keySet()) {
            Field field = configKeyMap.get(fieldName);
            try {
                serializableMap.put(fieldName, field.get(this));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        config.saveVoiceSettings(serializableMap);
        VoiceLogger.info("The voice settings has saved: 声音配置文件已保存", (Object) null);
        VoiceLogger.info("record channel close: 关闭录制", (Object) null);
    }

    public boolean isMute() {
        return mute;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isActive() {
        return isActive;
    }

    public class BindData {
        public int readSize;
        public byte[] data;

        public BindData(int readSize, byte[] data) {
            this.readSize = readSize;
            this.data = data;
        }
    }
}
