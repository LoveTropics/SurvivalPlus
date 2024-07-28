package com.lovetropics.gamemodebuild;

import com.lovetropics.gamemodebuild.command.GamemodeBuildCommand;
import com.lovetropics.gamemodebuild.command.ItemFilterArgument;
import com.lovetropics.gamemodebuild.container.BuildContainer;
import com.lovetropics.gamemodebuild.container.GBStackMarker;
import com.lovetropics.gamemodebuild.message.GBNetwork;
import com.lovetropics.gamemodebuild.state.GBClientState;
import com.lovetropics.gamemodebuild.state.GBServerState;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.regex.Pattern;

@Mod(GamemodeBuild.MODID)
public class GamemodeBuild {
	public static final String MODID = "gamemodebuild";
	public static final String NAME = "Build Mode";

	private static final DeferredRegister<ArgumentTypeInfo<?, ?>> ARGUMENT_REGISTER = DeferredRegister.create(Registries.COMMAND_ARGUMENT_TYPE, MODID);

	private static final Holder<ArgumentTypeInfo<?, ?>> ITEM_FILTER_ARGUMENT = ARGUMENT_REGISTER.register("item_filter", () -> ArgumentTypeInfos.registerByClass(ItemFilterArgument.class, SingletonArgumentInfo.contextAware(ItemFilterArgument::new)));

	public GamemodeBuild(IEventBus modBus, ModContainer container) {
		modBus.addListener(GBNetwork::register);
		ARGUMENT_REGISTER.register(modBus);
		BuildContainer.REGISTER.register(modBus);
		GBStackMarker.TYPES.register(modBus);

		// Register ourselves for server and other game events we are interested in
		NeoForge.EVENT_BUS.register(this);

		container.registerConfig(ModConfig.Type.SERVER, GBConfigs.serverSpec);
	}

	public static ResourceLocation rl(String path) {
		return ResourceLocation.fromNamespaceAndPath(MODID, path);
	}

    private static final Pattern QUALIFIER = Pattern.compile("-\\w+\\+\\d+");
    public static String getCompatVersion() {
    	return getCompatVersion(ModList.get().getModContainerById(MODID).orElseThrow(IllegalStateException::new).getModInfo().getVersion().toString());
    }
    private static String getCompatVersion(String fullVersion) {
    	return QUALIFIER.matcher(fullVersion).replaceAll("");
    }
    public static boolean isCompatibleVersion(String version) {
    	return getCompatVersion().equals(getCompatVersion(version));
    }

	@SubscribeEvent
	public void registerCommands(RegisterCommandsEvent event) {
		GamemodeBuildCommand.register(event.getDispatcher(), event.getBuildContext());
	}

	@SubscribeEvent
	public void onBreakSpeed(final PlayerEvent.BreakSpeed event) {
		if (event.getEntity().onGround()) return;

		if (GBConfigs.SERVER.removeBreakSpeedDebuff() && isActive(event.getEntity())) {
			// See Player#getDigSpeed, if the player is flying they break blocks 5 times slower.
			// Let's revert that as it's an annoying limitation in build mode
			event.setNewSpeed(event.getNewSpeed() * 5f);
		}
	}

	public static boolean isActive(Player player) {
		if (player.level().isClientSide()) {
			return GBClientState.isActive();
		} else {
			return GBServerState.isActiveFor(player);
		}
	}
}
