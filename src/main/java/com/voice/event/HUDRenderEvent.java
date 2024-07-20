package com.voice.event;

import com.voice.VoiceMod;
import com.voice.ui.UIManger;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = VoiceMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class HUDRenderEvent {
    @SubscribeEvent
    public static void onOverlayRender(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll(VoiceMod.MODID, new UIManger());
    }
}
