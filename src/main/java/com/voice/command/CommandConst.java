package com.voice.command;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class CommandConst {
    public static final String SET_VOLUME_LIMIT = "siro th";
    public static final String SET_VOLUME = "siro v";
    public static final String SET_PACKET_LENGTH = "siro len";
    public static final String SET_MUTE = "siro mute";
    public static final String SET_NO_MUTE = "siro nomute";
    public static final String GET_GET_AVG_LEVEL = "siro avg";
    public static final String GET_HELP = "siro help";
    public static final String SET_MIC_POS_X = "siro x";
    public static final String SET_MIC_POS_Y = "siro y";
    private static MutableComponent[] groupJoinRequestComponent = new MutableComponent[5];
    private static MutableComponent[] talkJoinRequestComponent = new MutableComponent[5];

    static {
        groupJoinRequestComponent[0] = Component.literal("玩家").withColor(0xFFFF0000);
        groupJoinRequestComponent[1] = null;
        groupJoinRequestComponent[2] = Component.literal("请求加入你的群组：").withColor(0xFFFF0000);
        groupJoinRequestComponent[3] = Component.literal("输入y").withColor(0xFFFFFF00);
        groupJoinRequestComponent[4] = Component.literal("同意玩家进入，5秒后该请求自动拒绝\n").withColor(0xFFFF0000);

        talkJoinRequestComponent[0] = Component.literal("玩家").withColor(0xFFFF0000);
        talkJoinRequestComponent[1] = null;
        talkJoinRequestComponent[2] = Component.literal("请求与你私聊：").withColor(0xFFFF0000);
        talkJoinRequestComponent[3] = Component.literal("输入y").withColor(0xFFFFFF00);
        talkJoinRequestComponent[4] = Component.literal("同意玩家进入，5秒后该请求自动拒绝\n").withColor(0xFFFF0000);
    }


    public static final Component groupJoinRequest(String playerName) {
        groupJoinRequestComponent[1] = Component.literal(playerName).withColor(0xFFFFFF00);
        MutableComponent cur = groupJoinRequestComponent[0];
        for (int i = 1; i < groupJoinRequestComponent.length; i++) {
            cur = cur.append(groupJoinRequestComponent[i]);
        }
        return cur;
    }

    public static final Component talkJoinRequest(String playerName) {
        talkJoinRequestComponent[1] = Component.literal(playerName).withColor(0xFFFFFF00);
        MutableComponent cur = talkJoinRequestComponent[0];
        for (int i = 1; i < talkJoinRequestComponent.length; i++) {
            cur = cur.append(talkJoinRequestComponent[i]);
        }
        return cur;
    }


    public static final String GET_VERSION = "1.0";
    public static final String SET_DISTANCE = "siro dis";

    public static final String SET_CREATE_TALK = "siro talk create";
    public static final String SET_CANCEL_TALK = "siro talk cancel";
    /**
     * group 相关参数
     */
    public static final String CREATE_GROUP = "siro group create";
    public static final String GET_GROUP_ID = "siro group id";
    public static final String JOIN_GROUP = "siro group join ";
    public static final String QUIT_GROUP = "siro group quit";
    public static final String KICK_PLAYER_FROM_GROUP = "siro group kick";
    public static final String INVITE_PLAYER_FROM_GROUP = "siro group invite";


    public static final String FLOAT_TYPE = Float.class.getTypeName();
    public static final String INTEGER_TYPE = Integer.class.getTypeName();

    public static Component compentUtil(String msg, int color) {
        return Component.literal(msg).withColor(color);
    }

    public static final Component helpMessage = Component.literal("指令指南: \n").withColor(0xFFFF0000)
            .append(compentUtil("- " + SET_MUTE, 0xFFFF0000)).append(compentUtil("： 闭麦\n", 0xFFFFFF00))
            .append(compentUtil("- " + SET_NO_MUTE, 0xFFFF0000)).append(compentUtil("：开麦\n", 0xFFFFFF00))
            .append(compentUtil("- " + SET_PACKET_LENGTH, 0xFFFF0000)).append(compentUtil("：设置发送语音包长度(谨慎设置，请输入960的倍数)\n", 0xFFFFFF00))
            .append(compentUtil("举例：/" + SET_PACKET_LENGTH + " 960\n", 0xFFFF7F00))
            .append(compentUtil("- " + SET_VOLUME, 0xFFFF0000)).append(compentUtil("：设置语音播放音量（范围0~10）\n", 0xFFFFFF00))
            .append(compentUtil("举例：/" + SET_VOLUME + " 1\n", 0xFFFF7F00))
            .append(compentUtil("- " + SET_VOLUME_LIMIT, 0xFFFF0000)).append(compentUtil("：设置麦克风阈值（高于这个阈值才说话,范围0.00~1.00）\n", 0xFFFFFF00))
            .append(compentUtil("举例：/" + SET_VOLUME_LIMIT + " 0.56 \n", 0xFFFF7F00))
            .append(compentUtil("- " + GET_GET_AVG_LEVEL, 0xFFFF0000)).append(compentUtil("：获取你麦克风平均音量，可以根据平均值设置麦克风阈值\n", 0xFFFFFF00))
            .append(compentUtil("- " + SET_DISTANCE + " <方块数>", 0xFFFF0000)).append(compentUtil("：调整收听范围\n", 0xFFFFFF00))
            .append(compentUtil("举例：/" + SET_DISTANCE + " 100 ", 0xFFFF7F00)).append(compentUtil("：调整收听范围为100个方块\n", 0xFFFFFF00))

            .append(compentUtil("- " + CREATE_GROUP + "<人数>", 0xFFFF0000)).withColor(0xFFFF0000).append(compentUtil("：创建群组聊天，并规定最大人数\n", 0xFFFFFF00))
            .append(compentUtil("举例：/" + CREATE_GROUP + " 4", 0xFFFF7F00)).append(compentUtil("： 创建了4人群组\n", 0xFFFFFF00))
            .append(compentUtil("- " + JOIN_GROUP + "<房间号>", 0xFFFF0000)).append(compentUtil("根据房间号加入群组\n", 0xFFFFFF00))
            .append(compentUtil("- " + QUIT_GROUP, 0xFFFF0000)).append(compentUtil("：退出群组(房主使用此命令，则是解散群)\n", 0xFFFFFF00))
            .append(compentUtil("- " + SET_CREATE_TALK + " <玩家名>", 0xFFFF0000)).append(compentUtil("：请求一个与一个玩家私聊\n", 0xFFFFFF00))
            .append(compentUtil("- " + SET_CANCEL_TALK + " <玩家名>", 0xFFFF0000)).append(compentUtil("：退出私聊\n", 0xFFFFFF00))
            .append(compentUtil("- " + KICK_PLAYER_FROM_GROUP + " <玩家名>", 0xFFFF0000)).append(compentUtil("：踢出玩家\n", 0xFFFFFF00))
            .append(compentUtil("- " + INVITE_PLAYER_FROM_GROUP + " <玩家名>", 0xFFFF0000)).append(compentUtil("：邀请玩家进入群组\n", 0xFFFFFF00));


}
