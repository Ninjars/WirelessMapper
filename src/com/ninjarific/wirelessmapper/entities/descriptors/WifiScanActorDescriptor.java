package com.ninjarific.wirelessmapper.entities.descriptors;

import java.util.List;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;

public class WifiScanActorDescriptor extends MovableActorDescriptor {

	private WifiScan mScan;
	private List<WifiConnectionData> mScanConnections;

	/*
	 * basic constructor
	 * TODO: constructor that includes movable actor information
	 */
	public WifiScanActorDescriptor(WifiScan scan, List<WifiConnectionData> connections) {
		super();
		mScan = scan;
		mScanConnections = connections;
	}
	
	public WifiScan getScan() {
		return mScan;
	}

	public List<WifiConnectionData> getScanConnections() {
		return mScanConnections;
	}
	
}
