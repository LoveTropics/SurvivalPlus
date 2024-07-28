package com.lovetropics.gamemodebuild.message;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.container.BuildContainer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.BitSet;

public record UpdateFilterMessage(BitSet filter) implements CustomPacketPayload {
	public static final Type<UpdateFilterMessage> TYPE = new Type<>(GamemodeBuild.rl("update_filter"));
	public static final StreamCodec<RegistryFriendlyByteBuf, UpdateFilterMessage> CODEC = ByteBufCodecs.byteArray(Integer.MAX_VALUE)
			.map(BitSet::valueOf, BitSet::toByteArray).map(UpdateFilterMessage::new, UpdateFilterMessage::filter).cast();

	public void handle(IPayloadContext ctx) {
		if (ctx.player().containerMenu instanceof BuildContainer container) {
			container.setFilter(filter);
		}
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
