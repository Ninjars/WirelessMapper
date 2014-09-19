package com.ninjarific.wirelessmapper.database.orm.models;

import com.j256.ormlite.field.DatabaseField;

public class WifiScanPointData extends BaseModel<Long> {
	public final static String WIFI_POINT_ID_FIELD_NAME = "point_id";
	public final static String WIFI_SCAN_ID_FIELD_NAME = "scan_id";
	public final static String LEVEL_ID_FIELD_NAME = "level_id";
	// This is a foreign object which just stores the id from the User object in this table.
	@DatabaseField(foreign = true, columnName = WIFI_POINT_ID_FIELD_NAME, canBeNull = false)
	WifiScan mScan;

	// This is a foreign object which just stores the id from the Post object in this table.
	@DatabaseField(foreign = true, columnName = WIFI_SCAN_ID_FIELD_NAME, canBeNull = false)
	WifiPoint mPoint;
	
	@DatabaseField(columnName = LEVEL_ID_FIELD_NAME, canBeNull = false)
	private int mLevel;
	
	public WifiScanPointData() {/* for ORMLite use */}
	
	public WifiScanPointData(WifiScan scan, WifiPoint point, int signalLevel) {
		mScan = scan;
		mPoint = point;
		mLevel = signalLevel;
	}
	
	public int getLevel() {
		return mLevel;
	}
	
	public WifiScan getScan() {
		return mScan;
	}
	
	public WifiPoint getPoint() {
		return mPoint;
	}
}
