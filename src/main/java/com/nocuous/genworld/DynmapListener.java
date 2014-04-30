package com.nocuous.genworld;

import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;

public class DynmapListener extends DynmapCommonAPIListener {

	public boolean apiAvailable = false;
	public DynmapCommonAPI api;

	@Override
	public void apiEnabled(DynmapCommonAPI arg0) {
		// TODO Auto-generated method stub
		api = arg0;
		apiAvailable = true;

	}

	@Override
	public void apiDisabled(DynmapCommonAPI arg0) {
		apiAvailable = false;
		api = null;
	}

}
