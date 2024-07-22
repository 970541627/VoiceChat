package com.voice.server.group;

import com.voice.config.GroupConfig;
import com.voice.util.ComponentUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GroupChat {
    private LinkedList<Group> groupLinkedList = new LinkedList<>();
    private Map<String, Group> joinGroupCache = new HashMap<>();
    private static GroupChat chat;

    public static GroupChat getInstance() {
        return chat == null ? (chat = new GroupChat()) : chat;
    }

    public List<String> getServerPlayersName(ServerPlayer member) {
        String playerName = member.getDisplayName().getString();
        if (joinGroupCache.containsKey(playerName)) {
            ServerPlayer[] serverPlayers = joinGroupCache.get(playerName).groupPlayerList();
            List<String> players = new LinkedList<>();
            for (int i = 0; i < serverPlayers.length; i++) {
                players.add(serverPlayers[i].getDisplayName().getString());
            }
            return players;
        }
        return null;
    }

    public Group kickPlayer(ServerPlayer owner, ServerPlayer target) {
        String ownerName = owner.getDisplayName().getString();
        if (joinGroupCache.containsKey(ownerName)) {
            Group group = joinGroupCache.get(ownerName);
            if (group.isOwner(owner)) {
                removePlayer(target, false);
                return group;
            }
        }
        return null;
    }

    public ServerPlayer[] getServerPlayers(ServerPlayer member) {
        String playerName = member.getDisplayName().getString();
        if (joinGroupCache.containsKey(playerName)) {
            ServerPlayer[] serverPlayers = joinGroupCache.get(playerName).groupPlayerList();
            return serverPlayers;
        }
        return null;
    }


    public void notifyOwner(Group group, Component msg) {
        group.getOwner().sendSystemMessage(msg);
    }

    public boolean createGroup(ServerPlayer creator, Integer maxPerson,String groupId) {
        if (!isInGroup(creator)) {
            Group group = new Group().createGroup(creator, maxPerson, groupId, creator);
            groupLinkedList.add(group);
            joinGroupCache.put(creator.getDisplayName().getString(), group);
            GroupConfig.getConfigInstance().saveGroup(group.getGroupId(), maxPerson);
            GroupConfig.getConfigInstance().saveGroupData(creator, group.getGroupId());
            return true;
        }
        return false;
    }
    public boolean createGroup(ServerPlayer creator, Integer maxPerson) {
        if (!isInGroup(creator)) {
            Group group = new Group().createGroup(creator, maxPerson, genRandomGroupId(), creator);
            groupLinkedList.add(group);
            joinGroupCache.put(creator.getDisplayName().getString(), group);
            GroupConfig.getConfigInstance().saveGroup(group.getGroupId(), maxPerson);
            GroupConfig.getConfigInstance().saveGroupData(creator, group.getGroupId());
            return true;
        }
        return false;
    }

    public boolean isInGroup(ServerPlayer player) {
        return joinGroupCache.containsKey(player.getDisplayName().getString());
    }

    public boolean joinGroup(ServerPlayer joiner, String groupId) {
        if (!isInGroup(joiner)) {
            LinkedList<Group> groupLinkedList = this.groupLinkedList;
            int size = groupLinkedList.size();
            for (int i = 0; i < size; i++) {
                Group group = groupLinkedList.get(i);
                if (group.getGroupId().equals(groupId)) {
                    ServerPlayer[] players = group.groupPlayerList();
                    if (players.length == 0) {
                        group.setOwner(joiner);
                    }
                    if (group.joinGroup(joiner)) {
                        joinGroupCache.put(joiner.getDisplayName().getString(), group);
                        GroupConfig.getConfigInstance().saveGroupData(joiner, group.getGroupId());
                    }else{
                        joiner.sendSystemMessage(ComponentUtils.defaultMessage("该群组人数已满"));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public Group removePlayer(ServerPlayer quiter, boolean canDissolve) {
        String playerName = quiter.getDisplayName().getString();
        if (joinGroupCache.containsKey(playerName)) {
            Group group = joinGroupCache.get(playerName);
            if (group.isOwner(quiter)) {
                if (canDissolve) {
                    ServerPlayer[] serverPlayers = group.groupPlayerList();
                    for (int i = 0; i < serverPlayers.length; i++) {
                        String removePlayerName = serverPlayers[i].getDisplayName().getString();
                        if (joinGroupCache.containsKey(removePlayerName)) {
                            joinGroupCache.remove(removePlayerName);
                            serverPlayers[i].sendSystemMessage(ComponentUtils.defaultMessage("群组已解散"));
                        }
                    }
                    joinGroupCache.remove(playerName);
                    groupLinkedList.remove(group);
                    GroupConfig.getConfigInstance().removeGroupCache(group.getGroupId());
                } else {
                    joinGroupCache.remove(playerName);
                    ServerPlayer[] serverPlayers = group.groupPlayerList();
                    if (serverPlayers.length != 0) {
                        group.setOwner(serverPlayers[0]);
                    }
                }

            } else {
                joinGroupCache.remove(playerName);
            }
            group.removeGroupPlayer(quiter, canDissolve);
            return group;
        }
        return null;
    }


    public String getGroupId(ServerPlayer sender) {
        String playerName = sender.getDisplayName().getString();
        if (joinGroupCache.containsKey(playerName)) {
            return joinGroupCache.get(playerName).getGroupId();
        }
        return "你没有加入任何群组";
    }

    private String genRandomGroupId() {
        String num = null;
        boolean canGen = false;
        while (!canGen) {
            num = String.valueOf((int) (Math.random() * 900000 + 100000));//[100000,999999]
            canGen = true;
            for (Group group : groupLinkedList) {
                if (group.getGroupId().equals(num)) {
                    canGen = false;
                    break;
                }
            }
        }
        return num;
    }

    public LinkedList<Group> getGroupLinkedList() {
        return groupLinkedList;
    }

    public Group getGroupById(String id) {
        for (Group group : groupLinkedList) {
            if (group.getGroupId().equals(id)) {
                return group;
            }
        }
        return null;
    }
}
