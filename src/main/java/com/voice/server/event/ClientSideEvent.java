package com.voice.server.event;

import com.voice.server.service.MicServerService;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(value = Dist.DEDICATED_SERVER, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientSideEvent {
    @SubscribeEvent
    public static void createThread(FMLCommonSetupEvent event) {
        MicServerService.getInstance().startNetworkServerThread();
    }
}
