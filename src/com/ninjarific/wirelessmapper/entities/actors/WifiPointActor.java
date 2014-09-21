package com.ninjarific.wirelessmapper.entities.actors;

import java.util.List;

import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.engine.MainEngineThread;
import com.ninjarific.wirelessmapper.entities.descriptors.WifiPointActorDescriptor;


public class WifiPointActor extends MoveableActor {
	private static final String TAG = "WifiPointActor";
	private static final boolean DEBUG = true;
	
	private List<WifiConnectionData> mScanConnections;
	private String mName;
	private Long mId;
	
	public WifiPointActor(WifiPointActorDescriptor desc, MainEngineThread mainEngineThread) {
		super(desc);
		WifiPoint point = desc.getPoint();
		mScanConnections = desc.getScanConnections();
		mName = point.getSsid();
		mId = point.getId();

		if (DEBUG) Log.d(TAG, "created WifiPointActor " + mId + " " + mName);
		if (mScanConnections.size() == 1) {
			if (DEBUG) Log.d(TAG, "\t single connection; set to ORBIT mode at radius " + mScanConnections.get(0));
			setOrbitRadius(mScanConnections.get(0).getLevel());
			setPosition(mainEngineThread
					.getScanActorById(mScanConnections
							.get(0)
							.getScan()
							.getId())
					.getPosition());
			setMode(Mode.ORBIT);
		}
		else if (DEBUG) Log.d(TAG, "\t " + mScanConnections.size() + " connections found, standard mode");
			
	}

	public String getActorLabel() {
		return mId.toString();
	}
	
}
