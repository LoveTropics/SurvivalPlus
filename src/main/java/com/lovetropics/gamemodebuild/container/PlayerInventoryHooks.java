package com.lovetropics.gamemodebuild.container;

import com.lovetropics.gamemodebuild.client.BuildScreen;
import com.lovetropics.gamemodebuild.message.OpenBuildInventoryMessage;
import com.lovetropics.gamemodebuild.state.GBClientState;
import com.lovetropics.gamemodebuild.state.GBPlayerStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public final class PlayerInventoryHooks {
	@SubscribeEvent
	public static void onOpenScreen(ScreenEvent.Opening event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		
		if (!GBClientState.isActive()) {
			return;
		}
		
		if (event.getScreen() instanceof InventoryScreen) {
			PacketDistributor.sendToServer(new OpenBuildInventoryMessage());

			final Inventory inventory = player.getInventory();
			BuildContainer container = new BuildContainer(0, inventory, player, null);
			event.setNewScreen(new BuildScreen(container, inventory, BuildContainer.title()));
		}
	}
	
	@SubscribeEvent
	public static void onToss(ItemTossEvent event) {
		if (GBStackMarker.isMarked(event.getEntity().getItem())) {
			event.setCanceled(true);
		}
	}

	@SubscribeEvent
	public static void onPickup(ItemEntityPickupEvent.Pre event) {
		if (GBStackMarker.isMarked(event.getItemEntity().getItem()) != GBPlayerStore.isActive(event.getPlayer())) {
			event.setCanPickup(TriState.FALSE);
		}
	}
}
