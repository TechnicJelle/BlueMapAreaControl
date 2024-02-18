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
	private transient int halfTileSize;
	private transient int tileOffset;

	private transient int prxSq;
	private transient int przSq;

	public AreaEllipse() {
		x = null;
		z = null;
		rx = null;
		rz = null;
	}

	@Override
	public boolean isValid() {
		return x != null && z != null && rx != null && rz != null && rx > 0 && rz > 0;
	}

	@Override
	public void init(BlueMapMap map) {
		tileSize = map.getTileSize().getX();
		tileOffset = map.getTileOffset().getX();
		halfTileSize = tileSize / 2;

		//pad radius to make up for the check in the middle of tile
		prxSq = (rx + halfTileSize) * (rx + halfTileSize);
		przSq = (rz + halfTileSize) * (rz + halfTileSize);
	}

	@Override
	public boolean containsTile(int tx, int tz) { //tile
		float bx = (tx * tileSize) + tileOffset; //block
		float bz = (tz * tileSize) + tileOffset;

		//check in middle of tile
		float mtx = bx + halfTileSize - x;
		float mtz = bz + halfTileSize - z;

		return (mtx*mtx) / prxSq + (mtz*mtz) / przSq <= 1.0f;
	}

	@Override
	public String debugString() {
		return "AreaEllipse: x= " + x + ", z= " + z + ", rx= " + rx + ", rz= " + rz;
	}

	@Override
	public ShapeMarker createBlockMarker() {
		Shape shape = Shape.createEllipse(x, z, rx, rz, 24);
		return ShapeMarker.builder()
				.label(debugString())
				.detail("type = " + type + "<br>" +
						"x = " + x + "<br>" +
						"z = " + z + "<br>" +
						"rx = " + rx + "<br>" +
						"rz = " + rz)
				.shape(shape, 0)
				.depthTestEnabled(false)
				.lineColor(new Color(0, 0, 255, 1f))
				.fillColor(new Color(0, 0, 200, 0.3f))
				.build();
	}
}
