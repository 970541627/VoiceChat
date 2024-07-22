package com.voice.server.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandFactory {
    private Map<String, CommandStrategy> commandStrategyMap;

    public CommandFactory() {
        List<CommandStrategy> strategies = new ArrayList<>();
        strategies.add(new GroupStrategy());
        strategies.add(new MicStrategy());
        strategies.add(new TalkStrategy());
        commandStrategyMap = strategies.stream().collect(Collectors.toMap(CommandStrategy::getType, strategy -> strategy));
    }
    public static class Holder {
        public static CommandFactory instance = new CommandFactory();
    }

    public static CommandFactory getInstance() {
        return Holder.instance;
    }

    public CommandStrategy get(String type) {
        return commandStrategyMap.get(type);
    }
}
