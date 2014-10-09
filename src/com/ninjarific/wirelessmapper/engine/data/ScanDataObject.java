package com.ninjarific.wirelessmapper.engine.data;

import java.util.ArrayList;
import java.util.List;

import com.ninjarific.wirelessmapper.database.orm.models.WifiConnectionData;
import com.ninjarific.wirelessmapper.database.orm.models.WifiPoint;
import com.ninjarific.wirelessmapper.database.orm.models.WifiScan;
import com.ninjarific.wirelessmapper.entities.actors.WifiScanActor;
import com.ninjarific.wirelessmapper.wifidata.DataManager;

public class ScanDataObject extends DataObject {
	
	private WifiScan mScan;
	private ArrayList<WifiConnectionData> mConnections;
	private List<WifiPoint> mConnectedPoints;
	private ArrayList<WifiScan> mConnectedScans;
	private WifiScanActor mActor;
	private boolean mPointsProcessed = false;

	public ScanDataObject(WifiScan scan, DataManager dataManager) {
		mScan = scan;
		mConnections = new ArrayList<WifiConnectionData>();
		mConnections.addAll(dataManager.getConnectionsForScan(scan));
		mConnectedPoints = dataManager.getPointsForScan(scan);
		mConnectedScans = dataManager.getAllScansConnectedToScan(scan);
		mActor = new WifiScanActor(scan);
	}
	
	public ArrayList<WifiConnectionData> getConnections() {
		return mConnections;
	}
	
	public List<WifiPoint> getConnectedPoints() {
		return mConnectedPoints;
	}
	
	public ArrayList<WifiScan> getConnectedScans() {
		return mConnectedScans;
	}
	
	public WifiScanActor getActor() {
		return mActor;
	}
	
	public WifiScan getScan() {
		return mScan;
	}
	
	@Override
	public boolean equals(Object e){
		return (e instanceof ScanDataObject && ((ScanDataObject) e).getScan().equals(mScan));
	}
	
	@Override
	public int hashCode(){
		return 29 + mScan.hashCode();
	}

	/*
	 * returns true if the scan has been directly loaded before,
	 * and so it can be assumed that its connections have been
	 * made already.
	 */
	public boolean allPointsProcessed() {
		return mPointsProcessed;
	}

	/*
	 * call once all point connections have been processed,
	 * to avoid duplicating forces
	 */
	public void finishedProcessingPoints() {
		mPointsProcessed  = true;
	}

}
