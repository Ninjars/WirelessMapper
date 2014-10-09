package com.ninjarific.wirelessmapper.entities.actors;

import android.graphics.PointF;
import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;


public class WifiScanActor extends ForceActor {
	private static final String TAG = "WifiScanActor";
	private static final boolean DEBUG = true;
	
	private String mName;
	private Long mId;

	public WifiScanActor(WifiScan scan) {
		super(new PointF(0,0), 20);
		if (DEBUG) Log.d(TAG, "created");
		mId = scan.getId();
	}
	
	public String getActorLabel() {
		return mId.toString();
	}
	
}
