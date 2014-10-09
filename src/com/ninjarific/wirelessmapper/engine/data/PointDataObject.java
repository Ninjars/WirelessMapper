package com.ninjarific.wirelessmapper.engine.data;

import java.util.ArrayList;
import java.util.List;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.entities.actors.WifiPointActor;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class PointDataObject extends DataObject {
	
	private WifiPoint mPoint;
	private ArrayList<WifiConnectionData> mConnections;
	private List<WifiScan> mConnectedScans;
	private WifiPointActor mActor;

	public PointDataObject(WifiPoint point, DataManager dataManager) {
		mPoint = point;
		mConnections = new ArrayList<WifiConnectionData>();
		mConnections.addAll(dataManager.getConnectionsForPoint(mPoint));
		mConnectedScans = dataManager.getScansForPoint(mPoint);
		mActor = new WifiPointActor(mPoint);
	}
	
	public ArrayList<WifiConnectionData> getConnections() {
		return mConnections;
	}
	
	public List<WifiScan> getConnectedScans() {
		return mConnectedScans;
	}
	
	public WifiPointActor getActor() {
		return mActor;
	}
	
	public WifiPoint getPoint() {
		return mPoint;
	}
	
	@Override
	public boolean equals(Object e){
		return (e instanceof PointDataObject && ((PointDataObject) e).getPoint().equals(mPoint));
	}
	
	@Override
	public int hashCode(){
		return 29 + mPoint.hashCode();
	}

}
