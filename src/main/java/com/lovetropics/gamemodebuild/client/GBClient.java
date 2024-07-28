package com.lovetropics.gamemodebuild.client;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.container.BuildContainer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = GamemodeBuild.MODID, dist = Dist.CLIENT)
public class GBClient {
    public GBClient(IEventBus bus) {
        bus.addListener((final RegisterMenuScreensEvent event) -> {
            event.register(BuildContainer.TYPE.get(), BuildScreen::new);
        });
    }
}
