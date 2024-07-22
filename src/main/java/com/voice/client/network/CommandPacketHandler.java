package com.voice.client.network;

import com.voice.client.service.MicClientService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.event.network.CustomPayloadEvent;

import javax.annotation.Nullable;
import java.nio.charset.Charset;

public class CommandPacketHandler {
    public String commandLine;
    public Float args;

    public CommandPacketHandler(String commandLine, @Nullable Float args) {
        this.commandLine = commandLine;
        this.args = args;
    }

    public CommandPacketHandler(FriendlyByteBuf friendlyByteBuf) {
        this.commandLine = new String(friendlyByteBuf.readByteArray(), Charset.defaultCharset());
        this.args = friendlyByteBuf.readFloat();
    }


    public static void encode(CommandPacketHandler commandPacketHandler, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByteArray(commandPacketHandler.commandLine.getBytes());
        if (commandPacketHandler.args==null) {
            commandPacketHandler.args=0.0f;
        }
        friendlyByteBuf.writeFloat(commandPacketHandler.args);
    }

    public static void handle(CommandPacketHandler commandPacketHandler, CustomPayloadEvent.Context context) {
        if (context.isClientSide()) {
            MicClientService micClientService = MicClientService.getInstance();
            micClientService.invokeMethod(commandPacketHandler.commandLine, commandPacketHandler.args);
        }
    }
}
