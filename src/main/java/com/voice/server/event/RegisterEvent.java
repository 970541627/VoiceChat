package com.voice.server.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.voice.client.network.GroupPacketHandler;
import com.voice.client.network.NetworkSender;
import com.voice.client.network.PlayerLogPacketHandler;
import com.voice.client.network.PrivateChatPacketHandler;
import com.voice.config.DistanceConfig;
import com.voice.config.GroupConfig;
import com.voice.server.privatechat.PrivateChat;
import com.voice.server.command.CommandConst;
import com.voice.server.command.CommandExecutor;
import com.voice.server.group.Group;
import com.voice.server.group.GroupChat;
import com.voice.server.service.GroupServerService;
import com.voice.server.service.MicServerService;
import com.voice.util.ComponentUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber
public class RegisterEvent {

    private static GroupServerService groupService = GroupServerService.getInstance();
    private static MicServerService serverService = MicServerService.getInstance();

    @SubscribeEvent
    public static void onServerStaring(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        Map<String, Pair<String, Pair<String, ArgumentType>>> commandsInfo = CommandConst.commandTypeMap;
        for (String command : commandsInfo.keySet()) {
            Pair<String, Pair<String, ArgumentType>> argsPair = commandsInfo.get(command);
            Pair<String, ArgumentType> argInfo = argsPair.getRight();
            if (argInfo == null) {
                createCommand(command, dispatcher);
            } else {
                createCommand(command, dispatcher, argInfo.getLeft(), argInfo.getRight());
            }
        }
    }

    @SubscribeEvent
    public static void chatMessage(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (event.getMessage().getString().equalsIgnoreCase("y") && groupService.getGroupRequestSize(player) != 0) {
            groupService.respReq(player, event.getMessage().getString());
            event.setCanceled(true);
        }
    }

    public static void createCommand(String commandLine, CommandDispatcher<CommandSourceStack> dispatcher) {
        createCommand(commandLine, dispatcher, null, null);
    }

    public static void createCommand(String commandLine, CommandDispatcher<CommandSourceStack> dispatcher, @Nullable String argsName, @Nullable ArgumentType<? extends Object> args) {
        String[] lines = commandLine.split(" ");
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal(lines[0]);
        LiteralArgumentBuilder<CommandSourceStack>[] childCommands = new LiteralArgumentBuilder[lines.length];
        childCommands[0] = literal;
        for (int i = 1; i < lines.length; i++) {
            childCommands[i] = Commands.literal(lines[i]);
        }
        LiteralArgumentBuilder<CommandSourceStack> childCommand = childCommands[lines.length - 1];
        childCommand = childCommand.requires((command) -> command.hasPermission(0));
        if (args != null) {
            childCommand = childCommand.then(Commands.argument(argsName, args).executes(CommandExecutor.instance));
        } else {
            childCommand = childCommand.executes(CommandExecutor.instance);
        }
        childCommands[lines.length - 1] = childCommand;
        for (int i = lines.length - 1; i >= 1; i--) {
            childCommands[i - 1] = childCommands[i - 1].then(childCommands[i]);
        }
        dispatcher.register(childCommands[0]);
    }


    @SubscribeEvent
    public static void playerLoggedInEvent(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            Map<String, Object> stringObjectMap = DistanceConfig.getConfigInstance().readConfigSettings(player.getDisplayName().getString(), ".set");
            Object distanceObject = stringObjectMap.get(player.getDisplayName().getString());
            double distance = 100.00;
            if (distanceObject instanceof Integer) {
                distance = ((Integer) distanceObject).doubleValue();
            } else if (distanceObject instanceof Double) {
                distance = (Double) distanceObject;
            }
            serverService.setDistanceLimit(player.getDisplayName().getString(), distance);
            serverService.addServerPlayer(player);
            GroupConfig.getConfigInstance().readGroupData(player);
            NetworkSender.sendPacketToLogPlayer(new PlayerLogPacketHandler(0, player.getDisplayName().getString()), player);
        }
    }

    @SubscribeEvent
    public static void playerLoggedOutEvent(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            serverService.removeDistanceLimit(player.getDisplayName().getString());
            GroupChat groupChat = GroupChat.getInstance();
            PrivateChat privateChat = PrivateChat.getInstance();
            if (groupChat.isInGroup(player)) {
                List<String> originalPlayerList = groupChat.getServerPlayersName(player);
                Group group = groupChat.removePlayer(player, false);
                originalPlayerList.remove(player.getDisplayName().getString());
                ServerPlayer[] players = group.groupPlayerList();
                NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(originalPlayerList), players);
                for (int i = 0; i < players.length; i++) {
                    players[i].sendSystemMessage(ComponentUtils.playerInfo(player.getDisplayName().getString(), "已下线，离开了群聊频道"));
                }
                GroupConfig.getConfigInstance().saveGroupData(player, group.getGroupId());
            } else if (privateChat.getPrivateChatPlayer(player) != null) {
                ServerPlayer chatPlayer = privateChat.getPrivateChatPlayer(player);
                privateChat.cancelPrivateChat(player);
                chatPlayer.sendSystemMessage(ComponentUtils.playerInfo(player.getDisplayName().getString(), "已下线，离开了私聊频道"));
                NetworkSender.sendGroupPlayersMembersToClient(new PrivateChatPacketHandler(new ArrayList<>()), new ServerPlayer[]{chatPlayer});
            }
            serverService.removeServerPlayer(player);
            //serverService.clearServerPacketHandlersServerSide();
        }
    }

}
