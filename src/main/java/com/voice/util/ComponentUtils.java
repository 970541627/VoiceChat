package com.voice.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;

import java.util.List;

public class ComponentUtils {
    private static final int RED = 0xFFFF0000;
    private static final int YELLOW = 0xFFFFFF00;
    private static final Integer[] defaultMessageColor = {RED, YELLOW, RED};
    private static final Integer[] jointMessageColor = {RED, YELLOW, RED, YELLOW, RED};

    private static Component messageColorRender(MutableComponent component, Integer[] messageColors) {
        List<Component> siblings = component.getSiblings();
        MutableComponent cur = MutableComponent.create(PlainTextContents.create(""));
        for (int i = 0; i < messageColors.length; i++) {
            cur = cur.append(((MutableComponent) siblings.get(i)).withColor(messageColors[i]));
        }
        return component;
    }

    public static Component playerInfo(String playerName, String details) {
        MutableComponent component = Component.literal("").append("玩家：").append(playerName).append(details);
        return messageColorRender(component, defaultMessageColor);
    }

    public static Component joinInfo(String playerName, String requestMessage) {
        MutableComponent component = Component.literal("").append("玩家：").append(playerName).append(requestMessage).append("输入y").append("同意玩家进入，否则5秒后自动拒绝");
        return messageColorRender(component, jointMessageColor);
    }

    public static Component defaultMessage(String pre, String cruxMessage, String end) {
        MutableComponent component = Component.literal("").append(pre).append(cruxMessage).append(end);
        return messageColorRender(component, defaultMessageColor);
    }

    public static Component defaultMessage(String message) {
        return Component.literal(message).withColor(RED);
    }
}
