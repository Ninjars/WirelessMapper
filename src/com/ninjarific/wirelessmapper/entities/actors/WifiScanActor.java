package com.ninjarific.wirelessmapper.entities.actors;

import java.util.List;

import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.MainEngineThread;
import com.ninjarific.wirelessmapper.entities.descriptors.WifiScanActorDescriptor;


public class WifiScanActor extends MoveableActor {
	private static final String TAG = "WifiScanActor";
	private static final boolean DEBUG = true;
	
	private List<WifiConnectionData> mScanConnections;
	private String mName;
	private Long mId;

	public WifiScanActor(WifiScanActorDescriptor desc) {
		super(desc);
		if (DEBUG) Log.d(TAG, "created");
		WifiScan scan = desc.getScan();
		mScanConnections = desc.getScanConnections();
		mId = scan.getId();
		this.setMass(100);
	}
	
	public String getActorLabel() {
		return mId.toString();
	}

	public void loadPointConnections(MainEngineThread mainEngineThread) {
		for (WifiConnectionData connection : mScanConnections) {
			WifiPointActor targetActor = mainEngineThread.getPointActorById(connection.getPoint().getId());
			this.addForceSource(targetActor, connection.getLevel());
		}
	}
	
}
