package com.technicjelle.bluemapareacontrol;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;

public interface Area {
	boolean isValid();

	void forMap(BlueMapMap map);

	boolean containsTile(int tx, int tz);

	String debugString();

	ShapeMarker createBlockMarker(BlueMapMap map);
}
