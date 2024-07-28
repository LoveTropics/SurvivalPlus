package com.lovetropics.gamemodebuild.message;

import com.lovetropics.gamemodebuild.GBConfigs;
import com.lovetropics.gamemodebuild.GamemodeBuild;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ListUpdateMessage(Operation operation, boolean whitelist, String name, String entry) implements CustomPacketPayload {
	public static final Type<ListUpdateMessage> TYPE = new Type<>(GamemodeBuild.rl("update_list"));
	public static final StreamCodec<RegistryFriendlyByteBuf, ListUpdateMessage> CODEC = StreamCodec.composite(
			ByteBufCodecs.idMapper(i -> Operation.values()[i], Operation::ordinal),
			ListUpdateMessage::operation,
			ByteBufCodecs.BOOL,
			ListUpdateMessage::whitelist,
			ByteBufCodecs.STRING_UTF8,
			ListUpdateMessage::name,
			ByteBufCodecs.STRING_UTF8,
			ListUpdateMessage::entry,
			ListUpdateMessage::new
	);

	public void handle(IPayloadContext context) {
		switch (operation) {
			case ADD -> {
				if (whitelist) {
					GBConfigs.SERVER.addToWhitelist(name, entry, false);
				} else {
					GBConfigs.SERVER.addToBlacklist(name, entry, false);
				}
			}
			case REMOVE -> {
				if (whitelist) {
					GBConfigs.SERVER.removeFromWhitelist(name, entry, false);
				} else {
					GBConfigs.SERVER.removeFromBlacklist(name, entry, false);
				}
			}
			case CLEAR -> {
				if (whitelist) {
					GBConfigs.SERVER.clearWhitelist(name, false);
				} else {
					GBConfigs.SERVER.clearBlacklist(name, false);
				}
			}
		}
	}

	public enum Operation {
		ADD, REMOVE, CLEAR;
	}

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
