package com.voice.thread;

import com.voice.client.service.MicClientService;
import com.voice.client.network.NetworkSender;
import com.voice.server.network.ServerMicDataPacketHandler;
import com.voice.basslib.BASSNative;
import com.voice.basslib.StreamAudio;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class SoundPacketHandlerClientThread implements Runnable {
    public volatile boolean hasStop = false;
    private MicClientService micClientService = MicClientService.getInstance();
    private String playerId;
    public void startThread(String playerId) {
        hasStop = false;
        this.playerId=playerId;
    }

    @Override
    public void run() {
        StreamAudio.ByValue data = BASSNative.INSTANCE.poll_input_data();
        if (!micClientService.isMute() && data.y > 0) {
            micClientService.setActive(true);
            NetworkSender.sendPacketToServer(new ServerMicDataPacketHandler(data, playerId));
        } else {
            micClientService.setActive(false);
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (!hasStop && connection != null) {
            if (!hasStop && connection.getLevel() == null) {
                MicClientService.getInstance().stopSoundThread(Minecraft.getInstance().player.getDisplayName().getString());
                hasStop = true;
            }
        }
    }
}
