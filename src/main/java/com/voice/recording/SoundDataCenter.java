package com.voice.recording;

import com.voice.config.Config;
import com.voice.log.VoiceLogger;
import com.voice.network.ClientPacketHandler;
import com.voice.network.PointerFree;
import com.voice.network.ServerPacketHandler;
import com.voice.players.ServerPlayerManager;
import com.voice.privatechat.RequestSolveThread;
import com.voice.snative.BASSNative;
import com.voice.snative.CLib;
import com.voice.ui.Hud;
import com.mojang.brigadier.context.CommandContext;
import de.maxhenkel.rnnoise4j.Denoiser;
import de.maxhenkel.rnnoise4j.UnknownPlatformException;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class SoundDataCenter {
    public static volatile ConcurrentLinkedQueue<BindData> audioBuffer = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedQueue<ServerPacketHandler> serverPacketHandlersClineSide = new ConcurrentLinkedQueue<>();
    public static volatile ConcurrentLinkedQueue<ClientPacketHandler> serverPacketHandlersServerSide = new ConcurrentLinkedQueue<>();
    private static ScheduledThreadPoolExecutor s = new ScheduledThreadPoolExecutor(4);
    public static SoundPacketHandlerClientThread soundPacketHandlerClientThread;
    public static SoundPacketHandlerServerThread soundPacketHandlerServerThread;
    public static RequestSolveThread requestSolveThread;
    public static DecimalFormat demical = new DecimalFormat("0.00");
    private static Config config = new Config();

    public static boolean isQuitGame = false;
    public static volatile boolean isActive = false;
    public static Denoiser denoiser;
    public static String playerId;

    public static boolean stop = false;
    public static boolean isSingleGame = false;
    public static boolean mute = false;


    public static int DEVICE = -1;
    public static int DEFAULT_PACKET_LENGTH = 480 * 8;
    public static float DEFAULT_AUDIO_VOLUME = 1.0f;
    public static float DEFAULT_AUDIO_VOLUME_LIMIT = 0.20f;


    public static float getFloatByTwoDecimalPlaces(float orignal) {
        return Float.parseFloat(demical.format(orignal));
    }

    public static short[] bytesToShort(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }


    public static void addRespRequestTask(ServerPlayer dest, CommandContext<CommandSourceStack> requestCommand) {
        requestSolveThread.addTask(dest, requestCommand);
    }

    public static void addGroupRequestTask(ServerPlayer dest, CommandContext<CommandSourceStack> requestCommand, ServerPlayer joiner) {
        addRespRequestTask(dest, requestCommand);
        requestSolveThread.addGroupRequestQueue(dest, joiner);
    }

    public static int getGroupRequestSize(ServerPlayer sender){
        return requestSolveThread.getReqSize(sender);
    }

    public static void respReq(ServerPlayer sender, String resp) {
        requestSolveThread.respTask(sender, resp, null);
    }

    public static Denoiser getSingleDenoiser() {
        try {
            if (denoiser == null) {
                denoiser = new Denoiser();
            }
            return denoiser;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnknownPlatformException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] shortToBytes(short[] data) {
        byte[] resultData = new byte[2 * data.length];
        int iter = 0;
        for (short sample : data) {
            resultData[iter++] = (byte) (sample & 0xff);     //低位存储，0xff是掩码操作
            resultData[iter++] = (byte) ((sample >> 8) & 0xff); //高位存储
        }
        return resultData;
    }

    public static void setDevice(int newDevice) {
        BASSNative.INSTANCE.init_device(DEVICE);
        DEVICE = newDevice;
    }

    public static void startRecord() {
        mute = false;
        BASSNative.INSTANCE.set_mute(false);
    }

    public static void stopRecord() {
        mute = true;
        BASSNative.INSTANCE.set_mute(true);
    }

    public static void startSoundThread() {
        if (s.isShutdown()) {
            s = new ScheduledThreadPoolExecutor(4);
        }
        if (Minecraft.getInstance().isSingleplayer()) {
            startNetworkServerThread();
            ServerPlayerManager.setServerPlayer();
            isSingleGame = true;
        } else {
            isSingleGame = false;
        }

        Map<String, Object> serializableMap = config.readConfigSettings();
        if (serializableMap != null) {
            DEVICE = (int) restoreValue(DEVICE, "device", serializableMap);
            DEFAULT_PACKET_LENGTH = (int) restoreValue(DEFAULT_PACKET_LENGTH, "packetLength", serializableMap);
            DEFAULT_AUDIO_VOLUME = (float) restoreValue(DEFAULT_AUDIO_VOLUME, "volume", serializableMap);
            DEFAULT_AUDIO_VOLUME_LIMIT = (float) restoreValue(DEFAULT_AUDIO_VOLUME_LIMIT, "volumeLimit", serializableMap);
            Hud.xRate = (int) restoreValue(Hud.xRate, "posx", serializableMap);
            Hud.yRate = (int) restoreValue(Hud.yRate, "posy", serializableMap);
        }
        CLib instance = BASSNative.getInstance();
        instance.setLength(DEFAULT_PACKET_LENGTH);
        instance.init_device(-1);
        instance.setVolume(DEFAULT_AUDIO_VOLUME);
        instance.set_record_volume_limit(DEFAULT_AUDIO_VOLUME_LIMIT);
        SoundDataCenter.stop = false;
        instance.startThread();
        SoundDataCenter.startNetworkClientThread();
        PointerFree.freeClientAllPointer();
        VoiceLogger.info("The real time voice system has loaded: 客户端语音已经启动");
    }

    private static Object restoreValue(Object dest, String key, Map<String, Object> src) {
        if (src.containsKey(key)) {
            dest = src.get(key);
        }
        return dest;
    }

    public static void startNetworkClientThread() {
        if (soundPacketHandlerClientThread == null) {
            soundPacketHandlerClientThread = new SoundPacketHandlerClientThread();
        }

        soundPacketHandlerClientThread.startThread();
        s.scheduleAtFixedRate(soundPacketHandlerClientThread, 1000, 20, TimeUnit.MILLISECONDS);

    }

    public static void startNetworkServerThread() {
        if (soundPacketHandlerServerThread == null) {
            soundPacketHandlerServerThread = new SoundPacketHandlerServerThread();
        }
        if (requestSolveThread == null) {
            requestSolveThread = new RequestSolveThread();
        }
        s.scheduleAtFixedRate(soundPacketHandlerServerThread, 0, 10, TimeUnit.MILLISECONDS);
        s.scheduleAtFixedRate(requestSolveThread, 0, 10, TimeUnit.MILLISECONDS);
    }

    public static void stopSoundThread(String quitPlayerId) {
        if (playerId != null && playerId.equals(quitPlayerId)) {
            SoundDataCenter.stop = true;
            isQuitGame = true;
            s.shutdownNow();
            SoundDataCenter.audioBuffer.clear();
            SoundDataCenter.serverPacketHandlersClineSide.clear();
            SoundDataCenter.serverPacketHandlersServerSide.clear();
            BASSNative.INSTANCE.stop_record();
            VoiceLogger.info("record channel close: 关闭录制", (Object) null);

            Map<String, Object> serializableMap = new HashMap<>();
            serializableMap.put("device", DEVICE);
            serializableMap.put("packetLength", DEFAULT_PACKET_LENGTH);
            serializableMap.put("volume", DEFAULT_AUDIO_VOLUME);
            serializableMap.put("volumeLimit", DEFAULT_AUDIO_VOLUME_LIMIT);
            serializableMap.put("posx", Hud.xRate);
            serializableMap.put("posy", Hud.yRate);
            config.saveVoiceSettings(serializableMap);
            VoiceLogger.info("The voice settings has saved: 声音配置文件已保存", (Object) null);
        }

    }

    public static byte[] uncompress(byte[] inputByte) throws IOException {
        int len = 0;
        Inflater infl = new Inflater();
        infl.setInput(inputByte);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outByte = new byte[1024];
        try {
            while (!infl.finished()) {
                // 解压缩并将解压缩后的内容输出到字节输出流bos中
                len = infl.inflate(outByte);
                if (len == 0) {
                    break;
                }
                bos.write(outByte, 0, len);
            }
            infl.end();
        } catch (Exception e) {
            //
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }


    /**
     * 压缩.
     *
     * @param inputByte 待压缩的字节数组
     * @return 压缩后的数据
     * @throws IOException
     */
    public static byte[] compress(byte[] inputByte) throws IOException {
        int len = 0;
        Deflater defl = new Deflater(Deflater.BEST_COMPRESSION);
        defl.setInput(inputByte);
        defl.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] outputByte = new byte[1024];
        try {
            while (!defl.finished()) {
                // 压缩并将压缩后的内容输出到字节输出流bos中
                len = defl.deflate(outputByte);
                bos.write(outputByte, 0, len);
            }
            defl.end();
        } finally {
            bos.close();
        }
        return bos.toByteArray();
    }


    public static class BindData {
        public int readSize;
        public byte[] data;

        public BindData(int readSize, byte[] data) {
            this.readSize = readSize;
            this.data = data;
        }
    }
}
