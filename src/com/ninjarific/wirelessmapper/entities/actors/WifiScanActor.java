package com.ninjarific.wirelessmapper.entities.actors;

import java.util.HashSet;
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

	public WifiScanActor(WifiScan scan) {
		super(new PointF(0,0), true);
		if (DEBUG) Log.d(TAG, "created");
		mScanConnections = new HashSet<WifiConnectionData>();
		mId = scan.getId();
		this.setMass(100);
	}
	
	public String getActorLabel() {
		return mId.toString();
	}
	
	public void addConnections(Set<WifiConnectionData> connections) {
		mScanConnections.addAll(connections);
	}
	
	public void addConnections(WifiConnectionData connection) {
		mScanConnections.add(connection);
	}

	public void createForceConnections(MainEngineThread mainEngineThread) {
		for (WifiConnectionData connection : mScanConnections) {
			WifiPointActor targetActor = mainEngineThread.getPointActorById(connection.getPoint().getId());
			if (targetActor != null) {
				this.addForceSource(targetActor, connection.getLevel());
			}
		}
	}
	
}
