package com.voice.ui;

import com.voice.VoiceMod;
import com.voice.players.ServerPlayerManager;
import com.voice.recording.SoundDataCenter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

public class UIManger implements IGuiOverlay {
    private static ResourceLocation active = new ResourceLocation(VoiceMod.MODID, "textures/gui/active.png");
    private static ResourceLocation inactive = new ResourceLocation(VoiceMod.MODID, "textures/gui/inactive.png");
    private static ResourceLocation icon = new ResourceLocation(VoiceMod.MODID, "textures/gui/alex.png");
    private static ResourceLocation speak = new ResourceLocation(VoiceMod.MODID, "textures/gui/speak.png");

    private Minecraft minecraft;

    public UIManger() {
        minecraft = Minecraft.getInstance();
    }


    @Override
    public void render(ForgeGui gui, GuiGraphics guiGraphics, float partialTick, int screenWidth, int screenHeight) {
        int posX = (int) (screenWidth * ((double) Hud.xRate / 100.00));
        int posY = (int) (screenHeight * ((double) Hud.yRate / 100.00));
        if (SoundDataCenter.isActive) {
            guiGraphics.blit(active, posX, posY, 0, 0, 0, 32, 32, 32, 32);
        } else {
            guiGraphics.blit(inactive, posX, posY, 0, 0, 0, 32, 32, 32, 32);
        }
        int iconPosX = 0;
        int iconPosY = (int) (screenWidth * ((double) 5.00 / 100.00));
        if (ServerPlayerManager.groupIsNotNull()) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, "群聊", 4, iconPosY - 10, 0xFFFFFF00);
            List<String> groupPlayers = ServerPlayerManager.getGroupPlayers();
            int size = groupPlayers.size();
            for (int i = 0; i < size; i++) {
                guiGraphics.blit(icon, iconPosX, iconPosY, 0, 8, 8, 8, 8, 64, 64);
                if (i == 0) {
                    guiGraphics.drawString(font, "群主：" + groupPlayers.get(i), iconPosX + 8 + 5, iconPosY, 0xFFFF0000);
                    if (ServerPlayerManager.getWhoIsTalking().equals(groupPlayers.get(i))) {
                        int width = font.width("群主：" +groupPlayers.get(i));
                        guiGraphics.blit(speak, iconPosX + 8 + width+5, iconPosY, 0, 0, 0, 8, 8, 8, 8);
                    }
                } else {
                    guiGraphics.drawString(font, groupPlayers.get(i), iconPosX + 8 + 5, iconPosY, 0xFFFF0000);
                    if (ServerPlayerManager.getWhoIsTalking().equals(groupPlayers.get(i))) {
                        int width = font.width(groupPlayers.get(i));
                        guiGraphics.blit(speak, iconPosX + 8 + width+5, iconPosY, 0, 0, 0, 8, 8, 8, 8);
                    }
                }

                iconPosY += 8 + 5;
            }
        } else if (ServerPlayerManager.talkIsNotNull()) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawString(font, "私聊", 4, iconPosY - 10, 0xFFFFFF00);
            List<String> talkPlayers = ServerPlayerManager.getTalkPlayers();
            int size = talkPlayers.size();
            for (int i = 0; i < size; i++) {
                guiGraphics.blit(icon, iconPosX, iconPosY, 0, 8, 8, 8, 8, 64, 64);
                if (i == 0) {
                    guiGraphics.drawString(font,  talkPlayers.get(i), iconPosX + 8 + 5, iconPosY, 0xFFFF0000);
                } else {
                    guiGraphics.drawString(font, talkPlayers.get(i), iconPosX + 8 + 5, iconPosY, 0xFFFF0000);
                }
                if (ServerPlayerManager.getWhoIsTalking().equals(talkPlayers.get(i))) {
                    int width = font.width(talkPlayers.get(i));
                    guiGraphics.blit(speak, iconPosX + 8 + width+5, iconPosY, 0, 0, 0, 8, 8, 8, 8);
                }
                iconPosY += 8 + 5;
            }
        }
        ServerPlayerManager.setWhoIsTalking("");
    }

}
