package com.technicjelle.bluemapareacontrol;

import com.flowpowered.math.vector.Vector2i;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;
import de.bluecolored.bluemap.api.math.Color;
import de.bluecolored.bluemap.api.math.Shape;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;

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

	private transient int tx;
	private transient int tz;
	private transient int trx;
	private transient int trz;

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
	public void calculateTilePositions(BlueMapMap map) {
		Vector2i pos = map.posToTile(x, z);
		tx = pos.getX();
		tz = pos.getY();
		Vector2i size = map.posToTile(rx, rz);
		trx = size.getX();
		trz = size.getY();
	}

	@Override
	public boolean containsTile(int tx, int tz) {
		return Math.pow(tx - this.tx, 2) / Math.pow(trx, 2) + Math.pow(tz - this.tz, 2) / Math.pow(trz, 2) <= 1;
	}

	@Override
	public String debugString() {
		return "AreaEllipse: tx=" + tx + ", tz=" + tz + ", trx=" + trx + ", trz=" + trz;
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

	@Override
	public ShapeMarker createTileMarker(BlueMapMap map) {
		Vector2i tileSize = map.getTileSize();
		int sx = tileSize.getX();
		int sz = tileSize.getY();
		Vector2i tileOffset = map.getTileOffset();
		int ox = tileOffset.getX() + sx/2;
		int oz = tileOffset.getY() + sz/2;
		Shape shape = Shape.createEllipse(ox+tx*sx, oz+tz*sz, (trx+0.5)*sx, (trz+0.5)*sz, 24);
		return ShapeMarker.builder()
				.label(debugString())
				.shape(shape, 1)
				.depthTestEnabled(false)
				.build();
	}
}
