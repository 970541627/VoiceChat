package com.voice.client.service;

import com.voice.server.service.GroupServerService;

import java.util.List;

public class GroupClientService {
    private List<String> groupPlayers;
    private List<String> talkPlayers;
    private String whoIsTalking = "";
    private static GroupClientService groupClientService;
    public static GroupClientService getInstance(){
        return  groupClientService==null? groupClientService=new GroupClientService(): groupClientService;
    }

    public  void updateGroupList(List<String> newList) {
        groupPlayers = newList;
    }

    public  void updateTalkList(List<String> newList) {
        talkPlayers = newList;
    }

    public boolean groupIsNotNull() {
        return groupPlayers != null && !groupPlayers.isEmpty();
    }

    public boolean talkIsNotNull() {
        return talkPlayers != null && !talkPlayers.isEmpty();
    }

    public String getWhoIsTalking() {
        return whoIsTalking;
    }

    public void setWhoIsTalking(String whoIsTalking) {
        this.whoIsTalking = whoIsTalking;
    }

    public List<String> getGroupPlayers() {
        return groupPlayers;
    }

    public List<String> getTalkPlayers() {
        return talkPlayers;
    }
}
