package com.lovetropics.gamemodebuild.message;

import com.lovetropics.gamemodebuild.GBConfigs;
import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.lovetropics.gamemodebuild.container.GBStackMarker;
import com.lovetropics.gamemodebuild.state.GBPlayerStore;
import com.lovetropics.gamemodebuild.state.GBServerState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.function.Predicate;

// TODO: Won't need a custom packet in 1.21.4+ - it's server-authoritative
public record SetGamemodeBuildSlotPacket(BlockPos blockPos) implements CustomPacketPayload {
    public static final Type<SetGamemodeBuildSlotPacket> TYPE = new Type<>(GamemodeBuild.rl("pick_block"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetGamemodeBuildSlotPacket> CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SetGamemodeBuildSlotPacket::blockPos,
            SetGamemodeBuildSlotPacket::new
    );

    public static void handle(SetGamemodeBuildSlotPacket packet, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player) || !GBServerState.isActiveFor(player)) {
            return;
        }

        BlockPos blockPos = packet.blockPos;
        if (!player.canInteractWithBlock(blockPos, ServerPlayer.INTERACTION_DISTANCE_VERIFICATION_BUFFER)) {
            return;
        }

        BlockState blockState = player.level().getBlockState(blockPos);

        // Just fake it, it's not important
        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(blockPos), Direction.UP, blockPos, false);
        ItemStack itemStack = blockState.getCloneItemStack(hitResult, player.level(), blockPos, player);
        if (itemStack.isEmpty()) {
            return;
        }

        FeatureFlagSet enabledFeatures = player.level().enabledFeatures();
        RegistryAccess registryAccess = player.level().registryAccess();
        Predicate<ItemStack> predicate = GBConfigs.SERVER.getFilter(GBPlayerStore.getList(player)).getStackPredicate(enabledFeatures, registryAccess);
        if (!predicate.test(itemStack)) {
            return;
        }

        GBStackMarker.mark(itemStack);
        itemStack.setCount(itemStack.getMaxStackSize());

        pickItem(player, itemStack);
    }

    private static void pickItem(ServerPlayer player, ItemStack itemStack) {
        Inventory inventory = player.getInventory();
        int slotWithItem = inventory.findSlotMatchingItem(itemStack);
        if (slotWithItem == Inventory.NOT_FOUND_INDEX) {
            pickFreshItem(inventory, itemStack);
        } else {
            pickExistingItem(inventory, slotWithItem);
        }
        player.connection.send(new ClientboundSetCarriedItemPacket(inventory.selected));
        player.inventoryMenu.broadcastChanges();
    }

    private static void pickExistingItem(Inventory inventory, int slot) {
        if (Inventory.isHotbarSlot(slot)) {
            inventory.selected = slot;
        } else {
            inventory.pickSlot(slot);
        }
    }

    private static void pickFreshItem(Inventory inventory, ItemStack itemStack) {
        inventory.selected = inventory.getSuitableHotbarSlot();
        if (!inventory.getItem(inventory.selected).isEmpty()) {
            int freeSlot = inventory.getFreeSlot();
            if (freeSlot != Inventory.NOT_FOUND_INDEX) {
                inventory.setItem(freeSlot, inventory.getItem(inventory.selected));
            }
        }
        inventory.setItem(inventory.selected, itemStack);
    }

    @Override
    public Type<SetGamemodeBuildSlotPacket> type() {
        return TYPE;
    }
}
