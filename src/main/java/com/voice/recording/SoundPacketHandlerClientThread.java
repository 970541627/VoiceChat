package com.voice.recording;

import com.voice.network.NetworkSender;
import com.voice.network.ServerPacketHandler;
import com.voice.snative.BASSNative;
import com.voice.snative.StreamAudio;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;

public class SoundPacketHandlerClientThread implements Runnable {
    public volatile boolean hasStop = false;

    public void startThread() {
        hasStop = false;
    }

    @Override
    public void run() {
        StreamAudio.ByValue data = BASSNative.INSTANCE.poll_input_data();
        if (!SoundDataCenter.mute && data.y > 0) {
            SoundDataCenter.isActive=true;
            NetworkSender.sendPacketToServer(new ServerPacketHandler(data, SoundDataCenter.playerId));
        }else{
            SoundDataCenter.isActive=false;
        }
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        if (!hasStop && connection != null) {
            if (!hasStop && connection.getLevel() == null) {
                SoundDataCenter.stopSoundThread(Minecraft.getInstance().player.getDisplayName().getString());
                hasStop = true;
            }
        }
    }
}
