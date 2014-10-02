package com.ninjarific.wirelessmapper.entities.actors;

import java.util.HashSet;
import java.util.Set;

import android.graphics.PointF;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.engine.MainEngineThread;


public class WifiPointActor extends MovableActor {
	private static final String TAG = "WifiPointActor";
	private static final boolean DEBUG = true;
	
	private Set<WifiConnectionData> mScanConnections;
	private String mName;
	private Long mId;
	
	public WifiPointActor(WifiPoint point, MainEngineThread mainEngineThread) {
		super(new PointF(0,0), 5, true);
		mScanConnections = new HashSet<WifiConnectionData>();
		mName = point.getSsid();
		mId = point.getId();
			
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
			WifiScanActor targetActor = mainEngineThread.getScanActorById(connection.getPoint().getId());
			if (targetActor != null) {
				this.addForceSource(targetActor, connection.getLevel(), false);
			}
		}
	}
	
}
