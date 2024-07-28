package com.lovetropics.gamemodebuild.message;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.container.BuildContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetScrollMessage(int scrollOffset) implements CustomPacketPayload {
	public static final Type<SetScrollMessage> TYPE = new Type<>(GamemodeBuild.rl("set_scroll"));
	public static final StreamCodec<RegistryFriendlyByteBuf, SetScrollMessage> CODEC = ByteBufCodecs.VAR_INT
			.map(SetScrollMessage::new, SetScrollMessage::scrollOffset).cast();

	public void handle(IPayloadContext ctx) {
		if (ctx.player().containerMenu instanceof BuildContainer container) {
			container.setScrollOffset(scrollOffset);
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
