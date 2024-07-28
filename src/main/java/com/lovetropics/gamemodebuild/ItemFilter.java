package com.lovetropics.gamemodebuild;

import net.minecraft.Util;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.SingleKeyCache;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ItemFilter {
	
	private record ItemTypeFilter(ResourceKey<Item> item) implements Predicate<Item> {
		@Override
		public boolean test(Item item) {
			return item.builtInRegistryHolder().is(this.item);
		}
	}

	private record TagFilter(TagKey<Item> tag) implements Predicate<Item> {
		@Override
		public boolean test(Item item) {
			return item.builtInRegistryHolder().is(tag);
		}
	}
	
	public static ItemFilter fromStrings(List<String> whitelist, List<String> blacklist) {
		return new ItemFilter(parsePredicates(whitelist), parsePredicates(blacklist));
	}
	
	private static List<Predicate<Item>> parsePredicates(List<String> predicates) {
		if (predicates == null) return new ArrayList<>();

		return predicates.stream()
				.map(ItemFilter::parsePredicate)
				.collect(Collectors.toList());
	}
	
	private static Predicate<Item> parsePredicate(String predicate) {
		Objects.requireNonNull(predicate);
		if ("*".equals(predicate)) {
			return item -> true;
		} else if (predicate.startsWith("#")) {
			final ResourceLocation tagLocation = ResourceLocation.parse(predicate.substring(1));
			return new TagFilter(TagKey.create(Registries.ITEM, tagLocation));
		} else {
			final ResourceLocation location = ResourceLocation.parse(predicate);
			return new ItemTypeFilter(ResourceKey.create(Registries.ITEM, location));
		}
	}
	
	private final List<Predicate<Item>> whitelistPredicates;
	private final List<Predicate<Item>> blacklistPredicates;
	private final SingleKeyCache<Key, List<ItemStack>> cache;

	private ItemFilter(List<Predicate<Item>> whitelist, List<Predicate<Item>> blacklist) {
		this.whitelistPredicates = new ArrayList<>(whitelist);
		this.blacklistPredicates = new ArrayList<>(blacklist);
		cache = Util.singleKeyCache(key -> computeStacks(key.featureFlags(), key.registryAccess()));
	}
	
	public List<ItemStack> getAllStacks(FeatureFlagSet enabledFeatures, RegistryAccess registryAccess) {
		return cache.getValue(new Key(enabledFeatures, registryAccess));
	}

	private List<ItemStack> computeStacks(FeatureFlagSet enabledFeatures, HolderLookup.Provider registryAccess) {
		if (whitelistPredicates.isEmpty()) {
			return List.of();
		}

		Set<ItemStack> items = ItemStackLinkedSet.createTypeAndComponentsSet();
		CreativeModeTab.Output output = createFilteredOutput(items);

		CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(enabledFeatures, true, registryAccess);
		BuiltInRegistries.CREATIVE_MODE_TAB.holders().forEach(holder -> {
			CreativeModeTab tab = holder.value();
			if (tab.getType() != CreativeModeTab.Type.SEARCH) {
				generateItems(holder.key(), tab, parameters, output);
			}
		});

		return List.copyOf(items);
	}

	// We have to invoke this logic ourselves, as Forge hooks the Vanilla path in such a way that it classloads client-side code
	private static void generateItems(ResourceKey<CreativeModeTab> tabKey, CreativeModeTab tab, CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
		tab.buildContents(parameters);
		tab.getDisplayItems().forEach(output::accept);
	}

	private CreativeModeTab.Output createFilteredOutput(final Set<ItemStack> items) {
		final Predicate<ItemStack> whitelist = stack -> {
			for (final Predicate<Item> predicate : whitelistPredicates) {
				if (predicate.test(stack.getItem())) {
					return true;
				}
			}
			return false;
		};

		final Predicate<ItemStack> blacklist;
		if (!blacklistPredicates.isEmpty()) {
			blacklist = stack -> {
				for (final Predicate<Item> predicate : blacklistPredicates) {
					if (predicate.test(stack.getItem())) {
						return false;
					}
				}
				return true;
			};
		} else {
			blacklist = stack -> true;
		}

        return (stack, visibility) -> {
			if (whitelist.test(stack) && blacklist.test(stack)) {
				items.add(stack);
			}
		};
	}

	private record Key(FeatureFlagSet featureFlags, HolderLookup.Provider registryAccess) {
	}
}
