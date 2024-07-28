package com.lovetropics.gamemodebuild;

import com.lovetropics.gamemodebuild.message.SetActiveMessage;
import com.lovetropics.gamemodebuild.state.GBClientState;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class GBKeyBindings {
	public static final KeyMapping SWITCH_MODE = new KeyMapping("Enable/Disable Build Mode", InputConstants.KEY_B, "Build Mode");

	@SubscribeEvent
	public static void onKeyInput(ClientTickEvent.Post event) {
		if (SWITCH_MODE.consumeClick()) {
			LocalPlayer player = Minecraft.getInstance().player;
			if (player != null) {
				// don't set local state: await confirmation from the server
				boolean active = !GBClientState.isActive();
				PacketDistributor.sendToServer(new SetActiveMessage(active));
			}
		}
	}
}
