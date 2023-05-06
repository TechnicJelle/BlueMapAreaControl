package com.technicjelle.bluemapareacontrol;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;

@ConfigSerializable
public class AreaEllipse implements Area {
	@Comment("Center X coordinate in blocks")
	private final Integer x;
	@Comment("Center Z coordinate in blocks")
	private final Integer z;

	@Comment("Radius X in blocks")
	private final Integer rx;
	@Comment("Radius Z in blocks")
	private final Integer rz;

	public static final String TYPE = "ellipse";
	private final String type = TYPE;

	private transient int tileSize;
	private transient int tileOffset;

	public AreaEllipse() {
		this.x = null;
		this.z = null;
		this.rx = null;
		this.rz = null;
	}

	public AreaEllipse(int x, int z, int rx, int rz) {
		this.x = x;
		this.z = z;
		this.rx = rx;
		this.rz = rz;
	}

	@Override
	public boolean isValid() {
		return x != null && z != null && rx != null && rz != null && rx > 0 && rz > 0;
	}

	@Override
	public void forMap(BlueMapMap map) {
		this.tileSize = map.getTileSize().getX();
		this.tileOffset = map.getTileOffset().getX();
	}

	@Override
	public boolean containsTile(int tx, int tz) {
		int x = tx * tileSize + tileOffset;
		int z = tz * tileSize + tileOffset;
		for (int dx = 0; dx <= 1; dx++) {
			for (int dz = 0; dz <= 1; dz++) {
				if (Math.pow(x + dx * tileSize - this.x, 2) / Math.pow(rx, 2) + Math.pow(z + dz * tileSize - this.z, 2) / Math.pow(rz, 2) <= 1) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String debugString() {
		return "AreaEllipse: x=" + x + ", z=" + z + ", rx=" + rx + ", rz=" + rz;
	}

	@Override
	public ShapeMarker createBlockMarker(BlueMapMap map) {
		Shape shape = Shape.createEllipse(x, z, rx, rz, 24);
		return ShapeMarker.builder()
				.label(debugString())
				.shape(shape, 0)
				.depthTestEnabled(false)
				.lineColor(new Color(0, 0, 255, 1f))
				.fillColor(new Color(0, 0, 200, 0.3f))
				.build();
	}
}
