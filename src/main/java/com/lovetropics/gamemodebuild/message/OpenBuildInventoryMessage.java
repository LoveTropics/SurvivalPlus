package com.lovetropics.gamemodebuild.message;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.container.BuildContainer;
import com.lovetropics.gamemodebuild.state.GBPlayerStore;
import com.lovetropics.gamemodebuild.state.GBServerState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.SimpleMenuProvider;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenBuildInventoryMessage() implements CustomPacketPayload {
    public static final Type<OpenBuildInventoryMessage> TYPE = new Type<>(GamemodeBuild.rl("open_build_inventory"));
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBuildInventoryMessage> CODEC = StreamCodec.unit(new OpenBuildInventoryMessage());

    public void handle(IPayloadContext ctx) {
        var player = ctx.player();
        if (GBServerState.isActiveFor(player)) {
            player.openMenu(new SimpleMenuProvider(BuildContainer::new, BuildContainer.title()), buf -> buf.writeUtf(GBPlayerStore.getList(player)));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
