package com.voice.server.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

public class CommandExecutor implements Command<CommandSourceStack> {
    public static Command<CommandSourceStack> instance = new CommandExecutor();
    private CommandFactory factory = new CommandFactory();

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        String commandsLine = context.getInput();
        ServerPlayer sender = context.getSource().getPlayer();
        Set<String> commands = CommandConst.commandTypeMap.keySet();
        try {
            for (String command : commands) {
                if (commandsLine.startsWith(command)) {
                    Pair<String, Pair<String, ArgumentType>> commandInfo = CommandConst.commandTypeMap.get(command);
                    String getType = commandInfo.getLeft();
                    CommandStrategy commandStrategy = factory.get(getType);
                    if (commandInfo.getRight() != null) {
                        Pair<String, ArgumentType> argInfo = commandInfo.getRight();
                        Class type = CommandConst.getClassByArgumentType(argInfo.getRight());
                        Object arg = context.getArgument(argInfo.getLeft(), type);
                        if (type.equals(EntitySelector.class))
                            arg = ((EntitySelector) arg).findSinglePlayer(context.getSource());
                        commandStrategy.executor(sender, command, arg);
                    } else {
                        commandStrategy.executor(sender, command, null);
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


}
