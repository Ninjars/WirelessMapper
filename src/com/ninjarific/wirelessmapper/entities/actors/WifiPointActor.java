package com.ninjarific.wirelessmapper.entities.actors;

import java.util.Set;

import android.graphics.PointF;
import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.engine.MainEngineThread;


public class WifiPointActor extends MovableActor {
	private static final String TAG = "WifiPointActor";
	private static final boolean DEBUG = true;
	
	private Set<WifiConnectionData> mScanConnections;
	private String mName;
	private Long mId;
	
	public WifiPointActor(WifiPoint point, Set<WifiConnectionData> scanConnections, MainEngineThread mainEngineThread) {
		super(new PointF(0,0), true);
		mScanConnections = scanConnections;
		mName = point.getSsid();
		mId = point.getId();

		if (DEBUG) Log.d(TAG, "created WifiPointActor " + mId + " " + mName);
//		if (mScanConnections.size() == 1) {
//			if (DEBUG) Log.d(TAG, "\t single connection; set to ORBIT mode at radius " + mScanConnections.get(0));
//			setOrbitRadius(mScanConnections.get(0).getLevel());
//			setPosition(mainEngineThread
//					.getScanActorById(mScanConnections
//							.get(0)
//							.getScan()
//							.getId())
//					.getPosition());
//			setMode(Mode.ORBIT);
//		}
//		else {
			if (DEBUG) Log.d(TAG, "\t " + mScanConnections.size() + " connections found, standard mode");
			for (WifiConnectionData connection : mScanConnections) {
				WifiScanActor targetActor = mainEngineThread.getScanActorById(connection.getScan().getId());
				this.addForceSource(targetActor, connection.getLevel());
			}
//		}
			
	}

	public String getActorLabel() {
		return mId.toString();
	}
	
}
