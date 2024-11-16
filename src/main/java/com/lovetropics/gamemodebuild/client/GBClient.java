package com.lovetropics.gamemodebuild.client;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.container.BuildContainer;
import com.lovetropics.gamemodebuild.message.SetGamemodeBuildSlotPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@Mod(value = GamemodeBuild.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = GamemodeBuild.MODID, value = Dist.CLIENT)
public class GBClient {
    public GBClient(IEventBus bus) {
        bus.addListener((final RegisterMenuScreensEvent event) -> {
            event.register(BuildContainer.TYPE.get(), BuildScreen::new);
        });
    }

    @SubscribeEvent
    public static void onInputTriggered(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (event.isPickBlock() && player != null && GamemodeBuild.isActive(player)) {
            event.setCanceled(true);
            HitResult hitResult = minecraft.hitResult;
            if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK) {
                player.connection.send(new SetGamemodeBuildSlotPacket(((BlockHitResult) hitResult).getBlockPos()));
            }
        }
    }
}
