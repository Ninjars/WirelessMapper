package com.ninjarific.wirelessmapper.entities.actors;

import java.util.HashSet;
import java.util.Set;

import android.graphics.PointF;
import android.util.Log;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.engine.GameController;


public class WifiScanActor extends MovableActor {
	private static final String TAG = "WifiScanActor";
	private static final boolean DEBUG = true;
	
	private Set<WifiConnectionData> mScanConnections;
	private String mName;
	private Long mId;

	public WifiScanActor(WifiScan scan) {
		super(new PointF(0,0), 50, true);
		if (DEBUG) Log.d(TAG, "created");
		mScanConnections = new HashSet<WifiConnectionData>();
		mId = scan.getId();
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

	public void createForceConnections(GameController controller) {
		for (WifiConnectionData connection : mScanConnections) {
			WifiPointActor targetActor = controller.getPointActorById(connection.getPoint().getId());
			if (targetActor != null) {
				this.addForceSource(targetActor, connection.getLevel(), true);
			}
		}
	}
	
}
