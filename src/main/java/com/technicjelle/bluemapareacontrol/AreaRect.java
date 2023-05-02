package com.technicjelle.bluemapareacontrol;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

@ConfigSerializable
public class AreaRect implements Area {
	@Comment("X coordinate of one corner of the rectangle in blocks")
	private final Integer x1;
	@Comment("Z coordinate of one corner of the rectangle in blocks")
	private final Integer z1;

	@Comment("X coordinate of the opposite corner of the rectangle in blocks")
	private final Integer x2;
	@Comment("Z coordinate of the opposite corner of the rectangle in blocks")
	private final Integer z2;

	public static final String TYPE = "rect";
	private final String type = TYPE;

	private transient int tx1; //tile top left
	private transient int tz1; //tile top left
	private transient int tx2; //tile bottom right
	private transient int tz2; //tile bottom right

	private AreaRect() {
		this.x1 = null;
		this.z1 = null;
		this.x2 = null;
		this.z2 = null;
	}

	public AreaRect(int x1, int z1, int x2, int z2) {
		//make sure x1/z1 is the top left corner and x2/z2 is the bottom right corner
		this.x1 = Math.min(x1, x2);
		this.z1 = Math.min(z1, z2);
		this.x2 = Math.max(x1, x2);
		this.z2 = Math.max(z1, z2);
	}

	@Override
	public boolean isValid() {
		return x1 != null && z1 != null && x2 != null && z2 != null && x1 < x2 && z1 < z2;
	}

	@Override
	public void calculateTilePositions(BlueMapMap map) {
		Vector2i pos = map.posToTile(x1, z1);
		tx1 = pos.getX();
		tz1 = pos.getY();
		Vector2i size = map.posToTile(x2, z2);
		tx2 = size.getX();
		tz2 = size.getY();
	}

	@Override
	public boolean containsTile(int tx, int tz) {
		return tx >= tx1 && tx <= tx2 && tz >= tz1 && tz <= tz2;
	}

	@Override
	public String debugString() {
		return "AreaRect tx1: " + tx1 + " tz1: " + tz1 + " tx2: " + tx2 + " tz2: " + tz2;
	}

	@Override
	public ShapeMarker createBlockMarker(BlueMapMap map) {
		Shape shape = Shape.createRect(x1, z1, x2+1, z2+1); //+1 because the shape is exclusive on the right and bottom side
		return ShapeMarker.builder()
				.label(debugString())
				.shape(shape, 0)
				.depthTestEnabled(false)
				.lineColor(new Color(0, 0, 255, 1f))
				.fillColor(new Color(0, 0, 200, 0.3f))
				.build();
	}

	@Override
	public ShapeMarker createTileMarker(BlueMapMap map) {
		Vector2i tileSize = map.getTileSize();
		int sx = tileSize.getX();
		int sz = tileSize.getY();
		Vector2i tileOffset = map.getTileOffset();
		int ox = tileOffset.getX();
		int oz = tileOffset.getY();
		Shape shape = Shape.createRect(ox+tx1*sx, oz+tz1*sz, ox+(tx2+1)*sx, oz+(tz2+1)*sz);
		return ShapeMarker.builder()
				.label(debugString())
				.shape(shape, 1)
				.depthTestEnabled(false)
				.lineColor(new Color(255, 0, 0, 1f))
				.fillColor(new Color(200, 0, 0, 0.3f))
				.build();
	}
}