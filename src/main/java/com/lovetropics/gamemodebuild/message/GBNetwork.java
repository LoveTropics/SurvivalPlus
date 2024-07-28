package com.lovetropics.gamemodebuild.message;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class GBNetwork {

	public static void register(RegisterPayloadHandlersEvent event) {
		final var registrar = event.registrar("1");

		registrar.playToServer(
				OpenBuildInventoryMessage.TYPE,
				OpenBuildInventoryMessage.CODEC,
				OpenBuildInventoryMessage::handle
		);
		registrar.playToServer(
				SetScrollMessage.TYPE,
				SetScrollMessage.CODEC,
				SetScrollMessage::handle
		);
		registrar.playToServer(
				UpdateFilterMessage.TYPE,
				UpdateFilterMessage.CODEC,
				UpdateFilterMessage::handle
		);

		registrar.playBidirectional(
				SetActiveMessage.TYPE,
				SetActiveMessage.CODEC,
				SetActiveMessage::handle
		);

		registrar.playToClient(
				ListUpdateMessage.TYPE,
				ListUpdateMessage.CODEC,
				ListUpdateMessage::handle
		);
	}
}
