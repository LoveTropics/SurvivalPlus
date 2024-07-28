package com.lovetropics.gamemodebuild.message;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.state.GBClientState;
import com.lovetropics.gamemodebuild.state.GBServerState;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SetActiveMessage(boolean active) implements CustomPacketPayload {
    public static final Type<SetActiveMessage> TYPE = new Type<>(GamemodeBuild.rl("set_active"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetActiveMessage> CODEC = ByteBufCodecs.BOOL
            .map(SetActiveMessage::new, SetActiveMessage::active).cast();

    public void handle(IPayloadContext ctx) {
        if (ctx.flow() == PacketFlow.SERVERBOUND) {
            if (ctx.player() instanceof ServerPlayer player) {
                GBServerState.requestActive(player, active);
            }
        } else {
            GBClientState.setActive(active);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
