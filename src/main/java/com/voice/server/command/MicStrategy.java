package com.voice.server.command;

import com.voice.client.network.CommandPacketHandler;
import com.voice.client.network.NetworkSender;
import com.voice.server.service.MicServerService;
import com.voice.util.ComponentUtils;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public class MicStrategy implements CommandStrategy {
    private MicServerService service=MicServerService.getInstance();

    @Override
    public void executor(ServerPlayer player, String commandWithNoArg, @Nullable Object arg) {
        if (arg != null) {
            if (commandWithNoArg.equals(CommandConst.SET_DISTANCE)) {
                double distance = ((Number) arg).doubleValue();
                service.setDistanceLimit(player.getDisplayName().getString(), ((Number) arg).doubleValue());
                player.sendSystemMessage(ComponentUtils.defaultMessage("成功调整收听距离为",String.valueOf(distance),""));
            } else {
                CommandPacketHandler commandPacket = new CommandPacketHandler(commandWithNoArg, (Float) arg);
                NetworkSender.sendExecuteCommandToClient(commandPacket, player);
            }
        }else{
            CommandPacketHandler commandPacket = new CommandPacketHandler(commandWithNoArg, null);
            NetworkSender.sendExecuteCommandToClient(commandPacket, player);
        }
    }


    @Override
    public String getType() {
        return CommandConst.MIC_TYPE;
    }
}
