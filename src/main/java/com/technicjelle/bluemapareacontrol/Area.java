package com.technicjelle.bluemapareacontrol;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.ShapeMarker;

public interface Area {
	boolean isValid();

	boolean containsTile(int x, int z);

	String debugString();

	ShapeMarker createMarker(BlueMapMap map);
}
