package com.technicjelle.bluemapareacontrol;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

import com.flowpowered.math.vector.Vector2i;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;

@ConfigSerializable
public class AreaRect implements Area {
	@Comment("X coordinate of one corner of the rectangle in blocks")
	private Integer x1;
	@Comment("Z coordinate of one corner of the rectangle in blocks")
	private Integer z1;

	@Comment("X coordinate of the opposite corner of the rectangle in blocks")
	private Integer x2;
	@Comment("Z coordinate of the opposite corner of the rectangle in blocks")
	private Integer z2;

	public static final String TYPE = "rect";
	private final String type = TYPE;

	private transient int tx1; //tile top left
	private transient int tz1; //tile top left
	private transient int tx2; //tile bottom right
	private transient int tz2; //tile bottom right

	private AreaRect() {
		x1 = null;
		z1 = null;
		x2 = null;
		z2 = null;
	}

	@Override
	public boolean isValid() {
		return x1 != null && z1 != null && x2 != null && z2 != null;
	}

	@Override
	public void init(BlueMapMap map) {
		//make sure x1/z1 is the top left corner and x2/z2 is the bottom right corner
		int mx1 = Math.min(x1, x2);
		int mz1 = Math.min(z1, z2);
		int mx2 = Math.max(x1, x2);
		int mz2 = Math.max(z1, z2);

		//save sorted values
		x1 = mx1;
		z1 = mz1;
		x2 = mx2;
		z2 = mz2;

		Vector2i pos1 = map.posToTile(x1, z1);
		tx1 = pos1.getX();
		tz1 = pos1.getY();
		Vector2i pos2 = map.posToTile(x2, z2);
		tx2 = pos2.getX();
		tz2 = pos2.getY();
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
}
