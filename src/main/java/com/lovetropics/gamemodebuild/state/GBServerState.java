package com.lovetropics.gamemodebuild.state;

import com.lovetropics.gamemodebuild.GBConfigs;
import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.message.SetActiveMessage;
import it.unimi.dsi.fastutil.objects.Reference2BooleanMap;
import it.unimi.dsi.fastutil.objects.Reference2BooleanOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber
public final class GBServerState {
	public static void setGloballyEnabled(MinecraftServer server, boolean enabled) {
		if (enabled == isGloballyEnabled()) {
			return;
		}

		Reference2BooleanMap<ServerPlayer> activeMap = new Reference2BooleanOpenHashMap<>();
		for (ServerPlayer player : server.getPlayerList().getPlayers()) {
			activeMap.put(player, isActiveFor(player));
		}

		GBConfigs.SERVER.enable(enabled);
        activeMap.forEach((player, wasActive) -> notifyPlayerActivity(wasActive, player));
	}

	public static void setEnabledFor(ServerPlayer player, boolean enabled) {
		boolean wasActive = isActiveFor(player);
		GBPlayerStore.setEnabled(player, enabled);
		notifyPlayerActivity(wasActive, player);
	}

	public static boolean isGloballyEnabled() {
		return GBConfigs.SERVER.enabled();
	}
	
	public static boolean isEnabledFor(Player player) {
		return isGloballyEnabled() && GBPlayerStore.isEnabled(player);
	}
	
	public static void requestActive(ServerPlayer player, boolean active) {
		if (!isEnabledFor(player)) {
			notifyDisabled(player);
			return;
		}
		boolean wasActive = isActiveFor(player);
		GBPlayerStore.setActive(player, active);
		notifyPlayerActivity(wasActive, player);
	}

	public static boolean isActiveFor(Player player) {
		return isEnabledFor(player) && GBPlayerStore.isActive(player);
	}

	private static void notifyPlayerActivity(boolean prevState, ServerPlayer player) {
		boolean state = isActiveFor(player);
        if (prevState == state) {
            return;
        }
        GBPlayerStore.switchToInventory(player, state);
        if (state) {
            player.displayClientMessage(Component.literal(GamemodeBuild.NAME + " activated"), true);
        } else {
            player.displayClientMessage(Component.literal(GamemodeBuild.NAME + " deactivated"), true);
        }
        sendPlayerState(player);
		player.closeContainer();
    }

	public static void notifyDisabled(ServerPlayer player) {
		player.displayClientMessage(Component.literal(GamemodeBuild.NAME + " is disabled!"), true);
	}

	public static void sendPlayerState(ServerPlayer player) {
		SetActiveMessage message = new SetActiveMessage(isActiveFor(player));
		PacketDistributor.sendToPlayer(player, message);
	}

	@SubscribeEvent
	public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
		if (event.getEntity() instanceof ServerPlayer player) {
			sendPlayerState(player);
		}
	}
}
