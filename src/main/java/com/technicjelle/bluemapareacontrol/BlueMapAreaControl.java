package com.technicjelle.bluemapareacontrol;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class BlueMapAreaControl extends JavaPlugin {
	private static final String CONF_EXT = ".conf";
	private static final String NODE_AREAS = "areas";
	private static final String NODE_DEBUG = "debug-mode";
	private static final String NODE_IS_WHITELIST = "is-whitelist";

	@Override
	public void onEnable() {
		BlueMapAPI.onEnable(onEnableListener);
		BlueMapAPI.onDisable(onDisableListener);
	}

	Consumer<BlueMapAPI> onEnableListener = api -> {
		getLogger().info("BlueMapAreaControl enabled!");

		// First time? Create configs
		if (getDataFolder().mkdirs()) {
			getLogger().info("Created plugin config directory");

			for (BlueMapMap map : api.getMaps()) {
				getLogger().info("Creating config for map: " + map.getId());

				Path mapConfigPath = getDataFolder().toPath().resolve(map.getId() + CONF_EXT);
				try {
					Files.copy(Objects.requireNonNull(getResource("default.conf")), mapConfigPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		// Load configs
		getLogger().info("Loading existing configs");

		File configPath = getDataFolder();
		File[] files = configPath.listFiles();
		if (files == null) return;

		for (File file : files) {
			if (!file.getName().endsWith(CONF_EXT)) continue;

			String mapId = file.getName().substring(0, file.getName().length() - CONF_EXT.length());

			Optional<BlueMapMap> oMap = api.getMap(mapId);
			if (oMap.isEmpty()) {
				getLogger().warning("Map not found: " + mapId);
				continue;
			}
			BlueMapMap map = oMap.get();

			getLogger().info("Loading config for map: " + map.getId());

			List<Area> areas = new ArrayList<>();

			HoconConfigurationLoader loader = HoconConfigurationLoader.builder().path(file.toPath()).build();

			CommentedConfigurationNode root = null;
			final boolean debugMode;
			final boolean isWhitelist;
			try {
				root = loader.load();
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (root == null) continue;

			debugMode = root.node(NODE_DEBUG).getBoolean(false);
			getLogger().info("Debug mode: " + debugMode);

			isWhitelist = root.node(NODE_IS_WHITELIST).getBoolean(true);
			getLogger().info("Is whitelist: " + isWhitelist);

			try {
				ConfigurationNode areasNode = root.node(NODE_AREAS);
				if (areasNode.virtual()) throw new Exception("areas property is required");
				List<? extends ConfigurationNode> children = areasNode.childrenList();
				for (ConfigurationNode child : children) {
					Area area = getArea(child);
					areas.add(area);
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			if(debugMode) {
				MarkerSet markerSet = map.getMarkerSets().computeIfAbsent("BlueMapAreaControl" + map.getId(), id -> MarkerSet.builder()
						.label("BlueMapAreaControl")
						.toggleable(true)
						.defaultHidden(true)
						.build());

				for (Area area : areas) {
					getLogger().info('\t' + area.debugString());
					markerSet.put(area.debugString(), area.createMarker(map));
				}
			}

			map.setTileFilter(tilePos -> {
				boolean inArea = false;
				for (Area area : areas) {
					if (area.containsTile(tilePos.getX(), tilePos.getY())) {
						inArea = true;
						break;
					}
				}
				return inArea == isWhitelist;
			});
		}
	};

	@NotNull Area getArea(ConfigurationNode node) throws Exception {
		String type = node.node("type").getString();
		if (type == null) throw new Exception("Could not find area type");
		switch (type) {
			case AreaRect.TYPE:
				AreaRect areaRect = node.get(AreaRect.class);
				if (areaRect == null) throw new Exception("Could not parse area rect");
				if (areaRect.isValid()) return areaRect;
				else throw new Exception("Invalid area rect");
			case AreaEllipse.TYPE:
				AreaEllipse areaEllipse = node.get(AreaEllipse.class);
				if (areaEllipse == null) throw new Exception("Could not parse area ellipse");
				if (areaEllipse.isValid()) return areaEllipse;
				else throw new Exception("Invalid area ellipse");
			default:
				throw new Exception("Invalid area type");
		}
	}

	Consumer<BlueMapAPI> onDisableListener = api ->
			getLogger().info("BlueMapAreaControl disabled!");

	@Override
	public void onDisable() {
		BlueMapAPI.unregisterListener(onEnableListener);
		BlueMapAPI.unregisterListener(onDisableListener);
	}
}
