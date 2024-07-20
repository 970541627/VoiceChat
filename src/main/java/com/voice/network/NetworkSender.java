package com.voice.network;

import com.voice.VoiceMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;

public class NetworkSender {
    private static SimpleChannel channel;
    private static SimpleChannel channelS;
    private static SimpleChannel channelPlayerInAndOut;
    private static SimpleChannel channelCommand;
    private static SimpleChannel channelGroup;
    private static SimpleChannel channelPrivateChat;

    public static SimpleChannel createChannel(String id) {
        return ChannelBuilder.named(
                new ResourceLocation(VoiceMod.MODID, id)).serverAcceptedVersions(((status, version) -> true))
                .clientAcceptedVersions((status, version) -> true).networkProtocolVersion(1).simpleChannel();
    }

    public static void registerGroup() {
        channelGroup = createChannel("group");
        channelGroup.messageBuilder(GroupPacketHandler.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(GroupPacketHandler::encode)
                .decoder(GroupPacketHandler::new)
                .consumerMainThread(GroupPacketHandler::handle)
                .add();
    }

    public static void registerS() {
        channel = createChannel("main");
        channel.messageBuilder(ServerPacketHandler.class, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ServerPacketHandler::encode)
                .decoder(ServerPacketHandler::new)
                .consumerMainThread(ServerPacketHandler::handle)
                .add();

    }

    public static void registerC() {
        channelS = createChannel("server");
        channelS.messageBuilder(ClientPacketHandler.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(ClientPacketHandler::encode)
                .decoder(ClientPacketHandler::new)
                .consumerMainThread(ClientPacketHandler::handle)
                .add();
    }

    public static void registerCommandChannel() {
        channelCommand = createChannel("command");
        channelCommand.messageBuilder(CommandPacketHandler.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CommandPacketHandler::encode)
                .decoder(CommandPacketHandler::new)
                .consumerMainThread(CommandPacketHandler::handle)
                .add();
    }

    public static void registerPlayerInAndOut() {
        channelPlayerInAndOut = createChannel("player");
        channelPlayerInAndOut.messageBuilder(PlayerLogPacketHandler.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayerLogPacketHandler::encode)
                .decoder(PlayerLogPacketHandler::new)
                .consumerMainThread(PlayerLogPacketHandler::handle)
                .add();
    }

    public static void registerPrivateChat() {
        channelPrivateChat = createChannel("secret");
        channelPrivateChat.messageBuilder(PrivateChatPacketHandler.class, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PrivateChatPacketHandler::encode)
                .decoder(PrivateChatPacketHandler::new)
                .consumerMainThread(PrivateChatPacketHandler::handle)
                .add();
    }

    public static void registerAll() {
        NetworkSender.registerS();
        NetworkSender.registerC();
        NetworkSender.registerPlayerInAndOut();
        NetworkSender.registerCommandChannel();
        NetworkSender.registerGroup();
        NetworkSender.registerPrivateChat();
    }

    public static void sendPrivateChatPacketToClient(Object packet, ServerPlayer[] target) {
        for (int i = 0; i < target.length; i++) {
            channelPrivateChat.send(packet, PacketDistributor.PLAYER.with(target[i]));
        }
    }

    public static void sendPacketToServer(Object packet) {
        channel.send(packet, PacketDistributor.SERVER.noArg());
    }

    public static void sendPacketToPlayers(Object packet, ServerPlayer player) {
        channelS.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendPacketToLogPlayer(Object packet, ServerPlayer player) {
        channelPlayerInAndOut.send(packet, PacketDistributor.PLAYER.with(player));
    }

    public static void sendExecuteCommandToClient(Object packet, ServerPlayer sender) {
        channelCommand.send(packet, PacketDistributor.PLAYER.with(sender));
    }

    public static void sendGroupPlayersMembersToClient(Object packet, ServerPlayer[] player) {
        for (int i = 0; i < player.length; i++) {
            channelGroup.send(packet, PacketDistributor.PLAYER.with(player[i]));
        }
    }
}
