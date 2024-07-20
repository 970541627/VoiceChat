package com.voice.network;

import com.voice.command.CommandConst;
import com.voice.recording.SoundDataCenter;
import com.voice.snative.BASSNative;
import com.voice.ui.Hud;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.network.CustomPayloadEvent;

import javax.annotation.Nullable;
import java.nio.charset.Charset;

public class CommandPacketHandler<T extends Object> {
    public String commandLine;
    public T args;

    public CommandPacketHandler(String commandLine, @Nullable T args) {
        this.commandLine = commandLine;
        this.args = args;
    }

    public CommandPacketHandler(FriendlyByteBuf friendlyByteBuf) {
        this.commandLine = new String(friendlyByteBuf.readByteArray(), Charset.defaultCharset());
        String type = new String(friendlyByteBuf.readByteArray(), Charset.defaultCharset());
        if (type.equals(CommandConst.INTEGER_TYPE)) {
            Integer integer = friendlyByteBuf.readInt();
            this.args = (T) integer;
        } else if (type.equals(CommandConst.FLOAT_TYPE)) {
            Float floats = friendlyByteBuf.readFloat();
            this.args = (T) floats;
        }

    }


    public static void encode(CommandPacketHandler commandPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByteArray(commandPacketHandler.commandLine.getBytes());
        if (commandPacketHandler.args != null) {
            if (commandPacketHandler.args instanceof Float) {
                friendlyByteBuf.writeByteArray(CommandConst.FLOAT_TYPE.getBytes());
                friendlyByteBuf.writeFloat((Float) commandPacketHandler.args);

            } else if (commandPacketHandler.args instanceof Integer) {
                friendlyByteBuf.writeByteArray(CommandConst.INTEGER_TYPE.getBytes());
                friendlyByteBuf.writeInt((Integer) commandPacketHandler.args);

            }
        } else {
            friendlyByteBuf.writeByteArray(CommandConst.INTEGER_TYPE.getBytes());
            friendlyByteBuf.writeInt(-1);

        }

    }

    public static void handle(CommandPacketHandler commandPacketHandler, CustomPayloadEvent.Context context) {
        if (context.isClientSide()) {
            String command = commandPacketHandler.commandLine;
            if (command.equals(CommandConst.SET_MUTE)) {
                SoundDataCenter.stopRecord();
                SoundDataCenter.mute = true;
            } else if (command.equals(CommandConst.SET_NO_MUTE)) {
                SoundDataCenter.startRecord();
                SoundDataCenter.mute = false;
            } else if (command.equals(CommandConst.SET_PACKET_LENGTH)) {
                SoundDataCenter.DEFAULT_PACKET_LENGTH = (Integer) commandPacketHandler.args;
                BASSNative.INSTANCE.setLength((Integer) commandPacketHandler.args);
            } else if (command.equals(CommandConst.SET_VOLUME)) {
                SoundDataCenter.DEFAULT_AUDIO_VOLUME = (Float) commandPacketHandler.args;
                BASSNative.getInstance().setVolume((Float) commandPacketHandler.args);
            } else if (command.equals(CommandConst.SET_VOLUME_LIMIT)) {
                SoundDataCenter.DEFAULT_AUDIO_VOLUME_LIMIT = (Float) commandPacketHandler.args;
                BASSNative.INSTANCE.set_record_volume_limit((Float) commandPacketHandler.args);
            } else if (command.equals(CommandConst.GET_GET_AVG_LEVEL)) {
                float avg_level = BASSNative.getInstance().get_avg_level();
                avg_level = SoundDataCenter.getFloatByTwoDecimalPlaces(avg_level);
                float range = 0.05f;
                String message = new StringBuilder("你的平均音量是：").append(avg_level).append(",建议修改声音阈值的范围为")
                        .append(SoundDataCenter.getFloatByTwoDecimalPlaces(avg_level - range))
                        .append("~")
                        .append(SoundDataCenter.getFloatByTwoDecimalPlaces(avg_level + range))
                        .append(",使用指令 /siro th <阈值>  例如：/siro th ")
                        .append(SoundDataCenter.getFloatByTwoDecimalPlaces(avg_level + range)).toString();
                Minecraft.getInstance().player.sendSystemMessage(Component.literal(message));
            } else if (command.equals(CommandConst.SET_MIC_POS_X)) {
                Hud.xRate= (int) commandPacketHandler.args;
            } else if (command.equals(CommandConst.SET_MIC_POS_Y)) {
                Hud.yRate= (int) commandPacketHandler.args;
            }else if(command.equals(CommandConst.GET_HELP)){
                Minecraft.getInstance().player.sendSystemMessage(CommandConst.helpMessage);
            }
        }
    }
}
