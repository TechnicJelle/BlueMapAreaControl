package com.technicjelle.bluemapareacontrol;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;

public interface Area {
	boolean isValid();

	/**
	 * Does some one-time calculations to prepare for the next calls to {@link #containsTile(int, int)}.
	 */
	void init(BlueMapMap map);

	boolean containsTile(int tx, int tz);

	String debugString();

	ShapeMarker createBlockMarker();
}
