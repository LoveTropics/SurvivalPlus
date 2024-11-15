package com.lovetropics.gamemodebuild.message;

import com.lovetropics.gamemodebuild.GBConfigs;
import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.container.BuildContainer;
import com.lovetropics.gamemodebuild.container.GBStackMarker;
import com.lovetropics.gamemodebuild.state.GBPlayerStore;
import com.lovetropics.gamemodebuild.state.GBServerState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

public record SetGamemodeBuildSlotPacket(short slotNum, BlockPos blockPos)  implements CustomPacketPayload {
    public static final Type<SetGamemodeBuildSlotPacket> TYPE = new Type<>(GamemodeBuild.rl("set_slot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetGamemodeBuildSlotPacket> CODEC;
    static {
        CODEC = StreamCodec.composite(ByteBufCodecs.SHORT, SetGamemodeBuildSlotPacket::slotNum, BlockPos.STREAM_CODEC, SetGamemodeBuildSlotPacket::blockPos, SetGamemodeBuildSlotPacket::new);
    }

    public SetGamemodeBuildSlotPacket(short slotNum, BlockPos blockPos) {
        this.slotNum = slotNum;
        this.blockPos = blockPos;
    }

    public static void handle(SetGamemodeBuildSlotPacket packet, IPayloadContext ctx) {
        var player = ctx.player();

        if (player instanceof ServerPlayer serverPlayer) {
            ctx.enqueueWork(() -> {
                if(!player.canInteractWithBlock(packet.blockPos, ServerPlayer.INTERACTION_DISTANCE_VERIFICATION_BUFFER)) {
                    return;
                }

                if (GBServerState.isActiveFor(serverPlayer)) {
                    ItemStack itemstack;

                    FeatureFlagSet featureFlags = player.level().enabledFeatures();
                    RegistryAccess registryAccess = player.level().registryAccess();
                    List<ItemStack> itemStacks = GBConfigs.SERVER.getFilter(GBPlayerStore.getList(player)).getAllStacks(featureFlags, registryAccess);

                    BlockState blockstate = player.level().getBlockState(packet.blockPos);
                    if (blockstate.isAir()) {
                        return;
                    }

                    itemstack = new ItemStack(blockstate.getBlock());

                    GBStackMarker.mark(itemstack);

                    if (itemstack.isEmpty()) {
                        return;
                    }

                    if(!itemStacks.stream().anyMatch(stack -> stack.is(itemstack.getItem()))) {
                        player.inventoryMenu.setRemoteSlot(packet.slotNum, itemstack);
                        player.inventoryMenu.broadcastChanges();
                        return;
                    }

                    if (!itemstack.isItemEnabled(player.level().enabledFeatures())) {
                        return;
                    }

                    CustomData customdata = itemstack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY);
                    if (customdata.contains("x") && customdata.contains("y") && customdata.contains("z")) {
                        BlockPos blockpos = BlockEntity.getPosFromTag(customdata.getUnsafe());
                        if (player.level().isLoaded(blockpos)) {
                            BlockEntity blockentity = player.level().getBlockEntity(blockpos);
                            if (blockentity != null) {
                                blockentity.saveToItem(itemstack, player.level().registryAccess());
                            }
                        }
                    }

                    boolean flag1 = packet.slotNum() >= 1 && packet.slotNum() <= 45;
                    boolean flag2 = itemstack.isEmpty() || itemstack.getCount() <= itemstack.getMaxStackSize();
                    if (flag1 && flag2) {
                        player.inventoryMenu.getSlot(packet.slotNum()).setByPlayer(itemstack);
                        player.inventoryMenu.broadcastChanges();
                    }
                }
            });
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


    public short slotNum() {
        return this.slotNum;
    }
}
