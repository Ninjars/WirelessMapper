package com.ninjarific.wirelessmapper.entities.descriptors;

import java.util.List;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;

public class WifiPointActorDescriptor extends MovableActorDescriptor {

	private WifiPoint mPoint;
	private List<WifiConnectionData> mScanConnections;

	public WifiPointActorDescriptor(WifiPoint point, List<WifiConnectionData> scanConnections) {
		super();
		mPoint = point;
		mScanConnections = scanConnections;
	}
	
	public WifiPoint getPoint() {
		return mPoint;
	}

	public List<WifiConnectionData> getScanConnections() {
		return mScanConnections;
	}
	
}
