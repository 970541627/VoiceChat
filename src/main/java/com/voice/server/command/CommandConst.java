package com.voice.server.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class CommandConst {
    public static Map<String, Pair<String, Pair<String, ArgumentType>>> commandTypeMap = new HashMap<>();
    public static Map<String, String> commandWithCallbackName = new HashMap<>();
    private static Map<Class, ArgumentType> argumentTypeMap = new HashMap<>();
    private static void initArgumentTypeMap() {
        argumentTypeMap.put(Float.TYPE, FloatArgumentType.floatArg());
        argumentTypeMap.put(Integer.TYPE, IntegerArgumentType.integer());
        argumentTypeMap.put(String.class, StringArgumentType.string());
        argumentTypeMap.put(EntitySelector.class, EntityArgument.player());
    }

    public static Class getClassByArgumentType(ArgumentType type) {
        Set<Class> typeKeys = argumentTypeMap.keySet();
        for (Class typeKey : typeKeys) {
            if (argumentTypeMap.get(typeKey).equals(type)) {
                return typeKey;
            }
        }
        return null;
    }



    static {
        initArgumentTypeMap();
    }


    private static String loadCommandsToList(String command, String commandType, String arg, Class argType) {
        if (arg == null) {
            commandTypeMap.put(command, new ImmutablePair<>(commandType, null));
        } else {
            ArgumentType argumentType = argumentTypeMap.get(argType);
            commandTypeMap.put(command, new ImmutablePair<>(commandType, new ImmutablePair<>(arg, argumentType)));
        }

        return command;
    }

    private static String loadCommandsToList(String command, String commandType) {
        return loadCommandsToList(command, commandType, null, null);
    }

    private static String loadCommandsToList(String command, String commandType, String callbackName) {
        commandWithCallbackName.put(command, callbackName);
        return loadCommandsToList(command, commandType, null, null);
    }

    public static final String MIC_TYPE = "mic";
    public static final String GROUP_TYPE = "group";
    public static final String TALK_TYPE = "talk";


    public static final String SET_VOLUME_LIMIT = loadCommandsToList("siro th", MIC_TYPE, "rate", Float.TYPE);
    public static final String SET_VOLUME = loadCommandsToList("siro v", MIC_TYPE, "rate", Float.TYPE);
    public static final String SET_PACKET_LENGTH = loadCommandsToList("siro len", MIC_TYPE, "len", Integer.TYPE);
    public static final String SET_MUTE = loadCommandsToList("siro mute", MIC_TYPE);
    public static final String SET_NO_MUTE = loadCommandsToList("siro nomute", MIC_TYPE);
    public static final String GET_GET_AVG_LEVEL = loadCommandsToList("siro avg", MIC_TYPE);
    public static final String GET_HELP = loadCommandsToList("siro help", MIC_TYPE);
    public static final String SET_MIC_POS_X = loadCommandsToList("siro x", MIC_TYPE, "rate", Integer.TYPE);
    public static final String SET_MIC_POS_Y = loadCommandsToList("siro y", MIC_TYPE, "rate", Integer.TYPE);
    public static final String SET_DISTANCE = loadCommandsToList("siro dis", MIC_TYPE, "distance", Integer.TYPE);


    public static final String GET_VERSION = "1.0";


    public static final String SET_CREATE_TALK = loadCommandsToList("siro talk create", TALK_TYPE, "player", EntitySelector.class);
    public static final String SET_CANCEL_TALK = loadCommandsToList("siro talk cancel", TALK_TYPE);
    /**
     * group 相关参数
     */
    public static final String CREATE_GROUP = loadCommandsToList("siro group create", GROUP_TYPE, "max", Integer.TYPE);
    public static final String GET_GROUP_ID = loadCommandsToList("siro group id", GROUP_TYPE);
    public static final String JOIN_GROUP = loadCommandsToList("siro group join ", GROUP_TYPE, "id", String.class);
    public static final String QUIT_GROUP = loadCommandsToList("siro group quit", GROUP_TYPE);
    public static final String KICK_PLAYER_FROM_GROUP = loadCommandsToList("siro group kick", GROUP_TYPE, "player", EntitySelector.class);
    public static final String INVITE_PLAYER_FROM_GROUP = loadCommandsToList("siro group invite", GROUP_TYPE, "player", EntitySelector.class);

    public static final String FLOAT_TYPE = Float.class.getTypeName();
    public static final String INTEGER_TYPE = Integer.class.getTypeName();

    static {
        Field[] fields = CommandConst.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(String.class)) {
                String name = field.getName().toLowerCase();
                int start = 0;
                int index = 0;
                while ((index = name.indexOf("_")) != -1) {
                    if (index == 0) {
                        name = name.substring(1);
                    } else if (index == name.length() - 1) {
                        name = name.substring(0, name.length() - 1);
                    } else {
                        name = name.substring(start, index) + String.valueOf(name.charAt(index + 1)).toUpperCase() + name.substring(index + 2);
                    }
                }
                try {
                    commandWithCallbackName.put((String) field.get(null), name);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

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
