package com.ninjarific.wirelessmapper.entities.descriptors;

import java.util.HashMap;

import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;

public class WifiPointActorDescriptor extends MovableActorDescriptor {

	private WifiPoint mPoint;
	private HashMap<Long, Integer> mScanConnections;

	public WifiPointActorDescriptor(WifiPoint point, HashMap<Long, Integer> scanConnections) {
		super();
		mPoint = point;
		mScanConnections = scanConnections;
	}
	
	public WifiPoint getPoint() {
		return mPoint;
	}

	public HashMap<Long, Integer> getScanConnections() {
		return mScanConnections;
	}
	
}
