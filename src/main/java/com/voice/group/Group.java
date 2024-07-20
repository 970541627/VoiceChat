package com.voice.group;

import net.minecraft.server.level.ServerPlayer;

import java.util.LinkedList;

public class Group {
    private LinkedList<ServerPlayer> groupPlayer;
    private String groupId;
    private boolean removed = false;
    private Integer maxPerson;
    private ServerPlayer owner;


    public Group createGroup(ServerPlayer serverPlayer, Integer maxPerson, String groupId, ServerPlayer owner) {
        this.groupId = groupId;
        this.owner = owner;
        groupPlayer = new LinkedList<>();
        this.maxPerson = maxPerson;
        joinGroup(serverPlayer);
        return this;
    }


    public boolean isOwner(ServerPlayer src) {
        String ownerName = owner.getDisplayName().getString();
        String srcName = src.getDisplayName().getString();
        return srcName.equals(ownerName);
    }

    public void setOwner(ServerPlayer owner) {
        this.owner = owner;
    }

    public void removeGroupPlayer(ServerPlayer removeServerPlayer, boolean idDissolve) {
        if (idDissolve && isOwner(removeServerPlayer)) {
            groupPlayer.clear();
            return;
        }
        groupPlayer.remove(removeServerPlayer);
    }

    public boolean joinGroup(ServerPlayer newServerPlayer) {
        if (groupPlayer.size() <= maxPerson) {
            groupPlayer.add(newServerPlayer);
            return true;
        }
        return false;
    }

    public ServerPlayer getOwner() {
        return owner;
    }

    public void quitGroup() {
        removed = true;
    }

    public boolean isQuit() {
        return removed;
    }

    public String getGroupId() {
        return groupId;
    }

    public ServerPlayer[] groupPlayerList() {
        return groupPlayer.toArray(new ServerPlayer[0]);
    }

    public boolean isInGroup(ServerPlayer player) {
        return groupPlayer.contains(player);
    }
}
