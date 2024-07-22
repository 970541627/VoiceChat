package com.voice.server.command;

import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;

public interface CommandStrategy {
    void executor(ServerPlayer player,String commandWithNoArgs, @Nullable Object arg);

    String getType();
}
