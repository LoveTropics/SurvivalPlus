package com.lovetropics.gamemodebuild.container;

import com.lovetropics.gamemodebuild.GamemodeBuild;
import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class GBStackMarker {
	public static final DeferredRegister<DataComponentType<?>> TYPES = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, GamemodeBuild.MODID);
	public static final Supplier<DataComponentType<Unit>> MARKER = TYPES.register("marker", () -> DataComponentType.<Unit>builder()
			.persistent(Codec.unit(Unit.INSTANCE)).networkSynchronized(StreamCodec.unit(Unit.INSTANCE))
			.build());

	public static void mark(ItemStack stack) {
		stack.set(GBStackMarker.MARKER, Unit.INSTANCE);
	}
	
	public static boolean isMarked(ItemStack stack) {
		return stack.has(GBStackMarker.MARKER);
	}
}
