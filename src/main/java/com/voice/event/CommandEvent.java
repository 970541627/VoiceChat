package com.voice.event;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.voice.command.CommandConst;
import com.voice.command.CommandExecutor;
import com.voice.config.DistanceConfig;
import com.voice.config.GroupConfig;
import com.voice.group.Group;
import com.voice.group.GroupChat;
import com.voice.network.GroupPacketHandler;
import com.voice.network.NetworkSender;
import com.voice.network.PlayerLogPacketHandler;
import com.voice.network.PrivateChatPacketHandler;
import com.voice.players.ServerPlayerManager;
import com.voice.privatechat.PrivateChat;
import com.voice.recording.SoundDataCenter;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

@Mod.EventBusSubscriber
public class CommandEvent {
    private static LinkedBlockingQueue<ServerPlayer> serverPlayerList = ServerPlayerManager.getServerPlayerList();

    @SubscribeEvent
    public static void onServerStaring(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        createCommand(CommandConst.SET_NO_MUTE, dispatcher);
        createCommand(CommandConst.SET_MUTE, dispatcher);
        createCommand(CommandConst.GET_GET_AVG_LEVEL, dispatcher);
        createCommand(CommandConst.GET_HELP, dispatcher);
        createCommand(CommandConst.GET_GROUP_ID, dispatcher);
        createCommand(CommandConst.QUIT_GROUP, dispatcher);
        createCommand(CommandConst.SET_MIC_POS_X, dispatcher, "rate", IntegerArgumentType.integer());
        createCommand(CommandConst.SET_MIC_POS_Y, dispatcher, "rate", IntegerArgumentType.integer());
        createCommand(CommandConst.SET_PACKET_LENGTH, dispatcher, "len", IntegerArgumentType.integer());
        createCommand(CommandConst.SET_VOLUME_LIMIT, dispatcher, "rate", FloatArgumentType.floatArg());
        createCommand(CommandConst.SET_VOLUME, dispatcher, "rate", FloatArgumentType.floatArg());
        createCommand(CommandConst.SET_DISTANCE, dispatcher, "dis", IntegerArgumentType.integer(0));
        createCommand(CommandConst.CREATE_GROUP, dispatcher, "max", IntegerArgumentType.integer());
        createCommand(CommandConst.JOIN_GROUP, dispatcher, "id", StringArgumentType.string());

        createCommand(CommandConst.SET_CREATE_TALK, dispatcher, "player", EntityArgument.player());
        createCommand(CommandConst.KICK_PLAYER_FROM_GROUP, dispatcher, "player", EntityArgument.player());
        createCommand(CommandConst.INVITE_PLAYER_FROM_GROUP, dispatcher, "player", EntityArgument.player());
        createCommand(CommandConst.SET_CANCEL_TALK, dispatcher);


    }

    @SubscribeEvent
    public static void chatMessage(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (event.getMessage().getString().equalsIgnoreCase("y") && SoundDataCenter.getGroupRequestSize(player) != 0) {
            SoundDataCenter.respReq(player, event.getMessage().getString());
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
            ServerPlayerManager.setDistanceLimit(player.getDisplayName().getString(), distance);
            GroupConfig.getConfigInstance().readGroupData(player);
            serverPlayerList.add(player);
            NetworkSender.sendPacketToLogPlayer(new PlayerLogPacketHandler(0, player.getDisplayName().getString()), player);
        }
    }

    @SubscribeEvent
    public static void playerLoggedOutEvent(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            if (serverPlayerList.contains(player)) {
                ServerPlayerManager.removeDistanceLimit(player.getDisplayName().getString());
                GroupChat groupChat = GroupChat.getInstance();
                PrivateChat privateChat = PrivateChat.getInstance();
                if (groupChat.isInGroup(player)) {
                    List<String> originalPlayerList = groupChat.getServerPlayersName(player);
                    Group group = groupChat.removePlayer(player,false);
                    originalPlayerList.remove(player.getDisplayName().getString());
                    ServerPlayer[] players = group.groupPlayerList();
                    NetworkSender.sendGroupPlayersMembersToClient(new GroupPacketHandler(originalPlayerList), players);
                    for (int i = 0; i < players.length; i++) {
                        players[i].sendSystemMessage(Component.literal("玩家" + player.getDisplayName().getString() + "已下线，离开了群聊频道").withColor(0xFFFF0000));
                    }
                    GroupConfig.getConfigInstance().saveGroupData(player, group.getGroupId());
                } else if (privateChat.getPrivateChatPlayer(player) != null) {
                    ServerPlayer chatPlayer = privateChat.getPrivateChatPlayer(player);
                    privateChat.cancelPrivateChat(player);
                    chatPlayer.sendSystemMessage(Component.literal("玩家" + player.getDisplayName().getString() + "已下线，离开了私聊频道").withColor(0xFFFF0000));
                    NetworkSender.sendGroupPlayersMembersToClient(new PrivateChatPacketHandler(new ArrayList<>()), new ServerPlayer[]{chatPlayer});
                }
                serverPlayerList.remove(player);
                SoundDataCenter.serverPacketHandlersServerSide.clear();
            }
        }
    }

}
