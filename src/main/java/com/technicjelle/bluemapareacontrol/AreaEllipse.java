package com.technicjelle.bluemapareacontrol;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Shape;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class AreaEllipse implements Area {
	@Comment("Center X coordinate in tiles")
	private final Integer x;
	@Comment("Center Z coordinate in tiles")
	private final Integer z;

	@Comment("Radius X in tiles")
	private final Integer rx;
	@Comment("Radius Z in tiles")
	private final Integer rz;

	public static final String TYPE = "ellipse";
	private final String type = TYPE;

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
	public boolean containsTile(int x, int z) {
		return Math.pow(x - this.x, 2) / Math.pow(rx, 2) + Math.pow(z - this.z, 2) / Math.pow(rz, 2) <= 1;
	}

	@Override
	public String debugString() {
		return "AreaEllipse: x=" + x + ", z=" + z + ", rx=" + rx + ", ry=" + rz;
	}

	@Override
	public ShapeMarker createMarker(BlueMapMap map) {
		Vector2i tileSize = map.getTileSize();
		int sx = tileSize.getX();
		int sz = tileSize.getY();
		Vector2i tileOffset = map.getTileOffset();
		int ox = tileOffset.getX() + sx/2;
		int oz = tileOffset.getY() + sz/2;
		Shape shape = Shape.createEllipse(ox+ x *sx, oz+ z *sz, (rx+0.5)*sx, (rz +0.5)*sz, 24);
		return new ShapeMarker(debugString(), shape, 90);
	}
}
