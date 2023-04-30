package com.technicjelle.bluemapareacontrol;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Shape;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class AreaRect implements Area {
	@Comment("X coordinate of the top left corner in tiles")
	private final Integer x;
	@Comment("Z coordinate of the top left corner in tiles")
	private final Integer z;

	@Comment("Width of the rectangle in tiles")
	private final Integer width;
	@Comment("Height of the rectangle in tiles")
	private final Integer height;

	public static final String TYPE = "rect";
	private final String type = TYPE;

	private AreaRect() {
		this.x = null;
		this.z = null;
		this.width = null;
		this.height = null;
	}

	public AreaRect(int x, int z, int width, int height) {
		this.x = x;
		this.z = z;
		this.width = width;
		this.height = height;
	}

	@Override
	public boolean isValid() {
		return x != null && z != null && width != null && height != null && width > 0 && height > 0;
	}

	@Override
	public boolean containsTile(int x, int z) {
		return x >= this.x && x < this.x + width && z >= this.z && z < this.z + height;
	}

	@Override
	public String debugString() {
		return "AreaRect: x=" + x + ", z=" + z + ", width=" + width + ", height=" + height;
	}

	@Override
	public ShapeMarker createMarker(BlueMapMap map) {
		Vector2i tileSize = map.getTileSize();
		int sx = tileSize.getX();
		int sz = tileSize.getY();
		Vector2i tileOffset = map.getTileOffset();
		int ox = tileOffset.getX();
		int oz = tileOffset.getY();
		Shape shape = Shape.createRect(ox+x*sx, oz+z*sz, ox+(x + width)*sx, oz+(z + height)*sz);
		return new ShapeMarker(debugString() + this, shape, 90);
	}
}