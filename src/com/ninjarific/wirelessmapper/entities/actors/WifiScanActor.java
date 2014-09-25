package com.ninjarific.wirelessmapper.entities.actors;

import java.util.Set;

import android.graphics.PointF;
import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.MainEngineThread;


public class WifiScanActor extends MovableActor {
	private static final String TAG = "WifiScanActor";
	private static final boolean DEBUG = true;
	
	private Set<WifiConnectionData> mScanConnections;
	private String mName;
	private Long mId;

	public WifiScanActor(WifiScan scan, Set<WifiConnectionData> connections) {
		super(new PointF(0,0), true);
		if (DEBUG) Log.d(TAG, "created");
		mScanConnections = connections;
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
