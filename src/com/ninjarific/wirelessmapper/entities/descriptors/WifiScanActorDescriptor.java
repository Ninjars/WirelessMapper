package com.ninjarific.wirelessmapper.entities.descriptors;

import android.util.LongSparseArray;

import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;

public class WifiScanActorDescriptor extends MovableActorDescriptor {

	private WifiScan mScan;
	private LongSparseArray<Integer> mScanConnections;

	/*
	 * basic constructor
	 * TODO: constructor that includes movable actor information
	 */
	public WifiScanActorDescriptor(WifiScan scan, LongSparseArray<Integer> processedConnections) {
		super();
		mScan = scan;
		mScanConnections = processedConnections;
	}
	
	public WifiScan getScan() {
		return mScan;
	}

	public LongSparseArray<Integer> getScanConnections() {
		return mScanConnections;
	}
	
}
