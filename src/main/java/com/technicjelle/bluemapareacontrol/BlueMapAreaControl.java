package com.technicjelle.bluemapareacontrol;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.technicjelle.UpdateChecker;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class BlueMapAreaControl implements Runnable {

	private static final String ADDON_ID, VERSION;
	static {
		Gson gson = new Gson();
		try (
				InputStream is = BlueMapAreaControl.class.getResourceAsStream("/bluemap.addon.json");
				Reader reader = new InputStreamReader(Objects.requireNonNull(is))
		) {
			JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
			ADDON_ID = jsonObject.get("id").getAsString();
			VERSION = jsonObject.get("version").getAsString();
		} catch (Exception ex) {
			throw new RuntimeException("Failed to load build-info.", ex);
		}
	}

	private final Logger logger = Logger.getLogger(ADDON_ID);
	private UpdateChecker updateChecker;

	private static final String CONF_EXT = ".conf";
	private static final String NODE_AREAS = "areas";
	private static final String NODE_DEBUG = "debug-mode";
	private static final String NODE_IS_WHITELIST = "is-whitelist";

	@Override
	public void run() {
		BlueMapAPI.onEnable(onEnableListener);
		BlueMapAPI.onDisable(onDisableListener);

		updateChecker = new UpdateChecker("TechnicJelle", "BlueMapAreaControl", VERSION);
		updateChecker.checkAsync();
	}

	Consumer<BlueMapAPI> onEnableListener = api -> {
		api.getRenderManager().stop(); //do not render anything yet, until the areas are loaded
		getLogger().info("BlueMapAreaControl enabled!");
		updateChecker.logUpdateMessage(getLogger());

		// First time? Create configs
		if (getDataFolder().mkdirs()) {
			getLogger().info("Created plugin config directory");

			for (BlueMapMap map : api.getMaps()) {
				getLogger().info("Creating config for map: " + map.getId());

				Path mapConfigPath = getDataFolder().toPath().resolve(map.getId() + CONF_EXT);
				try (InputStream defaultConfig = BlueMapAreaControl.class.getResourceAsStream("/default.conf")) {
					Files.copy(Objects.requireNonNull(defaultConfig), mapConfigPath);
				} catch (IOException e) {
					getLogger().log(Level.SEVERE, "Failed to copy default config for map " + map.getId(), e);
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

			CommentedConfigurationNode root;
			final boolean debugMode;
			final boolean isWhitelist;
			try {
				root = loader.load();
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Failed to load config for map " + map.getId(), e);
				continue;
			}
			if (root == null) {
				getLogger().warning("Failed to load config root for map " + map.getId());
				continue;
			}
			debugMode = root.node(NODE_DEBUG).getBoolean(false);

			isWhitelist = root.node(NODE_IS_WHITELIST).getBoolean(false);

			try {
				ConfigurationNode areasNode = root.node(NODE_AREAS);
				if (areasNode.virtual()) throw new Exception("areas property is required");
				List<? extends ConfigurationNode> children = areasNode.childrenList();
				for (ConfigurationNode child : children) {
					Area area = getArea(child);
					area.init(map);
					areas.add(area);
				}
			} catch (Exception e) {
				getLogger().log(Level.SEVERE, "Failed to load areas for map " + map.getId(), e);
				continue;
			}

			if (debugMode) {
				getLogger().info("\tDebug Mode is enabled");
				getLogger().info("\tIs Whitelist: " + isWhitelist);

				String key = "Area Control Debug Overlay";
				MarkerSet markerSet = map.getMarkerSets().computeIfAbsent(key, id -> MarkerSet.builder()
						.label(key)
						.toggleable(true)
						.defaultHidden(true)
						.build());

				for (Area area : areas) {
					getLogger().info('\t' + area.debugString());
					markerSet.put(area.debugString() + "block" + area, area.createBlockMarker());
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
		api.getRenderManager().start(); //the areas have been loaded, we may start rendering now
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

	public Logger getLogger() {
		return logger;
	}

	public File getDataFolder() {
		return Path.of("config", ADDON_ID).toFile();
	}

}
