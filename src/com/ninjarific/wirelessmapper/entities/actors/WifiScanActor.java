package com.ninjarific.wirelessmapper.entities.actors;

import android.util.Log;
import android.util.LongSparseArray;

import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.entities.descriptors.WifiScanActorDescriptor;


public class WifiScanActor extends MoveableActor {
	private static final String TAG = "WifiScanActor";
	private static final boolean DEBUG = true;
	
	private LongSparseArray<Integer> mScanConnections;
	private String mName;
	private Long mId;

	public WifiScanActor(WifiScanActorDescriptor desc) {
		super(desc);
		if (DEBUG) Log.d(TAG, "created");
		WifiScan scan = desc.getScan();
		mScanConnections = desc.getScanConnections();
		mId = scan.getId();
	}
	
	public String getActorLabel() {
		return mId.toString();
	}
	
}
